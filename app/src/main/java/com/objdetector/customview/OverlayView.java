package com.objdetector.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.LinkedList;
import java.util.List;


public class OverlayView extends View {
    private static int INPUT_SIZE = 300;

    private final Paint paint;
    private final List<DrawCallback> callbacks = new LinkedList();
    private String result;
    private List<Integer> colors;

    public OverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                20, getResources().getDisplayMetrics()));
    }

    public void addCallback(final DrawCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public synchronized void onDraw(final Canvas canvas) {
        for (final DrawCallback callback : callbacks) {
            callback.drawCallback(canvas);
        }

        if (result != null) {
            String title = result;
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.0f);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawText(title, 50, 100, paint);
        }
    }

    public void setResult(final String result) {
        this.result = result;
        postInvalidate();
    }

    public interface DrawCallback {
        void drawCallback(final Canvas canvas);
    }

}