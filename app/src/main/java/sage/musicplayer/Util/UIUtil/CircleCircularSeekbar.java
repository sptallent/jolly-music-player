package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import sage.musicplayer.R;


public class CircleCircularSeekbar extends androidx.appcompat.widget.AppCompatSeekBar {

    private int circleColor = Color.RED;
    private int progressColor = Color.GREEN;
    private int thumbColor = Color.BLACK;
    private int circleStrokeWidth = 10;
    private int progressStrokeWidth = 15;

    private Paint circlePaint;
    private Paint progressPaint;
    private Paint thumbPaint;

    private int centerX;
    private int centerY;
    private float radius;

    public CircleCircularSeekbar(Context context) {
        super(context);
        init();
    }

    public CircleCircularSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleCircularSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(circleStrokeWidth);
        circlePaint.setAntiAlias(true);

        progressPaint = new Paint();
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(progressStrokeWidth);
        progressPaint.setAntiAlias(true);

        thumbPaint = new Paint();
        thumbPaint.setColor(thumbColor);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setAntiAlias(true);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
        radius = Math.min(centerX, centerY) - circleStrokeWidth / 2;

        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        int progressAngle = (int) (getProgress() / (float) getMax() * 360);
        RectF progressArc = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(progressArc, -90, progressAngle, false, progressPaint);

        float thumbX = (float) (centerX + radius * Math.cos(Math.toRadians(progressAngle + 90)));
        float thumbY = (float) (centerY + radius * Math.sin(Math.toRadians(progressAngle + 90)));
        canvas.drawCircle(thumbX, thumbY, circleStrokeWidth, thumbPaint);

        super.onDraw(canvas);
    }

    public interface OnCircleCircularSeekBarChangeListener {
        void onProgressChanged(CircleCircularSeekbar seekBar, int progress, boolean fromUser);
        void onStartTrackingTouch(CircleCircularSeekbar seekBar);
        void onStopTrackingTouch(CircleCircularSeekbar seekBar);
    }

    private OnCircleCircularSeekBarChangeListener listener;

    public void setOnCircleCircularSeekBarChangeListener(OnCircleCircularSeekBarChangeListener listener) {
        this.listener = listener;
    }

    private class CircleCirclularSeekBarChangeListener implements OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (listener != null) {
                listener.onProgressChanged(CircleCircularSeekbar.this, progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (listener != null) {
                listener.onStartTrackingTouch(CircleCircularSeekbar.this);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (listener != null) {
                listener.onStopTrackingTouch(CircleCircularSeekbar.this);
            }
        }
    }
}
