package com.rutgerssustainability.android.rutgerssustainability.utils;

import android.media.MediaRecorder;

/**
 * Created by shreyashirday on 3/9/17.
 */
public class NoiseHelper {

    private final double EMA_FILTER = 0.6;
    private double mEma = 0.0;

    public double getDecibels(final MediaRecorder mediaRecorder, final double refAmp) {
        return  20 * Math.log10(getAmpltiudeEMA(mediaRecorder) / refAmp);
    }

    private double getAmplitude(final MediaRecorder mediaRecorder) {
        if (mediaRecorder != null) {
            return mediaRecorder.getMaxAmplitude();
        }
        return 0;
    }

    private double getAmpltiudeEMA(final MediaRecorder mediaRecorder) {
        final double amp = getAmplitude(mediaRecorder);
        mEma = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEma;
        return mEma;
    }

}
