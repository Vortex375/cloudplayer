package de.pandaserv.music.client.audio;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import de.pandaserv.music.shared.NotSupportedException;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/12/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AudioSystem {
    private MediaElement mediaElement;
    private JavaScriptObject audioContext;
    private JavaScriptObject analyserNode;

    private Timer visDataTimer;

    public AudioSystem(MediaElement mediaElement) throws NotSupportedException {
        this.mediaElement = mediaElement;

        visDataTimer = new Timer() {
            @Override
            public void run() {
                updateVisData();
            }
        };

        audioContext = createContext();
        if (audioContext == null) {
            throw new NotSupportedException();
        }
        /*
         * delay connecting the audio nodes until after the
         * window.onload event
         * as a workaround to http://crbug.com/112368
         */
        delayedStart();
    }

    private native void delayedStart() /*-{
        var that = this;
        $wnd.addEventListener('load', function(e) {
            that.@de.pandaserv.music.client.audio.AudioSystem::start()();
        })
    }-*/;

    void start() {
        setup(audioContext, mediaElement);
    }

    private native JavaScriptObject createContext()/*-{
        console.log("creating audio context...");
        if ($wnd.webkitAudioContext) {
            return new $wnd.webkitAudioContext();
        } else if ($wnd.mozAudioContext){
            return new $wnd.mozAudioContext();
        } else {
            return null;
        }

    }-*/;

    private native void setup(JavaScriptObject context, MediaElement element) /*-{
        console.log("creating and connecting audio nodes");
        var sourceNode = context.createMediaElementSource(element);
        var analyserNode = context.createAnalyser();
        analyserNode.fftSize = 512;
        analyserNode.minDecibels = -100;
        analyserNode.maxDecibels = 0;
        console.log("minDb: " + analyserNode.minDecibels);
        console.log("maxDb: " + analyserNode.maxDecibels);

        sourceNode.connect(analyserNode);
        analyserNode.connect(context.destination);
        console.log("audio setup complete");
        // pass the analyserNode to java code
        this.@de.pandaserv.music.client.audio.AudioSystem::setAnalyserNode(Lcom/google/gwt/core/client/JavaScriptObject;)(analyserNode);
    }-*/;

    public void startCollectVisData() {
        visDataTimer.scheduleRepeating(33); // 30 FPS
    }

    public void stopCollectVisData() {
        visDataTimer.cancel();
    }

    void setAnalyserNode(JavaScriptObject node) {
        this.analyserNode = node;
    }

    private void updateVisData() {
        doCollectVisData(analyserNode);
    }

    private native void doCollectVisData(JavaScriptObject analyser) /*-{
        var byteArray = new Uint8Array(256);
        analyser.getByteFrequencyData(byteArray);
        //var debug= "freq.data: ";
        //for (var i = 0; i < byteArray.length; i++) {
        //    debug += byteArray[i] + ", "
        //}
        //console.log(debug);
        this.@de.pandaserv.music.client.audio.AudioSystem::drawVis(Lcom/google/gwt/core/client/JavaScriptObject;)(byteArray);
    }-*/;

    private native void drawVis(JavaScriptObject data)/*-{
        var scale = [0.0, 0.58740105196819947475, 1.51984209978974632953, 3, 5.34960420787279789901,
            9.07936839915898531814, 15, 24.39841683149119159603, 39.31747359663594127255, 63, 100.59366732596476638411,
            160.2698943865437650902, 255];

        var bars = new Array();
        for (var i = 0; i < 12; i++) {
            var a = Math.ceil(scale[i]);
            var b = Math.floor(scale[i+1]);
            var dif = b - a;
            var n = 0;

            if (b < a){
                n += data[b] * (scale[i + 1] - scale[i]);
            } else {

                if (a > 0){
                    n += data[a - 1] * (a - scale[i]);
                }
                while (a < b){
                    n += data[a];
                    a += 1;
                }
                if (b < 256) {
                    n += data[b] * (scale[i + 1] - b);
                }
            }
            if (dif > 0) {
                n /= dif;
            }

            //var x;
            //if (n == 0) {
            //    x = 0
            //} else {
            //    x = 20 * Math.log(n * 100) / Math.LN10;
            //}
            //x = Math.max(0, Math.min(x, 100));

            //TODO: falloff
            bars.push(n);
        }
        console.log("bars: " + bars);
    }-*/;
}
