package com.test.compassapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by chavi on 9/4/16.
 */
public class MainActivity extends Activity implements SensorEventListener {
    private static final int NUM_MAG_FIELD_VALUES = 3;
    private static final int MAGNET_THRESHOLD = 100;

    private TextView magneticRawTextView;
    private TextView magnetStateTextView;

    private SensorManager sensorManager;
    private Sensor rawMagneticSensor;

    private float[] magnetometerReading = new float[NUM_MAG_FIELD_VALUES];
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.test.compassapp.R.layout.activity_main);
        magneticRawTextView = (TextView) findViewById(R.id.magnetic_raw_text_view);
        magnetStateTextView = (TextView) findViewById(R.id.magnet_state_text);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rawMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorManager.registerListener(this, rawMagneticSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

                if (checkAboveThreshold(magnetometerReading)) {
                    magnetStateTextView.setText("ON MAGNET");
                    magnetStateTextView.setTextColor(Color.GREEN);
                } else {
                    magnetStateTextView.setText("OFF MAGNET");
                    magnetStateTextView.setTextColor(Color.RED);
                }

                magneticRawTextView.setText(String.format(Locale.US, "RAW magnetic data: \nx %s\ny %s\nz %s", magnetometerReading[0],
                        magnetometerReading[1], magnetometerReading[2]));
                break;

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
