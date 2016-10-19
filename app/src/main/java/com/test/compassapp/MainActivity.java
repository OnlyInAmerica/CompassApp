package com.test.compassapp;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by chavi on 9/4/16.
 */
public class MainActivity extends Activity implements SensorEventListener {
    private TextView magneticTextView;
    private TextView magneticRawTextView;
    private TextView proximityTextView;
    private TextView lightTextView;

    private SensorManager sensorManager;
    private Sensor magneticSensor;
    private Sensor rawMagneticSensor;
    private Sensor lightSensor;

    private Sensor proximitySensor;
    private float[] magnetometerReading = new float[3];
    private float[] singleArr = new float[1];
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(com.test.compassapp.R.layout.activity_main);
        magneticTextView = (TextView) findViewById(com.test.compassapp.R.id.magnetic_text_view);
        magneticRawTextView = (TextView) findViewById(com.test.compassapp.R.id.magnetic_raw_text_view);
        proximityTextView = (TextView) findViewById(com.test.compassapp.R.id.proximity_text_view);
        lightTextView = (TextView) findViewById(com.test.compassapp.R.id.light_text_view);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rawMagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.d("MainActivity", "send broadcast");
//                sendBroadcast(new Intent(TestBroadcast.ACTION_START));
//            }
//        }, 5000);

//        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        adapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
//            @Override
//            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
//                Log.d("MainActivity", "scanned item " + device.getName());
//                if ("tkr".equals(device.getName())) {
//                    List<UUID> uuids = parseUuids(scanRecord);
//                    Log.d("MainActivity", "UUIDs " + uuids);
//                }
//            }
//        });


    }

    private List<UUID> parseUuids(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }

        return uuids;
    }

    @Override
    protected void onStart() {
        super.onStart();

        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
        sensorManager.registerListener(this, rawMagneticSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

                magneticTextView.setText(String.format(Locale.US, "magnetic data: \nx %s\ny %s\nz %s", magnetometerReading[0],
                        magnetometerReading[1], magnetometerReading[2]));
                break;
            case Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                System.arraycopy(event.values, 0, magnetometerReading,
                        0, magnetometerReading.length);

                magneticRawTextView.setText(String.format(Locale.US, "RAW magnetic data: \nx %s\ny %s\nz %s", magnetometerReading[0],
                        magnetometerReading[1], magnetometerReading[2]));
                break;

            case Sensor.TYPE_PROXIMITY:
                System.arraycopy(event.values, 0, singleArr, 0, singleArr.length);
                proximityTextView.setText(String.format("proximity: %s", singleArr[0]));
                break;

            case Sensor.TYPE_LIGHT:
                System.arraycopy(event.values, 0, singleArr, 0, singleArr.length);
                lightTextView.setText(String.format("light val: %s", singleArr[0]));
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

}
