package com.test.compassapp.algos;

import android.hardware.SensorEvent;
import android.support.annotation.NonNull;

import com.test.compassapp.MagneticMountDetector;

import timber.log.Timber;

/**
 * Detect equal and opposing effects on the Y and Z axes of the SensorEvent values to determine mount status.
 * Created by dbro on 10/21/16.
 */
public class YZThresholdAlgo implements MagneticMountDetector.SensorAlgo {

    /**
     * How many samples to collect before considering baseline values established
     */
    private static final int BASELINE_SAMPLE_NUM = 20;

    /**
     * What multiple of the baseline value should be considered a mount event.
     */
    private static final int BASELINE_MOUNT_MULTIPLIER = 2;

    /**
     * How many samples returning an identical confidence are required in order to report
     * that confidence
     */
    private static final int HYSTERESIS_SAMPLE_PERIOD = 10;

    private boolean gotBaseline = false;
    private float[][] memory = new float[BASELINE_SAMPLE_NUM][3];
    private int baselineIdx = 0;
    private float[] baseline = new float[3];
    private int hysteresisCounter = 0;

    private double hysteresisConfidence;
    private double confidence = ZERO_CONFIDENCE;

    public YZThresholdAlgo() {
    }

    @Override
    public double processEvent(@NonNull SensorEvent event) {

        float y = event.values[1];
        float z = event.values[2];

        double newConfidence = 0;

        if (!gotBaseline) {
            memory[baselineIdx][0] = event.values[0];
            memory[baselineIdx][1] = event.values[1];
            memory[baselineIdx][2] = event.values[2];

            baselineIdx = (baselineIdx + 1) % memory.length;

            if (baselineIdx == 0) {
                gotBaseline = true;
                calculateBaselines();
            }
            newConfidence = ZERO_CONFIDENCE;
            return newConfidence;
        }

        if (y > (baseline[1] * BASELINE_MOUNT_MULTIPLIER) && z < (baseline[2] * BASELINE_MOUNT_MULTIPLIER)) {
            newConfidence = FULL_CONFIDENCE_ON_MAGNET;
        } else {
            newConfidence = FULL_CONFIDENCE_OFF_MAGNET;
        }

        if (confidence != newConfidence) {
            if (newConfidence != hysteresisConfidence) {
                // During hysteresis period, the value changed. Reset counter
                hysteresisConfidence = newConfidence;
                hysteresisCounter = 0;
            } else if (checkHysteresisPeriod()) {
                confidence = newConfidence;
            }
        }
        return confidence;
    }

    private boolean checkHysteresisPeriod() {
        return ++hysteresisCounter > HYSTERESIS_SAMPLE_PERIOD;
    }

    private void calculateBaselines() {
        float sumX = 0, sumY = 0, sumZ = 0;

        for (float[] values : memory) {
            sumX += values[0];
            sumY += values[1];
            sumZ += values[2];
        }

        baseline[0] = sumX / memory.length;
        baseline[1] = sumY / memory.length;
        baseline[2] = sumZ / memory.length;
        Timber.d("Baselines are x %f, y %f, z %f", baseline[0], baseline[1], baseline[2]);
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
}
