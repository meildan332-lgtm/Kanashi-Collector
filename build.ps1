param(
    [string]$Version = "1.2.0",
    [switch]$TestMode,
    [switch]$Installer
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Zulu\zulu-17" }
$javac = Join-Path $javaHome "bin\javac.exe"
$jarTool = Join-Path $javaHome "bin\jar.exe"
$baseZip = Join-Path $repo "dist\Kanashi-Collector-v1.0.2-Portable.zip"
$buildLabel = if ($TestMode) { "$Version-test" } else { $Version }
$buildRoot = Join-Path $repo "work\build-$buildLabel"
$stage = Join-Path $buildRoot "portable"
$classes = Join-Path $buildRoot "classes"
$testClasses = Join-Path $buildRoot "test-classes"
$outputName = if ($TestMode) { "Kanashi-Collector-TEST-v$Version-Portable.zip" } else { "Kanashi-Collector-v$Version-Portable.zip" }
$output = Join-Path $repo "dist\$outputName"
$installerOutput = Join-Path $repo "dist\Kanashi-Collector-Setup.exe"
$webViewDir = Join-Path $repo "work\swingwebview-1.0.10"
$webViewJar = Join-Path $webViewDir "webview-1.0.10.jar"
$webViewUrl = "https://repo1.maven.org/maven2/ca/weblite/webview/1.0.10/webview-1.0.10.jar"

if (!(Test-Path -LiteralPath $javac) -or !(Test-Path -LiteralPath $jarTool)) {
    throw "Java 17 JDK를 찾지 못했습니다. JAVA_HOME을 설정해 주세요."
}
if (!(Test-Path -LiteralPath $baseZip)) {
    throw "기준 포터블 ZIP을 찾지 못했습니다: $baseZip"
}
if (!(Test-Path -LiteralPath $webViewJar)) {
    New-Item -ItemType Directory -Force -Path $webViewDir | Out-Null
    Write-Host "Downloading Swing WebView 1.0.10..."
    Invoke-WebRequest -Uri $webViewUrl -OutFile $webViewJar
}

if (Test-Path -LiteralPath $buildRoot) {
    $resolvedBuildRoot = (Resolve-Path -LiteralPath $buildRoot).Path
    $resolvedWorkRoot = (Resolve-Path -LiteralPath (Join-Path $repo "work")).Path
    if (!$resolvedBuildRoot.StartsWith($resolvedWorkRoot + [IO.Path]::DirectorySeparatorChar)) {
        throw "빌드 폴더가 work 폴더 밖에 있어 정리할 수 없습니다."
    }
    [IO.Directory]::Delete($resolvedBuildRoot, $true)
}
New-Item -ItemType Directory -Force -Path $stage, $classes, $testClasses | Out-Null
Expand-Archive -LiteralPath $baseZip -DestinationPath $stage -Force
$launcherConfig = Join-Path $stage "app\Kanashi Collector.cfg"
$webViewJars = Get-Item -LiteralPath $webViewJar
$configText = Get-Content -LiteralPath $launcherConfig -Raw
$classPathLines = ($webViewJars | ForEach-Object { "app.classpath=`$APPDIR\webview\$($_.Name)" }) -join "`r`n"
$configText = $configText.Replace("app.classpath=`$APPDIR\SOOP-Collector.jar", "app.classpath=`$APPDIR\SOOP-Collector.jar`r`n$classPathLines")
if ($TestMode) {
    $configText += "`r`njava-options=-Dkanashi.testMode=true`r`n"
}
Set-Content -LiteralPath $launcherConfig -Value $configText -Encoding UTF8
New-Item -ItemType Directory -Force -Path (Join-Path $stage "app\webview") | Out-Null
Copy-Item -LiteralPath $webViewJars.FullName -Destination (Join-Path $stage "app\webview") -Force
$appJar = Join-Path $stage "app\SOOP-Collector.jar"
$sources = Get-ChildItem -LiteralPath (Join-Path $repo "recovered-source") -Recurse -Filter "*.java"
$sourcePaths = $sources | ForEach-Object FullName
$compileClassPath = $appJar
$compileClassPath += ";" + (($webViewJars | ForEach-Object FullName) -join ";")
& $javac -encoding UTF-8 -cp $compileClassPath -d $classes $sourcePaths
if ($LASTEXITCODE -ne 0) { throw "애플리케이션 컴파일 실패" }

$tests = Get-ChildItem -LiteralPath (Join-Path $repo "tests") -Recurse -Filter "*.java" | ForEach-Object FullName
& $javac -encoding UTF-8 -cp "$classes;$compileClassPath" -d $testClasses $tests
if ($LASTEXITCODE -ne 0) { throw "테스트 컴파일 실패" }
& (Join-Path $javaHome "bin\java.exe") -cp "$testClasses;$classes;$compileClassPath" app.soopcollector.SecuritySmokeTest
if ($LASTEXITCODE -ne 0) { throw "테스트 실패" }

Push-Location $classes
try {
    & $jarTool uf $appJar .
    if ($LASTEXITCODE -ne 0) { throw "JAR 갱신 실패" }
} finally {
    Pop-Location
}

if (Test-Path -LiteralPath $output) { Remove-Item -LiteralPath $output -Force }
Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $output -CompressionLevel Optimal
$hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $output).Hash.ToLowerInvariant()
Write-Host "Built: $output"
Write-Host "SHA256: $hash"

if ($Installer) {
    if ($TestMode) { throw "테스트 모드는 설치 파일로 패키징할 수 없습니다." }
    $jpackage = Join-Path $javaHome "bin\jpackage.exe"
    if (!(Test-Path -LiteralPath $jpackage)) { throw "jpackage를 찾지 못했습니다." }
    $wixDir = Join-Path $repo "work\wix314"
    $wixZip = Join-Path $repo "work\wix314-binaries.zip"
    if (!(Test-Path -LiteralPath (Join-Path $wixDir "candle.exe"))) {
        if (!(Test-Path -LiteralPath $wixZip)) {
            Invoke-WebRequest -Uri "https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip" -OutFile $wixZip
        }
        New-Item -ItemType Directory -Force -Path $wixDir | Out-Null
        Expand-Archive -LiteralPath $wixZip -DestinationPath $wixDir -Force
    }
    $packageDir = Join-Path $buildRoot "installer"
    New-Item -ItemType Directory -Force -Path $packageDir | Out-Null
    $previousPath = $env:PATH
    try {
        $env:PATH = "$wixDir;$previousPath"
        & $jpackage --type exe --name "Kanashi Collector" --app-version $Version --vendor "Kanashi" `
            --description "SOOP donation and chat collector with NashiBall" --app-image $stage `
            --dest $packageDir --resource-dir (Join-Path $repo "installer") `
            --icon (Join-Path $stage "Kanashi Collector.ico") --win-per-user-install `
            --win-shortcut --win-menu --win-menu-group "Kanashi" `
            --win-upgrade-uuid "BFA65E35-69A9-4C7C-A1A9-D99B5157BC9D"
        if ($LASTEXITCODE -ne 0) { throw "설치 파일 생성 실패" }
    } finally {
        $env:PATH = $previousPath
    }
    $generatedInstaller = Get-ChildItem -LiteralPath $packageDir -Filter "*.exe" | Select-Object -First 1
    if ($null -eq $generatedInstaller) { throw "생성된 설치 파일을 찾지 못했습니다." }
    Copy-Item -LiteralPath $generatedInstaller.FullName -Destination $installerOutput -Force
    $installerHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $installerOutput).Hash.ToLowerInvariant()
    Write-Host "Built: $installerOutput"
    Write-Host "SHA256: $installerHash"
}
