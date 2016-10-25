package com.test.compassapp;

/**
 * Created by dbro on 10/21/16.
 */

public class SensorEventUtil {

    public static double getAbsYValue(float[] values) {
        return Math.abs(values[1]);
    }

    public static double getMagnitude(float[] values) {
        double x2 = Math.pow(values[0], 2);
        double y2 = Math.pow(values[1], 2);
        double z2 = Math.pow(values[2], 2);

        double mag = Math.sqrt(x2 + y2 + z2);

        return mag;
    }
}
