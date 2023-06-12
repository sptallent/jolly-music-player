package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.RectF;

public class VisualizerView extends View {

    private Paint mPaint;
    private byte[] mBytes;
    private RectF[] mRectFs;

    private Visualizer mVisualizer;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBytes = null;
        mPaint = new Paint();
        mPaint.setStrokeWidth(1f);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
    }

    public void link(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            throw new NullPointerException("MediaPlayer is null");
        }
        if (mediaPlayer.getAudioSessionId() == 0) {
            throw new IllegalStateException("MediaPlayer audio session ID is not set yet");
        }
        if(mediaPlayer.isPlaying()) {

            // Create a new Visualizer object and attach it to the MediaPlayer
            mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());

            // Set the Visualizer listener
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    // Update the waveform data
                    mBytes = bytes;
                    invalidate();
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    // Do nothing
                }
            }, Visualizer.getMaxCaptureRate(), false, true);

            // Enable the Visualizer object
            mVisualizer.setEnabled(true);

            // Set the capture size
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
        }
    }

    public void release() {
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBytes == null) {
            return;
        }

        if (mRectFs == null || mRectFs.length < mBytes.length) {
            mRectFs = new RectF[mBytes.length];
            float width = getWidth() / (float) mBytes.length;
            float radius = width / 2;
            for (int i = 0; i < mRectFs.length; i++) {
                float x = i * width;
                float y = getHeight() / 2;
                mRectFs[i] = new RectF(x - radius, y - radius, x + radius, y + radius);
            }
        }

        for (int i = 0; i < mBytes.length; i++) {
            float radius = (mBytes[i] + 128) * mRectFs[i].width() / 128;
            canvas.drawCircle(mRectFs[i].centerX(), mRectFs[i].centerY(), radius, mPaint);
        }
    }
}