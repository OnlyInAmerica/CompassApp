package com.test.compassapp.algos;

import android.hardware.SensorEvent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.test.compassapp.MagneticMountDetector;
import com.test.compassapp.SensorEventUtil;

/**
 * Use the simple magnitude plus memory of the SensorEvent values to determine mount status.
 * Created by dbro on 10/21/16.
 */
public class MagnitudeMemoryAlgo implements MagneticMountDetector.SensorAlgo {

    /**
     * Description:
     * 3 Average periods trending by 20% = magnet transition
     */

    /**
     * How many readings to hold in memory
     */
    private static final int MEMORY_SIZE = 100;

    private double[] valuesMemory = new double[MEMORY_SIZE];
    private long[] timestampMemory = new long[MEMORY_SIZE];

    private int memoryIdx = -1;
    private boolean didFillMemory = false;

    private double[] averagedValuesMemory = new double[5];
    private int averagedMemoryIdx = 0;

    private int confidence;
    private double lastReducedValue;
    private double threshold = .31;

    // Counter
    private long sampleCount;

    // Baselines
    private double baselineDerivative;
    private double baselineMagnitude;

    private int trendingPeriodCount = 0;

    public MagnitudeMemoryAlgo() {
    }

    @Override
    public double processEvent(@NonNull SensorEvent event) {

        double value = SensorEventUtil.getAbsYValue(event.values);

        memoryIdx = (memoryIdx + 1) % MEMORY_SIZE;

        valuesMemory[memoryIdx] = value;
        timestampMemory[memoryIdx] = System.currentTimeMillis();

        if (!didFillMemory && !(didFillMemory = (memoryIdx == MEMORY_SIZE - 1))) {
            confidence = ZERO_CONFIDENCE;
            return confidence;
        }

        if (sampleCount++ % (MEMORY_SIZE) == 0) {
            lastReducedValue = (calculateAverage() - baselineMagnitude) / 2f;

            baselineDerivative = (baselineDerivative + calculateDerivative()) / 2f;
            baselineMagnitude = (baselineMagnitude + calculateAverage()) / 2f;

            averagedValuesMemory[averagedMemoryIdx] = baselineMagnitude;
            //evaluateAverageValueTrend();
            averagedMemoryIdx = (averagedMemoryIdx + 1) % averagedValuesMemory.length;
            //Log.d("Algo", "Set baseline derivative " + baselineDerivative + " average " + baselineMagnitude);
        }

        return confidence;
    }

    private void evaluateAverageValueTrend() {

        boolean isTransition = true;
        final float averageTrendThreshold = .20f;
        int count = 0;
        double[] ratios = new double[averagedValuesMemory.length - 1];
        double totalRatio = 0;
        double lastValue = averagedValuesMemory[averagedMemoryIdx];
        for (int idx = (averagedMemoryIdx + 1) % averagedValuesMemory.length; count < averagedValuesMemory.length - 1; idx = (idx + 1) % averagedValuesMemory.length) {
            double curValue = averagedValuesMemory[idx];

            // Has each value monotonically increased by 15%
            double ratio = (curValue / lastValue);
            totalRatio += ratio;
            ratios[count] = ratio;
            if (isOnMagnet() && ratio > (1 - averageTrendThreshold)) {
                isTransition = false;
            } else if (!isOnMagnet() && ratio < (1 + averageTrendThreshold)) {
                isTransition = false;
            }
            count++;
        }
        if (isTransition) {
            Log.d("Algo", String.format("State transition occurred: %s", isOnMagnet() ? " off magnet" : " on magnet. average ratios were " + ratios[0] + ", " + ratios[1] + ", " + ratios[2]));
        } else {
            Log.d("Algo", "No transition. average ratios were " + ratios[0] + ", " + ratios[1] + ", " + ratios[2]);
        }
        if (isTransition && isOnMagnet()) {
            confidence = FULL_CONFIDENCE_OFF_MAGNET;
        } else if (isTransition) {
            confidence = FULL_CONFIDENCE_ON_MAGNET;
        }
        //lastReducedValue = totalRatio / ratios.length;
    }

    private boolean isOnMagnet() {
        return confidence > ZERO_CONFIDENCE;
    }

    private double calculateDerivative() {
        double first = valuesMemory[(memoryIdx+1) % MEMORY_SIZE];
        double last = valuesMemory[memoryIdx];

        long firstTime = timestampMemory[(memoryIdx+1) % MEMORY_SIZE];
        long lastTime = timestampMemory[memoryIdx];

        return (last - first) / (lastTime - firstTime);
    }

    private double calculateAverage() {
        double sum = 0;

        for (double value : valuesMemory) {
            sum += value;
        }
        return sum / MEMORY_SIZE;
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
