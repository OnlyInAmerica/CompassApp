package com.test.compassapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by dbro on 10/21/16.
 */
public class ChartView extends View {

    private static final int NUM_Y_AXIS_LABELS = 6;

    private static final float SCALING_FACTOR = 2;

    private static final float PX_PER_POINT = 1;

    private static int[] seriesColors;

    private static final int FONT_SIZE_PX = 25;

    private Callback callback;

    private Paint axisLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint seriesLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint tooltipLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint thresholdPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint seriesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap tempBitmap;
    private Canvas tempCanvas;

    private float width;
    private float height;
    private int valuesSize;
    private float thresholdValue = 1;

    private float minValue;
    private float maxValue;

    private MarkerSeries markerSeries;
    private HashMap<String, Series> seriesMap = new HashMap<>();
    private Series activeSeries;

    private boolean adjustingThreshold = false;

    private android.graphics.Matrix scalingMatrix = new android.graphics.Matrix();

    public ChartView(Context context) {
        super(context);
        init();
    }

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ChartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setThreshold(float thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private void init() {

        // For printing y-axis labels
        axisLabelPaint.setStyle(Paint.Style.FILL);
        axisLabelPaint.setTextSize(FONT_SIZE_PX);
        axisLabelPaint.setTextAlign(Paint.Align.RIGHT);
        axisLabelPaint.setColor(getResources().getColor(R.color.chartAxisLabels));

        // For printing series labels
        seriesLabelPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        seriesLabelPaint.setStrokeWidth(2);
        seriesLabelPaint.setTextSize(FONT_SIZE_PX);
        seriesLabelPaint.setTextAlign(Paint.Align.LEFT);
        seriesLabelPaint.setColor(getResources().getColor(R.color.chartAxisLabels));

        // For printing x-axis markers
        markerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        markerPaint.setStrokeWidth(2);
        markerPaint.setTextSize(FONT_SIZE_PX);
        markerPaint.setColor(getResources().getColor(R.color.chartMarkers));

        // For printing threshold value during adjustment
        int thresholdColor = getResources().getColor(R.color.chartThreshold);
        tooltipLabelPaint.setStyle(Paint.Style.FILL);
        tooltipLabelPaint.setTextSize(FONT_SIZE_PX);
        tooltipLabelPaint.setColor(thresholdColor);

        // For drawing the horizontal threshold level
        thresholdPaint.setStyle(Paint.Style.STROKE);
        thresholdPaint.setStrokeWidth(2);
        thresholdPaint.setColor(thresholdColor);

        // For drawing value points
        seriesPaint.setStyle(Paint.Style.FILL);
        seriesPaint.setColor(Color.BLACK);
        seriesPaint.setStrokeWidth(3);

        scalingMatrix.preScale(2, 2);

        seriesColors = new int[] {
                getResources().getColor(R.color.xAxis),
                getResources().getColor(R.color.yAxis),
                getResources().getColor(R.color.zAxis),
                getResources().getColor(R.color.mountState)
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        tempBitmap = Bitmap.createBitmap((int) (getWidth() / SCALING_FACTOR), (int) (getHeight() / SCALING_FACTOR), Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);

        width = w / SCALING_FACTOR;
        height = h / SCALING_FACTOR;
        valuesSize = (int) (width / PX_PER_POINT);

        markerSeries = new MarkerSeries("Markers");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        tempCanvas.drawColor(0x0dffffff, PorterDuff.Mode.CLEAR);
        tempCanvas.drawColor(0xff000000, PorterDuff.Mode.ADD);

        // Plot values
        int seriesIdx = 0;
        float newMin = 0;
        float newMax = 0;
        for (Series series : seriesMap.values()) {
            int seriesColor = seriesColors[seriesIdx % seriesColors.length];
            seriesPaint.setColor(seriesColor);

            float prevValue = 0;

            for (int x = 0; x < series.size; x++) {

                float value = series.values[(series.getFirstDrawIdx() + x) % series.values.length];
                float xAxisVal = PX_PER_POINT * x;
                newMin = Math.min(value, newMin);
                newMax = Math.max(value, newMax);

                float scaledValue = (Math.abs(maxValue - value) / Math.abs(maxValue - minValue)) * height;
                if (x > 0) {
                    tempCanvas.drawLine(PX_PER_POINT * (x-1), prevValue, xAxisVal, scaledValue, seriesPaint);
                } else {
                    // Draw series label
                    float valAvg = average(series.values, series.getFirstDrawIdx(), 10);
                    float seriesLabelY = (Math.abs(maxValue - (valAvg)) / Math.abs(maxValue - minValue)) * height;
                    tempCanvas.drawText(series.name, PX_PER_POINT, seriesLabelY, seriesLabelPaint);
                }

                prevValue = scaledValue;
                if (xAxisVal >= width) break;
            }

            seriesIdx++;
        }

        // Draw active series pieces: Threshold line and y-axis labels
        if (activeSeries != null) {
            // Draw threshold line
            float thresholdYPos = (Math.abs(maxValue - thresholdValue) / Math.abs(maxValue - minValue)) * height;
            tempCanvas.drawLine(0, thresholdYPos, width, thresholdYPos, thresholdPaint);
            if (adjustingThreshold) {
                tempCanvas.drawText(String.format(Locale.US, "%.2f", thresholdValue), 100, thresholdYPos - FONT_SIZE_PX, tooltipLabelPaint);
            }

            // Draw y-axis labels
            float increment = height / NUM_Y_AXIS_LABELS;
            for (int y = 0; y <= NUM_Y_AXIS_LABELS; y++) {
                float scaledVal = y * increment;
                float val = ((scaledVal / height) * Math.abs(maxValue - minValue)) + minValue;
                float labelHeight = Math.max(2 * FONT_SIZE_PX, Math.min(height - FONT_SIZE_PX, height - scaledVal));
                tempCanvas.drawText(String.valueOf((int) val), width, labelHeight, axisLabelPaint);
            }
        }

        // Draw time markers
        int markerXPos = 0;
        for (int x = 0; x < markerSeries.size; x++) {
            String marker = markerSeries.values[(markerSeries.getFirstDrawIdx() + x) % markerSeries.values.length];

            if (!TextUtils.isEmpty(marker)) {
                tempCanvas.drawText(marker, x * PX_PER_POINT, height / 2, markerPaint);
            }
            if (markerXPos >= width) break;
        }

        canvas.drawBitmap(tempBitmap, scalingMatrix, null);

        maxValue = newMax;
        minValue = newMin;
    }

    public void addTimeMarker(@NonNull String markerName) {
        // Ignore values until we have performed size-dependent initialization
        if (valuesSize == 0) return;

        markerSeries.addMarker(markerName);
    }

    public void appendValue(@NonNull String seriesName, double value) {

        // Ignore values until we have performed size-dependent initialization
        if (valuesSize == 0) return;

        Series series = seriesMap.get(seriesName);
        if (series == null) {
            series = new Series(seriesName);
            seriesMap.put(seriesName, series);

            if (activeSeries == null) {
                activeSeries = series;
            }
        }

        series.addValue((float) value);
        postInvalidate();
    }

    public void setActiveSeries(@NonNull String seriesName) {
        this.activeSeries = seriesMap.get(seriesName);
    }

    public void clear() {
        for (Series series : seriesMap.values()) {
            series.size = 0;
        }
    }

    public void saveToFile(@NonNull OutputStream outputStream) {
        if (tempBitmap == null) return;

        boolean result = tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        Timber.d("Saved chart with result %b", result);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {

        if (activeSeries == null) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                thresholdPaint.setAlpha(100);
                adjustingThreshold = true;
            case MotionEvent.ACTION_MOVE:
                thresholdValue = valueFromYPos(event.getY() / SCALING_FACTOR); //((height - (thresholdTouchY)) / height) * maxValue;
                break;

            case MotionEvent.ACTION_UP:
                thresholdPaint.setAlpha(255);
                adjustingThreshold = false;
                if (callback != null) {
                    callback.onThresholdSet(thresholdValue);
                }
        }
        return true;
    }

    private static float average(float[] values, int offset, int count) {
        float sum = 0;
        for (int x = 0; x < count; x++) {
            sum += values[(offset + x) % values.length];
        }
        return sum / count;
    }

    private float valueFromYPos(float y) {
        return (((height - (y)) / height) * Math.abs(maxValue - minValue)) + minValue;
    }

    public interface Callback {
        void onThresholdSet(float threshold);
    }

    public class Series {
        private float[] values;
        private int valueIdx = 0;   // Next empty index
        private int size = 0;       // Count of filled values
        String name;

        public Series(@NonNull String name) {
            this.values = new float[valuesSize];
            this.name = name;
        }

        public void addValue(float value) {
            maxValue = Math.max(maxValue, value);
            minValue = Math.min(minValue, value);
            values[valueIdx] = value;
            size = Math.min(size + 1, values.length);
            valueIdx = (valueIdx + 1) % values.length;
        }

        public int getFirstDrawIdx() {
            if (size < values.length) {
                return 0;
            } else {
                return valueIdx;
            }
        }
    }

    public class MarkerSeries {
        private String[] values;
        private int valueIdx = 0;   // Next empty index
        private int size = 0;       // Could of filled values
        String name;

        public MarkerSeries(@NonNull String name) {
            this.values = new String[valuesSize];
            this.name = name;
        }

        public void addMarker(String marker) {
            values[valueIdx] = marker;
            size = Math.min(size + 1, values.length);
            valueIdx = (valueIdx + 1) % values.length;
        }

        public int getFirstDrawIdx() {
            if (size < values.length) {
                return 0;
            } else {
                return valueIdx;
            }
        }
    }
}
