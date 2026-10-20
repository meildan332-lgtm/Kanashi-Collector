package app.soopcollector;

import ca.weblite.webview.swing.WebViewComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Test-build browser powered by the system Edge WebView2 Chromium runtime. */
public final class NashiBallBrowserPanel extends JPanel implements AutoCloseable {
    private static final String SITE_URL = "https://kanashi-pinball-roulette.bloomyhawk11.chatgpt.site/";
    private final WebViewComponent browser;

    public NashiBallBrowserPanel(Supplier<String> entries) {
        super(new BorderLayout(0, 8));
        Objects.requireNonNull(entries);
        setBorder(new EmptyBorder(10, 18, 16, 18));
        setBackground(new Color(250, 247, 242));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);
        JLabel title = new JLabel(Boolean.getBoolean("kanashi.testMode") ? "나시볼 · TEST" : "나시볼");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 17f));
        JButton reload = new JButton("새로고침");
        toolbar.add(title);
        toolbar.add(reload);
        add(toolbar, BorderLayout.NORTH);

        System.setProperty(WebViewComponent.MODE_PROPERTY, "heavyweight");
        this.browser = WebViewComponent.create(WebViewComponent.Mode.HEAVYWEIGHT);
        this.browser.setDefaultContextMenuEnabled(false);
        this.browser.addJavascriptFunction("kanashiCollectedBalls", (String ignored) -> entries.get());
        this.browser.addOnBeforeLoad(
                "document.addEventListener('DOMContentLoaded',function(){"
                + "document.querySelectorAll('audio,video').forEach(function(m){m.muted=true;m.volume=0;});"
                + "var winner=document.querySelector('#in_winningRank');"
                + "if(!winner||document.querySelector('#btnKanashiImport'))return;"
                + "var row=document.createElement('div');row.className='row kanashi-import-row';"
                + "row.innerHTML='<label><span>공 수집</span></label><div class=\"kanashi-import-wrap\"><button type=\"button\" id=\"btnKanashiImport\">수집한 공 불러오기</button><span id=\"kanashiImportStatus\"></span></div>';"
                + "var style=document.createElement('style');style.textContent='.kanashi-import-row{align-items:center}.kanashi-import-wrap{display:flex;align-items:center;gap:7px;flex:1}.kanashi-import-wrap button{white-space:nowrap;padding:5px 9px}.kanashi-import-wrap span{font-size:11px;white-space:nowrap}';document.head.appendChild(style);"
                + "var winnerRow=winner.closest('.row');winnerRow.parentNode.insertBefore(row,winnerRow.nextSibling);"
                + "document.querySelector('#btnKanashiImport').addEventListener('click',async function(){"
                + "var status=document.querySelector('#kanashiImportStatus');status.textContent='불러오는 중...';"
                + "try{var names=await window.kanashiCollectedBalls('');"
                + "if(!names||!names.trim()){status.textContent='수집된 공 없음';return;}"
                + "var input=document.querySelector('#in_names');input.value=names;"
                + "input.dispatchEvent(new Event('input',{bubbles:true}));input.dispatchEvent(new Event('blur',{bubbles:true}));"
                + "if(typeof getReady==='function')getReady();"
                + "var count=names.split(/[\\r\\n,]+/).filter(Boolean).reduce(function(sum,item){var m=item.match(/\\*(\\d+)/);return sum+(m?parseInt(m[1],10):1);},0);"
                + "status.textContent=count+'개 불러옴';}catch(e){status.textContent='불러오기 실패';}"
                + "});"
                + "});");
        this.browser.setUrl(SITE_URL);
        reload.addActionListener(event -> this.browser.setUrl(SITE_URL));
        add(this.browser, BorderLayout.CENTER);
    }

    @Override
    public void close() {
        this.browser.dispose();
    }
}
