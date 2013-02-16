package de.pandaserv.music.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.shared.NotSupportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ich
 * Date: 2/12/13
 * Time: 11:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AudioSystem {
    static class VisArray {
        private double[] data;

        private VisArray(int size) {
            data = new double[size];
        }

        public static VisArray create(int size) {
            return new VisArray(size);
        }

        public void set(int index, double value) {
            data[index] = value;
        }

        public double[] asArray() {
            return data;
        }
    }

    public interface VisDataHandler {
        public void onVisDataUpdate(int[] data);
    }

    private static final int FFT_SIZE = 512;
    private static final int VIS_BARS = 12;
    private static final int VIS_FPS = 60;
    private static final int VIS_DELAY = 4; /* delay before falloff in frames */
    private static final int VIS_FALLOFF = 4; /* falloff in pixels per frame */

    private MediaElement mediaElement;
    private JavaScriptObject audioContext;
    private JavaScriptObject analyserNode;

    private List<VisDataHandler> handlers;
    private Timer visDataTimer;
    private int[] bars;
    private byte[] delay;

    private double[] scale;
    private boolean haveScale;

    public AudioSystem(MediaElement mediaElement) throws NotSupportedException {
        this.mediaElement = mediaElement;

        visDataTimer = new Timer() {
            @Override
            public void run() {
                updateVisData();
            }
        };

        bars = new int[VIS_BARS];
        delay = new byte[VIS_BARS];
        handlers = new ArrayList<VisDataHandler>();
        haveScale = false;

        audioContext = createContext();
        if (audioContext == null) {
            GWT.log("Web Audio API not supported");
            throw new NotSupportedException();
        }
        /*
         * delay connecting the audio nodes
         * as a workaround to http://crbug.com/112368
         */
        new Timer() {
            @Override
            public void run() {
                start();
            }
        }.schedule(1);
    }

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
        analyserNode.fftSize = 512; //TODO: use FFT_SIZE constant
        analyserNode.minDecibels = -60;
        analyserNode.maxDecibels = 0;
        analyserNode.smoothingTimeConstant = 0.3;
        console.log("minDb: " + analyserNode.minDecibels);
        console.log("maxDb: " + analyserNode.maxDecibels);
        console.log("smoothing: " + analyserNode.smoothingTimeConstant);

        sourceNode.connect(analyserNode);
        analyserNode.connect(context.destination);
        console.log("audio setup complete");
        // pass the analyserNode to java code
        this.@de.pandaserv.music.client.audio.AudioSystem::setAnalyserNode(Lcom/google/gwt/core/client/JavaScriptObject;)(analyserNode);
    }-*/;

    public void startCollectVisData() {
        visDataTimer.scheduleRepeating(1000 / VIS_FPS);
    }

    public void stopCollectVisData() {
        visDataTimer.cancel();
    }

    void setAnalyserNode(JavaScriptObject node) {
        this.analyserNode = node;
    }

    private void updateVisData() {
        VisArray visArray = VisArray.create(FFT_SIZE / 2);
        doCollectVisData(analyserNode, visArray);
        formatVis(visArray.asArray());

        for (VisDataHandler handler: handlers) {
            handler.onVisDataUpdate(this.bars);
        }
    }

    private native void doCollectVisData(JavaScriptObject analyser, VisArray visArray) /*-{
        var byteArray = new Uint8Array(256); // TODO: use FFT_SIZE / 2 constant
        analyser.getByteFrequencyData(byteArray);

        for (var i = 0; i < byteArray.length; i++) {
            visArray.@de.pandaserv.music.client.audio.AudioSystem.VisArray::set(ID)(i, byteArray[i]);
        }
    }-*/;

    private void calculateScale() {
        // calculate logarithmic scale
        scale = new double[VIS_BARS + 1];
        for (int i = 0; i < scale.length; i++) {
            scale[i] = Math.pow(FFT_SIZE / 2, i / (double) VIS_BARS) - 1;
        }
        GWT.log("calculated logarithmic scale: " + Arrays.toString(scale));
        haveScale = true;
    }

    private void formatVis(double[] fftData) {
        GWT.log("formatVis(): fftData=" + Arrays.toString(fftData));
        /*double[] scale = {0.0, 0.58740105196819947475, 1.51984209978974632953, 3, 5.34960420787279789901,
                9.07936839915898531814, 15, 24.39841683149119159603, 39.31747359663594127255, 63, 100.59366732596476638411,
                160.2698943865437650902, 255};*/
        if (!haveScale) {
            calculateScale();
        }

        int bandsPerBar = fftData.length / VIS_BARS;
        double[] bars = new double[VIS_BARS];
        for (int i = 0; i < bars.length; i++) {
            int a = (int) Math.ceil(scale[i]);
            int b = (int) Math.floor(scale[i+1]);
            int dif = b - a;
            double n = 0;

            if (b < a){
                n += fftData[b] * (scale[i + 1] - scale[i]);
            } else {

                if (a > 0){
                    n += fftData[a - 1] * (a - scale[i]);
                }
                while (a < b){
                    n += fftData[a];
                    a++;
                }
                if (b < (FFT_SIZE / 2)) {
                    n += fftData[b] * (scale[i + 1] - b);
                }
            }

            double x;
            if (n == 0) {
                x = 0;
            } else {
                //x = 20 * Math.log10(n * 100);
                //x = 20 * Math.log10(n * 100);
                //x = 60 * Math.log10(n);
                //x = (Math.pow(255, n / 255)) - 1;
                x = 127 * (Math.log(n) / Math.log(127));
            }
            x = Math.max(0, Math.min(x, 256));

            /*double x = 0;
            for (int j = 0; j < bandsPerBar; j++) {
                x += fftData[i*bandsPerBar+j];
            }
            x = 40 * Math.log10(x);
            x = Math.max(0, Math.min(x, 256));*/


            this.bars[i] -= Math.max(0, VIS_FALLOFF - this.delay[i]);
            if (delay[i] > 0) {
                delay[i]--;
            }
            if (x > this.bars[i]) {
                this.bars[i] = (int) x;
                delay[i] = VIS_DELAY;
            }
        }
    }

    public HandlerRegistration addVisDataHandler(final VisDataHandler handler) {
        handlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                handlers.remove(handler);
            }
        };
    }
}
