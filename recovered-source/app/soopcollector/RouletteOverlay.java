/*
 * Decompiled with CFR 0.152.
 */
package app.soopcollector;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

final class RouletteOverlay
implements AutoCloseable {
    private static final Gson GSON = new Gson();
    private static final int FIRST_PORT = 18765;
    private final HttpServer server;
    private final ScheduledExecutorService executor;
    private final int port;
    private final AtomicLong sequence = new AtomicLong();
    private final ArrayDeque<RouletteEvent> events = new ArrayDeque();
    private final Map<Long, Runnable> completionCallbacks = new ConcurrentHashMap<Long, Runnable>();
    private long fallbackReadyAt;
    private static final String OVERLAY_HTML = "<!doctype html>\n<html lang=\"ko\">\n<head>\n  <meta charset=\"utf-8\">\n  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">\n  <style>\n    *{box-sizing:border-box}html,body{width:100%;height:100%;margin:0;overflow:hidden;background:transparent;font-family:'Malgun Gothic','Noto Sans KR',sans-serif}\n    #stage{position:absolute;left:50%;top:50%;width:min(100vw,800px);height:min(100vh,600px);display:flex;flex-direction:column;align-items:center;justify-content:center;gap:14px;opacity:0;transform:translate(-50%,-47%) scale(.97);transform-origin:center;transition:opacity .3s,transform .3s}\n    #stage.show{opacity:1;transform:translate(-50%,-50%) scale(1)}\n    #donor{padding:7px 17px;border-radius:13px;background:rgba(255,255,255,.78);color:#71371f;font-size:22px;font-weight:900;box-shadow:0 5px 16px rgba(75,42,25,.13);letter-spacing:-.4px}\n    #singleSlot{width:520px;max-width:calc(100% - 60px);height:126px;overflow:hidden;border-radius:20px;background:rgba(45,48,54,.88);box-shadow:0 10px 30px rgba(25,20,18,.28),inset 0 1px 0 rgba(255,255,255,.12);color:#fff;text-align:center;backdrop-filter:blur(8px)}\n    #reelTrack{width:100%;will-change:transform;transform:translateY(0)}\n    .reelItem{height:126px;display:flex;align-items:center;justify-content:center;padding:18px 30px;font-size:42px;font-weight:1000;line-height:1.18;word-break:keep-all;overflow-wrap:anywhere;text-shadow:0 2px 5px rgba(0,0,0,.3)}\n    #singleSlot.win{animation:resultPop .42s ease-out;background:rgba(222,83,36,.94);box-shadow:0 12px 34px rgba(140,52,20,.36),0 0 0 3px rgba(255,224,196,.58)}\n    #counter{min-width:76px;padding:7px 14px;border-radius:12px;background:rgba(59,63,70,.68);color:#fff;font-size:18px;font-weight:900;text-align:center;box-shadow:0 5px 16px rgba(28,22,20,.2);text-shadow:0 1px 3px rgba(0,0,0,.3)}\n    #summary{display:none;max-width:650px;padding:10px 18px;border-radius:15px;background:rgba(255,255,255,.92);color:#4b352c;font-size:18px;font-weight:800;line-height:1.45;text-align:center;box-shadow:0 7px 22px rgba(43,30,24,.2)}\n    #summary.show{display:block;animation:summaryIn .24s ease-out}\n    @keyframes resultPop{0%{transform:scale(.992)}55%{transform:scale(1.012)}100%{transform:scale(1)}}\n    @keyframes summaryIn{from{opacity:0;transform:translateY(5px)}to{opacity:1;transform:translateY(0)}}\n  </style>\n</head>\n<body>\n  <main id=\"stage\"><div id=\"counter\">00/00</div><div id=\"singleSlot\"><div id=\"reelTrack\"><div class=\"reelItem\">READY</div></div></div><div id=\"donor\"></div><div id=\"summary\"></div></main>\n  <script>\n    const stage=document.getElementById('stage'),donor=document.getElementById('donor'),slot=document.getElementById('singleSlot'),reelTrack=document.getElementById('reelTrack'),counter=document.getElementById('counter'),summary=document.getElementById('summary');\n    let since=null,queue=[],busy=false;\n    const wait=ms=>new Promise(resolve=>setTimeout(resolve,ms));\n    let audioContext=null;\n    const audio=()=>{try{if(!audioContext)audioContext=new(window.AudioContext||window.webkitAudioContext)();if(audioContext.state==='suspended')audioContext.resume();return audioContext}catch(e){return null}};\n    function tone(frequency,duration,volume,type='triangle',delay=0){const context=audio();if(!context)return;const now=context.currentTime+delay,osc=context.createOscillator(),gain=context.createGain();osc.type=type;osc.frequency.setValueAtTime(frequency,now);gain.gain.setValueAtTime(.0001,now);gain.gain.exponentialRampToValueAtTime(volume,now+.004);gain.gain.exponentialRampToValueAtTime(.0001,now+duration);osc.connect(gain).connect(context.destination);osc.start(now);osc.stop(now+duration+.02)}\n    function rouletteTick(strength=1){tone(270+Math.random()*35,.052,.07*strength,'sine');tone(820+Math.random()*90,.024,.026*strength,'triangle',.006)}\n    async function playTickSequence(duration){const started=performance.now();while(performance.now()-started<duration-70){const progress=Math.min(1,(performance.now()-started)/duration);rouletteTick(.92-.20*progress);await wait(46+Math.pow(progress,2.55)*235)}}\n    function playWin(){tone(740,.30,.07,'sine');tone(1110,.38,.052,'sine',.09);tone(1480,.24,.028,'triangle',.18)}\n    const countText=(current,total)=>String(current).padStart(2,'0')+'/'+String(total).padStart(2,'0');\n    const resultSummary=(labels,counts)=>labels.map((label,index)=>counts[index]>0?label+' '+counts[index]+'\uac1c':'').filter(Boolean).join(' \u00b7 ');\n    async function spin(labels,winner,current,total){slot.classList.remove('win');counter.textContent=countText(current,total);const sequence=[];let step=Math.floor(Math.random()*labels.length);for(let turn=0;turn<22;turn++){sequence.push(labels[step%labels.length]);step++;}sequence.push(labels[winner]);reelTrack.innerHTML=sequence.map(label=>'<div class=\"reelItem\"></div>').join('');[...reelTrack.children].forEach((item,index)=>item.textContent=sequence[index]);reelTrack.style.transition='none';reelTrack.style.transform='translateY(0)';void reelTrack.offsetHeight;await wait(80);reelTrack.style.transition='transform 2.8s cubic-bezier(.10,.66,.16,1)';reelTrack.style.transform='translateY(-'+((sequence.length-1)*126)+'px)';const ticks=playTickSequence(2800);await wait(2920);await ticks;slot.classList.add('win');playWin();await wait(1250);}\n    async function play(event){busy=true;stage.classList.add('show');donor.textContent=event.nickname;reelTrack.innerHTML='<div class=\"reelItem\">READY</div>';reelTrack.style.transition='none';reelTrack.style.transform='translateY(0)';counter.textContent=countText(0,event.results.length);summary.textContent='';summary.classList.remove('show');const counts=Array(event.labels.length).fill(0);await wait(350);for(let i=0;i<event.results.length;i++){const winner=event.results[i];await spin(event.labels,winner,i+1,event.results.length);counts[winner]++;if(event.results.length>1){summary.textContent=resultSummary(event.labels,counts);summary.classList.add('show');}}try{await fetch('/api/complete?id='+event.id,{method:'POST',cache:'no-store'});}catch(e){}await wait(2000);stage.classList.remove('show');await wait(350);busy=false;runQueue();}\n    function runQueue(){if(!busy&&queue.length)play(queue.shift());}\n    async function poll(){try{const query=since===null?'-1':String(since);const response=await fetch('/api/events?since='+query,{cache:'no-store'});const data=await response.json();if(since===null)since=data.latest;else{for(const event of data.events)queue.push(event);since=data.latest;runQueue();}}catch(e){}setTimeout(poll,650);}poll();\n  </script>\n</body>\n</html>\n";

    private RouletteOverlay(HttpServer server, ScheduledExecutorService executor, int port) {
        this.server = server;
        this.executor = executor;
        this.port = port;
    }

    static RouletteOverlay start() {
        for (int port = 18765; port < 18775; ++port) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, r -> {
                    Thread thread = new Thread(r, "kanashi-overlay");
                    thread.setDaemon(true);
                    return thread;
                });
                RouletteOverlay overlay = new RouletteOverlay(server, executor, port);
                server.createContext("/overlay", overlay::serveOverlay);
                server.createContext("/api/events", overlay::serveEvents);
                server.createContext("/api/complete", overlay::serveComplete);
                server.setExecutor(executor);
                server.start();
                return overlay;
            }
            catch (IOException iOException) {
                continue;
            }
        }
        return new RouletteOverlay(null, null, 0);
    }

    boolean isRunning() {
        return this.server != null;
    }

    String url() {
        return this.isRunning() ? "http://127.0.0.1:" + this.port + "/overlay" : "\uc624\ubc84\ub808\uc774 \uc11c\ubc84 \uc2dc\uc791 \uc2e4\ud328";
    }

    void publish(String nickname, List<String> labels, List<Double> probabilities, List<Integer> results) {
        this.publish(nickname, labels, probabilities, results, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void publish(String nickname, List<String> labels, List<Double> probabilities, List<Integer> results, Runnable onComplete) {
        if (!this.isRunning()) {
            return;
        }
        long id = this.sequence.incrementAndGet();
        RouletteEvent event = new RouletteEvent(id, nickname, List.copyOf(labels), List.copyOf(probabilities), List.copyOf(results));
        if (onComplete != null) {
            this.completionCallbacks.put(id, onComplete);
        }
        long fallbackDelay = 0L;
        ArrayDeque<RouletteEvent> arrayDeque = this.events;
        synchronized (arrayDeque) {
            this.events.addLast(event);
            while (this.events.size() > 100) {
                this.events.removeFirst();
            }
            if (onComplete != null) {
                long now = System.currentTimeMillis();
                long playbackMillis = 350L + (long)Math.max(1, results.size()) * 4250L;
                this.fallbackReadyAt = Math.max(now, this.fallbackReadyAt) + playbackMillis;
                fallbackDelay = Math.max(1L, this.fallbackReadyAt - now + 5000L);
            }
        }
        if (onComplete != null) {
            this.executor.schedule(() -> this.completeEvent(id), fallbackDelay, TimeUnit.MILLISECONDS);
        }
    }

    private void serveOverlay(HttpExchange exchange) throws IOException {
        RouletteOverlay.send(exchange, 200, "text/html; charset=utf-8", OVERLAY_HTML.getBytes(StandardCharsets.UTF_8));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void serveEvents(HttpExchange exchange) throws IOException {
        long since = RouletteOverlay.queryLong(exchange.getRequestURI().getRawQuery(), "since", -1L);
        ArrayList<RouletteEvent> result = new ArrayList<RouletteEvent>();
        long latest = this.sequence.get();
        if (since >= 0L) {
            ArrayDeque<RouletteEvent> arrayDeque = this.events;
            synchronized (arrayDeque) {
                for (RouletteEvent event : this.events) {
                    if (event.id <= since) continue;
                    result.add(event);
                }
            }
        }
        RouletteOverlay.send(exchange, 200, "application/json; charset=utf-8", GSON.toJson(new EventBatch(latest, result)).getBytes(StandardCharsets.UTF_8));
    }

    private void serveComplete(HttpExchange exchange) throws IOException {
        long id = RouletteOverlay.queryLong(exchange.getRequestURI().getRawQuery(), "id", -1L);
        this.completeEvent(id);
        RouletteOverlay.send(exchange, 200, "text/plain; charset=utf-8", "ok".getBytes(StandardCharsets.UTF_8));
    }

    private void completeEvent(long id) {
        Runnable callback = this.completionCallbacks.remove(id);
        if (callback != null) {
            callback.run();
        }
    }

    private static long queryLong(String query, String key, long fallback) {
        if (query == null) {
            return fallback;
        }
        for (String item : query.split("&")) {
            String[] pair = item.split("=", 2);
            if (pair.length != 2 || !pair[0].equals(key)) continue;
            try {
                return Long.parseLong(pair[1]);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return fallback;
    }

    private static void send(HttpExchange exchange, int status, String contentType, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody();){
            output.write(bytes);
        }
    }

    @Override
    public void close() {
        this.completionCallbacks.clear();
        if (this.server != null) {
            this.server.stop(0);
        }
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }

    private record RouletteEvent(long id, String nickname, List<String> labels, List<Double> probabilities, List<Integer> results) {
    }

    private record EventBatch(long latest, List<RouletteEvent> events) {
    }
}
