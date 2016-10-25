package com.test.compassapp.algos;

import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.test.compassapp.MagneticMountDetector;
import com.test.compassapp.SensorEventUtil;

/**
 * Use the derivative of the SensorEvent values to determine mount status.
 * Created by dbro on 10/21/16.
 */
public class DerivativeAlgo implements MagneticMountDetector.SensorAlgo {

    /**
     * How many readings to hold in memory
     */
    private static final int MEMORY_SIZE = 5;

    private double[] valuesMemory = new double[MEMORY_SIZE];
    private long[] timestampMemory = new long[MEMORY_SIZE];
    private int memoryIdx = -1;
    private boolean didFillMemory = false;

    private int confidence;
    private double lastReducedValue;
    private double threshold = .31;

    public DerivativeAlgo() {
    }

    @Override
    public double processEvent(@NonNull SensorEvent event) {

        double value = SensorEventUtil.getAbsYValue(event.values);

        memoryIdx = (memoryIdx + 1) % MEMORY_SIZE;

        valuesMemory[memoryIdx] = value;
        timestampMemory[memoryIdx] = System.currentTimeMillis();

        if (!didFillMemory && !(didFillMemory = (memoryIdx == MEMORY_SIZE - 1))) {
            confidence = ZERO_CONFIDENCE;
        }

        lastReducedValue = calculateDerivative();

        if (lastReducedValue >= threshold && confidence <= ZERO_CONFIDENCE) {
            confidence = FULL_CONFIDENCE_ON_MAGNET;
        } else if (lastReducedValue <= -threshold && confidence >= ZERO_CONFIDENCE ) {
            confidence = FULL_CONFIDENCE_OFF_MAGNET;
        }

        return confidence;
    }

    private double calculateDerivative() {
        double first = valuesMemory[(memoryIdx+1) % MEMORY_SIZE];
        double last = valuesMemory[memoryIdx];

        long firstTime = timestampMemory[(memoryIdx+1) % MEMORY_SIZE];
        long lastTime = timestampMemory[memoryIdx];

        return (last - first) / (lastTime - firstTime);
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
