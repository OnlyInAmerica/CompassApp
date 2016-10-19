package com.test.compassapp;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by chavi on 9/5/16.
 */
public class CompassService extends Service implements SensorEventListener {
    public static final String COMPASS_DATA_START = BuildConfig.APPLICATION_ID + "compass_data_start";

    private static final int MAX_NUM_OUT_OF_RANGE = 20;
    private static final int MAX_TIME_MS = 2000;

    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private Handler handler = new Handler();
    private float[] firstReading = new float[3];
    private float[] magnetometerReading = new float[3];
    private long startTime;
    private boolean startCheckingRange;

    private int numOutOfRangeValues = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:

                if (!startCheckingRange) {
                    System.arraycopy(event.values, 0, firstReading,
                            0, magnetometerReading.length);

                    startTime = System.currentTimeMillis();
                    startCheckingRange = true;
                }

                if (timeIsUp()) {
                    System.arraycopy(event.values, 0, magnetometerReading,
                            0, magnetometerReading.length);

                    if (outOfRange(magnetometerReading[0], firstReading[0]) &&
                            outOfRange(magnetometerReading[1], firstReading[1]) &&
                            outOfRange(magnetometerReading[2], firstReading[2])) {
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.startActivity(intent);
                        stopSelf();
                    }

                    startCheckingRange = false;
                }

                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            switch (intent.getAction()) {
                case COMPASS_DATA_START:
                    startCheckingRange = false;
                    sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
                    Log.d("CompassService", "startListener");
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean outOfRange(float currReading, float lastReading) {
        Log.d("CompassService", "outOfRange : " + currReading + " " + lastReading);
        return (Math.abs(currReading - lastReading) > 50);
    }

    private boolean timeIsUp() {
        return System.currentTimeMillis() - startTime >= MAX_TIME_MS;

    }
}
