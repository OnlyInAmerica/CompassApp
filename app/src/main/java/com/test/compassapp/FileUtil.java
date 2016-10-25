package com.test.compassapp;

import android.support.annotation.NonNull;

import timber.log.Timber;

/**
 * Created by dbro on 10/25/16.
 */

public class FileUtil {

    public static String createChartCaptureFilenameForResult(@MagneticMountDetector.MountStatus int result) {
        return "chart_" + result + "_" + System.currentTimeMillis() + ".png";
    }

    @MagneticMountDetector.MountStatus
    public static int parseResultFromChartCaptureFilename(@NonNull String chartCaptureFilename) {
        try {
            //noinspection WrongConstant
            return Integer.parseInt(chartCaptureFilename.split("_")[1]);
        } catch (NumberFormatException e) {
            Timber.e(e, "Failed to parse result from chart capture filename. Interpreting as 'No Determination'");
            return MagneticMountDetector.NO_DETERMINATION;
        }
    }
}
