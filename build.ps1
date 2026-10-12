param(
    [string]$Version = "1.1.0"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Zulu\zulu-17" }
$javac = Join-Path $javaHome "bin\javac.exe"
$jarTool = Join-Path $javaHome "bin\jar.exe"
$baseZip = Join-Path $repo "dist\Kanashi-Collector-v1.0.2-Portable.zip"
$buildRoot = Join-Path $repo "work\build-$Version"
$stage = Join-Path $buildRoot "portable"
$classes = Join-Path $buildRoot "classes"
$testClasses = Join-Path $buildRoot "test-classes"
$output = Join-Path $repo "dist\Kanashi-Collector-v$Version-Portable.zip"

if (!(Test-Path -LiteralPath $javac) -or !(Test-Path -LiteralPath $jarTool)) {
    throw "Java 17 JDK를 찾지 못했습니다. JAVA_HOME을 설정해 주세요."
}
if (!(Test-Path -LiteralPath $baseZip)) {
    throw "기준 포터블 ZIP을 찾지 못했습니다: $baseZip"
}

New-Item -ItemType Directory -Force -Path $stage, $classes, $testClasses | Out-Null
Expand-Archive -LiteralPath $baseZip -DestinationPath $stage -Force
$appJar = Join-Path $stage "app\SOOP-Collector.jar"
$sources = Get-ChildItem -LiteralPath (Join-Path $repo "recovered-source") -Recurse -Filter "*.java" | ForEach-Object FullName
& $javac -encoding UTF-8 -cp $appJar -d $classes $sources
if ($LASTEXITCODE -ne 0) { throw "애플리케이션 컴파일 실패" }

$tests = Get-ChildItem -LiteralPath (Join-Path $repo "tests") -Recurse -Filter "*.java" | ForEach-Object FullName
& $javac -encoding UTF-8 -cp "$classes;$appJar" -d $testClasses $tests
if ($LASTEXITCODE -ne 0) { throw "테스트 컴파일 실패" }
& (Join-Path $javaHome "bin\java.exe") -cp "$testClasses;$classes;$appJar" app.soopcollector.SecuritySmokeTest
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
