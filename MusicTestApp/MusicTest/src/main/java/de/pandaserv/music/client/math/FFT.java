package de.pandaserv.music.client.math;

/**
 * This code is heavily based on fft.c from Audacious Media Player (http://audacious-media-player.org/)
 *
 * The following is the copyright notice from "fft.c" where the code was taken from:
 *
 * fft.c
 * Copyright 2011 John Lindgren
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
public class FFT {
    public static final int FFT_SIZE = 512;                         /* size of the DFT (fixed for now) */
    public static final int LOG = 9;                                /* log(FFT_SIZE) (base 2) */

    private static double[] hamming = new double[FFT_SIZE];         /* hamming window, scaled to sum to 1 */
    private static int[] reversed = new int[FFT_SIZE];              /* bit-reversal table */
    private static Complex[] roots = new Complex[FFT_SIZE / 2];     /* N-th roots of unity */

    static {
        // generate tables
        for (int n = 0; n < FFT_SIZE; n++) {
            hamming[n] = 1 - 0.85 * Math.cos (2 * Math.PI * n / FFT_SIZE);
        }
        for (int n = 0; n < FFT_SIZE; n++) {
            reversed[n] = bitReverse(n);
        }
        for (int n = 0; n < FFT_SIZE / 2; n ++) {
            //roots[n] = cexpf (2 * M_PI * I * n / N);
            roots[n] = new Complex(0, 2 * Math.PI * n / FFT_SIZE).exp();
        }
    }

    /* Reverse the order of the lowest LOG bits in an integer. */
    private static int bitReverse(int x) {
        int y = 0;

        for (int n = LOG; n > 0;n--)
        {
            y = (y << 1) | (x & 1);
            x >>= 1;
        }

        return y;
    }

    /* Perform the DFT using the Cooley-Tukey algorithm.  At each step s, where
     * s=1..log N (base 2), there are N/(2^s) groups of intertwined butterfly
     * operations.  Each group contains (2^s)/2 butterflies, and each butterfly has
     * a span of (2^s)/2.  The twiddle factors are nth roots of unity where n = 2^s. */
    private static void doFFT(Complex[] a) {
        int half = 1;       /* (2^s)/2 */
        int inv = FFT_SIZE / 2;    /* N/(2^s) */

        /* loop through steps */
        while (inv > 0)
        {
            /* loop through groups */
            for (int g = 0; g < FFT_SIZE; g += half << 1)
            {
                /* loop through butterflies */
                for (int b = 0, r = 0; b < half; b ++, r += inv)
                {
                    Complex even = a[g + b].copy();
                    Complex odd = roots[r].copy().mul(a[g + half + b]);
                    a[g + b].add(odd);
                    a[g + half + b] = even.sub(odd);
                }
            }

            half <<= 1;
            inv >>= 1;
        }
    }

    /* Input is N=512 PCM samples.
     * Output is intensity of frequencies from 1 to N/2=256. */
    public static double[] calcFreq(double data[]) {
        //JSUtil.log("calcFreq()");
        double[] freq = new double[FFT_SIZE / 2];

        /* input is filtered by a Hamming window */
        /* input values are in bit-reversed order */
        Complex[] a = new Complex[FFT_SIZE];
        for (int n = 0; n < FFT_SIZE; n ++) {
            a[reversed[n]] = new Complex(data[n] * hamming[n], 0);
        }

        //JSUtil.log("pre-FFT: " + Arrays.toString(a));
        doFFT(a);
        //JSUtil.log("post-FFT: " + Arrays.toString(a));

        /* output values are divided by N */
        /* frequencies from 1 to N/2-1 are doubled */
        for (int n = 0; n < FFT_SIZE / 2 - 1; n ++)
            freq[n] = 2 * a[1 + n].abs() / FFT_SIZE;

        /* frequency N/2 is not doubled */
        freq[FFT_SIZE / 2 - 1] = a[FFT_SIZE / 2].abs() / FFT_SIZE;

        //JSUtil.log("result: " + Arrays.toString(freq));
        return freq;
    }
}
