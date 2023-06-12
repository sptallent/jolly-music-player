package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import sage.musicplayer.R;

public class BubbleLayout extends View {

    private static final int NUM_BUBBLES_CLUSTERS = 10;
    private static final int MIN_RADIUS = 2;
    private static final int MAX_RADIUS = 10;
    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 4;
    private int BUBBLE_COLOR;

    private List<BubbleView> bubbles = new ArrayList<>();
    private Random random = new Random();
    private int width, height;

    private static final int UPDATE_INTERVAL = 50; //100 or 50 milliseconds
    private ScheduledExecutorService executorService;
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            update();
        }
    };

    public BubbleLayout(Context context) {
        super(context);
        //init();
    }

    public BubbleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init();
    }

    public BubbleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //init();
    }

    public void start() {
        init();
    }

    public void stop() {
        bubbles = new ArrayList<>();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void init() {
        BUBBLE_COLOR = getResources().getColor(R.color.whiteTransparent);
        for (int i = 0; i < NUM_BUBBLES_CLUSTERS; i++) {
            bubbles.addAll(createBubbles());
        }
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(updateRunnable, UPDATE_INTERVAL, UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private ArrayList<BubbleView> createBubbles() {
        int numBubbles = random.nextInt(NUM_BUBBLES_CLUSTERS) +1;//3
        random = new Random();
        ArrayList<BubbleView> bubs = new ArrayList<>();
        for(int i = 0; i < numBubbles; i++) {
            int radius = MIN_RADIUS + random.nextInt(MAX_RADIUS - MIN_RADIUS + 1);
            int range = Math.max(0, width - radius);
            //int x = radius + random.nextInt(Math.max(1, range));
            int x = (width/2);
            int speed = MIN_SPEED + random.nextInt(MAX_SPEED - MIN_SPEED + 1);
            bubs.add(new BubbleView(getContext(), radius, speed, x, BUBBLE_COLOR, MIN_RADIUS, MAX_RADIUS, MIN_SPEED, MAX_SPEED, width, random));
        }
        return bubs;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            for (BubbleView bubble : bubbles) {
                bubble.draw(canvas);
                bubble.move(0, height);
            }
            invalidate();
        }catch(ConcurrentModificationException ex) {
            ex.printStackTrace();
        }
    }

    private void update() {
        List<BubbleView> copy = new ArrayList<>(bubbles);
        for (BubbleView bubble : bubbles) {
            bubble.setColor(BUBBLE_COLOR);
            bubble.update(copy);
        }
    }

    public void removeAllBubbles() {
        bubbles.clear();
    }

    public void setBubbleColor(int bubbleColor) {
        BUBBLE_COLOR = bubbleColor;
    }
}