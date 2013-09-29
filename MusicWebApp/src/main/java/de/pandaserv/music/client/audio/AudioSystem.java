package de.pandaserv.music.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.math.FFT;
import de.pandaserv.music.client.misc.NotSupportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * The formatVis() method is based on code from Audacious Media Player (http://audacious-media-player.org/)
 *
 * The following is the copyright notice from "ui_infoarea.c" where the code was taken from:
 *
 * ui_infoarea.c
 * Copyright 2010-2012 William Pitcock and John Lindgren
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions, and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions, and the following disclaimer in the documentation
 *    provided with the distribution.
 *
 * This software is provided "as is" and without any warranty, express or
 * implied. In no event shall the authors be liable for any damages arising from
 * the use of this software.
 */
public class AudioSystem {
    static class VisArray {
        private double[] data;

        public VisArray(int size) {
            data = new double[size];
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

    private Element mediaElement;
    private JavaScriptObject audioContext;
    private boolean connected;

    /* Visualization stuff */
    private static final int FFT_SIZE = 1024;
    private static final int VIS_BARS = 30;
    private static final int VIS_FPS = 30;
    private static final int VIS_DELAY = 2;     /* delay before falloff in frames */
    private static final int VIS_FALLOFF = 8;   /* falloff in pixels per frame */

    private List<VisDataHandler> handlers;
    private Timer visDataTimer;                 /* fired VIS_FPS times per second */
    private int[] bars;                         /* current value of the (visible) spectrum bars */
    private byte[] delay;                       /* delay value for each bar until falloff begins */
    private double[] scale;                     /* logarithmic scale for transformation from FFT output -> vis bars */
    private boolean haveScale;

    public AudioSystem() throws NotSupportedException {

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
        mediaElement = null;
        connected = false;
    }

    public void setMediaElement(final MediaElement mediaElement) {
        this.mediaElement = mediaElement;
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

    private native void setup(JavaScriptObject context, Element element) /*-{
        console.log("creating and connecting audio nodes");
        $wnd.audioSystemSourceNode = context.createMediaElementSource(element);
        $wnd.audioSystemAnalyserNode = context.createScriptProcessor(1024); // FFT_SIZE
        // prepare arrays to store sample data for visualization
       // $wnd.vis_left_channel = new Float32Array(512);
        //$wnd.vis_right_channel = new Float32Array(512);
        $wnd.audioSystemAnalyserNode.onaudioprocess = function (event) {
            //console.log("onaudioprocess");
            try {
                if ($wnd.vis_left_channel === undefined || $wnd.vis_right_channel === undefined) {
                    $wnd.vis_left_channel = event.inputBuffer.getChannelData(0);
                    $wnd.vis_right_channel = event.inputBuffer.getChannelData(1);
                    console.log("onaudioprocess: set references");
                }
            } catch (e) {
                console.log("Exception in onaudioprocess: " + e);
            }
        };

        $wnd.audioSystemSourceNode.connect(context.destination);
        $wnd.audioSystemSourceNode.connect($wnd.audioSystemAnalyserNode);
        $wnd.audioSystemAnalyserNode.connect(context.destination); // need to connect to destination or no audio is processed. BUG?
        console.log("audio setup complete");
    }-*/;


    public void connect() {
        setup(audioContext, mediaElement);
        connected = true;
    }

    public void disconnect() {
        if (connected) {
            doDisconnect();
            connected = false;
        }
    }

    public native void doDisconnect() /*-{
        console.log("disconnecting source");
        $wnd.audioSystemSourceNode.disconnect();
        $wnd.audioSystemAnalyserNode.disconnect();
        // make sure the old analyser node does not collect data anymore
        $wnd.audioSystemAnalyserNode.onaudioprocess = undefined;
        $wnd.vis_left_channel = undefined;
        $wnd.vis_right_channel = undefined;
    }-*/;

    public void startCollectVisData() {
        visDataTimer.scheduleRepeating(1000 / VIS_FPS);
    }

    public void stopCollectVisData() {
        visDataTimer.cancel();
    }

    private void updateVisData() {
        VisArray visArray = new VisArray(FFT_SIZE);
        if (!doCollectVisData(visArray)) {
           return;
        }
        formatVis(FFT.calcFreq(visArray.asArray()));

        for (VisDataHandler handler: handlers) {
            handler.onVisDataUpdate(this.bars);
        }
    }

    private native boolean doCollectVisData(VisArray visArray) /*-{
        if ($wnd.vis_left_channel === undefined || $wnd.vis_right_channel === undefined) {
            // no data available
            console.log("no vis data available")
            return false;
        }
        var left = $wnd.vis_left_channel;
        var right = $wnd.vis_right_channel;
        for (var i = 0; i < left.length; i++) {
            // hand off data and convert to mono
            visArray.@de.pandaserv.music.client.audio.AudioSystem.VisArray::set(ID)(i, (left[i] + right[i]) / 2.0);
        }
        //console.log("handed off " + left.length + " samples");
        return true;
    }-*/;

    private void calculateScale() {
        // calculate logarithmic scale - 1
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
                x = 110 * Math.log10(n * 100);
                //x = 20 * Math.log10(n * 100);
                //x = 50 * Math.log10(n * 200);
                //x = (Math.pow(255, n / 255)) - 1;
                //x = 127 * (Math.log(n) / Math.log(127));
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
