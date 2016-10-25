package com.test.compassapp;

import android.hardware.SensorEvent;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.test.compassapp.algos.MagnitudeAlgo;

/**
 * Created by dbro on 10/21/16.
 */
public class MagneticMountDetector {

    public static final float DEFAULT_CONFIDENCE_THRESHOLD = .5f;

    public static final int OFF_MAGNET = 0;
    public static final int NO_DETERMINATION = 1;
    public static final int ON_MAGNET = 2;

    @IntDef({OFF_MAGNET, NO_DETERMINATION, ON_MAGNET})
    public @interface MountStatus {
    }

    private SensorAlgo algo;
    private float threshold;
    private double confidence;

    public MagneticMountDetector() {
        this(DEFAULT_CONFIDENCE_THRESHOLD);
    }

    public MagneticMountDetector(float threshold) {
        this.threshold = threshold;
        this.algo = new MagnitudeAlgo(MagnitudeAlgo.MAGNITUDE_Y);
    }

    public void setAlgoThreshold(float threshold) {
        this.algo.setThresholdValue(threshold);
    }

    public float getThreshold() {
        return threshold;
    }

    public void setAlgo(@NonNull SensorAlgo algo) {
        this.algo = algo;
    }

    public SensorAlgo getAlgo() {
        return this.algo;
    }

    @MountStatus
    public int processEvent(@NonNull SensorEvent magneticSensorEvent) {
        confidence = algo.processEvent(magneticSensorEvent);

        if (confidence > threshold) {
            return ON_MAGNET;
        } else if (confidence < -threshold) {
            return OFF_MAGNET;
        } else {
            return NO_DETERMINATION;
        }
    }

    /**
     * @return the current confidence value between [-1, 1], with -1 being the strongest off-magnet
     * confidence, and 1 being the strongest on-magnet confidence.
     * This will return 0 until {@link #processEvent(SensorEvent)} is called at least once.
     */
    public double getConfidence() {
        return confidence;
    }

    public interface SensorAlgo {

        int FULL_CONFIDENCE_ON_MAGNET = 1;
        int FULL_CONFIDENCE_OFF_MAGNET = -1;
        int ZERO_CONFIDENCE = 0;

        /**
         * Process the given event and return a simplified value representing the current confidence
         * of being off magnet or on magnet
         *
         * @param event the latest sensor event
         * @return a value between [FULL_CONFIDENCE_OFF_MAGNET, FULL_CONFIDENCE_ON_MAGNET], with
         * ZERO_CONFIDENCE being the midpoint and indicating no confidence whatsoever
         */
        double processEvent(@NonNull SensorEvent event);

        /**
         * @return the last reduced value of this algorithm. This can return a graph-friendly
         * value representing the internal state of the algorithm, or 0 if unimplemented.
         * This should be the same unit as used by {@link #setThresholdValue(double)}
         */
        double getLastReducedValue();

        /**
         * Set a threshold to the algorithm.
         * Should be in the same unit as used by {@link #getLastReducedValue()}
         */
        void setThresholdValue(double threshold);
    }

    public static String describeState(@MountStatus int status) {
        switch (status) {
            case OFF_MAGNET:
                return "Off";
            case ON_MAGNET:
                return "On";
            case NO_DETERMINATION:
                return "...";
            default:
                return "?";
        }
    }
}
