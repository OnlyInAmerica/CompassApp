package com.test.compassapp.algos;

import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.test.compassapp.MagneticMountDetector;

/**
 * The original algorithm currently in use in Pearl
 * Created by dbro on 10/21/16.
 */
public class OGAlgo implements MagneticMountDetector.SensorAlgo {

    private static final int MAGNET_THRESHOLD = 100;
    private static final int NUM_MAG_FIELD_VALUES = 3;

    public OGAlgo() {
    }

    @Override
    public double processEvent(@NonNull SensorEvent event) {

        if (checkAboveThreshold(event.values)) {
            return FULL_CONFIDENCE_ON_MAGNET;
        } else {
            return FULL_CONFIDENCE_OFF_MAGNET;
        }
    }

    @Override
    public double getLastReducedValue() {
        // unused
        return 0;
    }

    @Override
    public void setThresholdValue(double threshold) {
        // unused
    }

    private boolean checkAboveThreshold(float[] sumReading) {
        boolean above = true;

        for (int i = 0; i < NUM_MAG_FIELD_VALUES; i++) {
            above = above && aboveThreshold(sumReading[i]);
        }

        return above;
    }

    private boolean aboveThreshold(float currReading) {
        return Math.abs(currReading) >= MAGNET_THRESHOLD;
    }
}
