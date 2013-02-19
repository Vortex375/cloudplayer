package de.pandaserv.music.client.audio;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.user.client.Timer;
import com.google.web.bindery.event.shared.HandlerRegistration;
import de.pandaserv.music.client.math.FFT;
import de.pandaserv.music.shared.NotSupportedException;

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
        var analyserNode = context.createScriptProcessor(512);
        analyserNode.onaudioprocess = function (event) {
            $wnd.vis_left_channel = event.inputBuffer.getChannelData(0);
            $wnd.vis_right_channel = event.inputBuffer.getChannelData(1);
        };

        sourceNode.connect(context.destination);
        sourceNode.connect(analyserNode);
        analyserNode.connect(context.destination); // need to connect to destination or no audio is processed. BUG?
        console.log("audio setup complete");
        // pass the analyserNode to java code
        //this.@de.pandaserv.music.client.audio.AudioSystem::setAnalyserNode(Lcom/google/gwt/core/client/JavaScriptObject;)(analyserNode);
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
        return true;
        //console.log("collected vis data")
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
                x = 100 * Math.log10(n * 100);
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
