package com.test.compassapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.test.compassapp.algos.YZThresholdAlgo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import timber.log.Timber;

import static android.os.Environment.DIRECTORY_PICTURES;
import static com.test.compassapp.MagneticMountDetector.NO_DETERMINATION;
import static com.test.compassapp.MagneticMountDetector.OFF_MAGNET;
import static com.test.compassapp.MagneticMountDetector.ON_MAGNET;

/**
 * Created by dbro on 10/21/16.
 */
public class ChartActivity extends AppCompatActivity implements SensorEventListener, ChartView.Callback, AdapterView.OnItemSelectedListener {
    private static final int NUM_MAG_FIELD_VALUES = 3;

    private ChartView chartView;
    private TextView mountStatusView;
    private Spinner algoSpinner;

    private SensorManager sensorManager;
    private Sensor rawMagneticSensor;

    private float[] magnetometerReading = new float[NUM_MAG_FIELD_VALUES];
    private Handler handler = new Handler();

    private MagneticMountDetector mountDetector = new MagneticMountDetector();
    private int lastMountResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chart);
        chartView = (ChartView) findViewById(R.id.chart);
        mountStatusView = (TextView) findViewById(R.id.mount_status);
        //algoSpinner = (Spinner) findViewById(R.id.algo_spinner);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rawMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);

        chartView.setCallback(this);
        chartView.setThreshold(mountDetector.getThreshold());

        // Setup algo chooser. For now, don't allow choosing since all these algos are trash except YZThresholdAlgo
//        ArrayList<AlgoLabel> algos = new ArrayList<>();
//        algos.add(new AlgoLabel("Magnitude", new MagnitudeAlgo(MagnitudeAlgo.MAGNITUDE_TOTAL)));
//        algos.add(new AlgoLabel("Y-Magnitude", new MagnitudeAlgo(MagnitudeAlgo.MAGNITUDE_Y)));
//        algos.add(new AlgoLabel("Y-Mag-Deriv", new DerivativeAlgo()));
//        algos.add(new AlgoLabel("Mag-Memory", new MagnitudeMemoryAlgo()));
//        algos.add(new AlgoLabel("3-Axis", new YZThresholdAlgo()));

//        algoSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, algos));

//        algoSpinner.setOnItemSelectedListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        mountDetector.setAlgo(new YZThresholdAlgo());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveChart();
                break;
            case R.id.action_review:

                Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
            case Sensor.TYPE_MAGNETIC_FIELD:


                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

                int result = mountDetector.processEvent(event);

                if (result == ON_MAGNET) {
                    mountStatusView.setText("ON MAGNET");
                    mountStatusView.setTextColor(Color.GREEN);
                } else if (result == OFF_MAGNET) {
                    mountStatusView.setText("OFF MAGNET");
                    mountStatusView.setTextColor(Color.RED);
                } else if (result == NO_DETERMINATION) {
                    mountStatusView.setText("NO DETERMINATION");
                    mountStatusView.setTextColor(Color.BLACK);
                }

                chartView.appendValue("X", magnetometerReading[0]);
                chartView.appendValue("Y", magnetometerReading[1]);
                chartView.appendValue("Z", magnetometerReading[2]);

                if (result != lastMountResult) {
                    chartView.addTimeMarker(MagneticMountDetector.describeState(result));
                } else {
                    chartView.addTimeMarker("");
                }

                lastMountResult = result;
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onThresholdSet(float threshold) {
        mountDetector.setAlgoThreshold(threshold);
    }

    /* Algo spinner Listener */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        MagneticMountDetector.SensorAlgo selectedAlgo = ((AlgoLabel) algoSpinner.getAdapter().getItem(position)).algo;
        mountDetector.setAlgo(selectedAlgo);
        chartView.clear();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // No-op
    }

    public void saveChart() {

        try {
            File chartImage = new File(getApplicationContext().getExternalFilesDir(DIRECTORY_PICTURES), "chart_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(chartImage);
            chartView.saveToFile(fos);
            fos.close();
            Toast.makeText(getApplicationContext(), "Saved Chart", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Timber.e(e, "Failed to save chart file");
        }
    }

    /* End Algo spinner Listener */

    static class AlgoLabel {
        final MagneticMountDetector.SensorAlgo algo;
        final String label;

        public AlgoLabel(@NonNull String label, @NonNull MagneticMountDetector.SensorAlgo algo) {
            this.label = label;
            this.algo = algo;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
