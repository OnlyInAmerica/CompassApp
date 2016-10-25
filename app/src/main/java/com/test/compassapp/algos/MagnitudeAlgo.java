package com.test.compassapp.algos;

import android.hardware.SensorEvent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.test.compassapp.MagneticMountDetector;
import com.test.compassapp.SensorEventUtil;

/**
 * Use the simple magnitudes of the SensorEvent values to determine mount status.
 * Created by dbro on 10/21/16.
 */
public class MagnitudeAlgo implements MagneticMountDetector.SensorAlgo {

    public static final int MAGNITUDE_TOTAL = 0;
    public static final int MAGNITUDE_Y = 1;

    @IntDef({MAGNITUDE_TOTAL, MAGNITUDE_Y})
    public @interface MagnitudeType {
    }

    @MagnitudeType
    private int type;
    private double lastReducedValue;
    private double threshold = 50;

    public MagnitudeAlgo(@MagnitudeType int type) {
        this.type = type;
    }

    @Override
    public double processEvent(@NonNull SensorEvent event) {

        switch (type) {
            case MAGNITUDE_TOTAL:
                lastReducedValue = SensorEventUtil.getMagnitude(event.values);
                break;

            case MAGNITUDE_Y:
                lastReducedValue = SensorEventUtil.getAbsYValue(event.values);
                break;
        }

        if (lastReducedValue >= threshold) {
            return FULL_CONFIDENCE_ON_MAGNET;
        } else {
            return FULL_CONFIDENCE_OFF_MAGNET;
        }
    }

    @Override
    public double getLastReducedValue() {
        return lastReducedValue;
    }

    @Override
    public void setThresholdValue(double threshold) {
        this.threshold = threshold;
    }

}
