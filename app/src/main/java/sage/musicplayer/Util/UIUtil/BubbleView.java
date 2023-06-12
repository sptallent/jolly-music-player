package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

import java.util.List;
import java.util.Random;

import sage.musicplayer.R;

public class BubbleView extends View {
    private int radius;
    private int speed;
    private int x;
    private int y;
    private int tx; //velocity x
    private int ty; //velocity y
    private int minRadius;
    private int maxRadius;
    private int minSpeed;
    private int maxSpeed;
    private int width;
    private Random random;
    private Paint paint;
    private float oscillation;
    private float oscillationStep;
    private static final float OSCILLATION_FREQUENCY = 0.1f; // oscillations per second
    private static final float OSCILLATION_AMPLITUDE = 5; // pixels
    private static final float COLLISION_Y_OFFSET = 50; // pixels
    private static final float COLLISION_SPEED_MULTIPLIER = 1.5f; // speed multiplier after collision
    private static final float COLLISION_DIRECTION_VARIANCE = 180; // degrees 30
    private static final float MAX_SPEED = 4;
    private int color;
    //private static final double DRAG = 0.01;

    public BubbleView(Context context, int radius, int speed, int x, int c, int minRadius, int maxRadius, int minSpeed, int maxSpeed, int width, Random random) {
        super(context);
        this.radius = radius;
        this.speed = speed;
        this.x = x;
        this.y = 0;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.width = width;
        this.random = random;
        color = c;
        oscillation = random.nextFloat() * 2 * (float) Math.PI;
        oscillationStep = random.nextFloat() * 0.1f;
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);

        // create gradient fill
        int[] colors = {color, color, color};
        float[] positions = {0, 0.5f, 1};
        Shader shader = new RadialGradient(x, y, radius, colors, positions, Shader.TileMode.CLAMP);


        // draw bubble with gradient fill and texture
        paint.setShader(shader);
        //paint.setAlpha(128);
        //paint.setShadowLayer(4, 2, 2, Color.DKGRAY);

        canvas.drawCircle(x, y, radius, paint);

        // create random size and shape for bubble
        //float size = random.nextFloat() * radius + radius;
        //float width = random.nextFloat() * size / 2 + size / 2;
        //float height = random.nextFloat() * size / 2 + size / 2;

        // create texture
        /*Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bubble_texture);
        Shader textureShader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        ComposeShader composeShader = new ComposeShader(shader, textureShader, PorterDuff.Mode.MULTIPLY);*/

        // create random gradient fill for bubble
        //int[] colors = {getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorTransparentAccent), getResources().getColor(R.color.colorSecondaryAccent)};
        //int color1 = getResources().getColor(R.color.colorWhite);
        //int color2 = getResources().getColor(R.color.colorPrimary);
        //super trippy colors. maybe use while music is playing?
        //int color1 = Color.HSVToColor(new float[]{random.nextInt(10)*10, 1, 1});
        //int color2 = Color.HSVToColor(new float[]{random.nextInt(10)*10, 1, 1});
        //Shader shader = new LinearGradient(x - radius / 2, y - radius / 2, x + radius / 2, y + radius / 2, color1, color2, Shader.TileMode.CLAMP);
        //Shader shader = new RadialGradient(x, y, radius, color1, color2, Shader.TileMode.CLAMP);
        // draw bubble with gradient fill
        //paint.setShader(shader);
        //canvas.drawCircle(x, y, radius, paint);
    }

    public void move(int minY, int maxY) {
        // Add random variation to speed and direction
        //speed += (float) (Math.random() - 0.5) * 0.2;
        tx += (float) (Math.random() - 0.5) * 0.1;

        // Apply drag force
        //speed *= DRAG;

        // Apply turbulence force
        x += (float) (Math.random() - 0.5) * 0.5;
        y += (float) (Math.random() - 0.5) * 0.5;

        // Update position
        x += tx * speed;
        y -= speed;

        // Wrap around when reaching x edges
        if (x < -radius) {
            x = width + radius;
        }
        if (x > width + radius) {
            x = -radius;
        }

        if(y <= maxY && y >= maxY-50) {
            this.x = (width/2);
        }

        // If bubble goes above the screen, recreate it at a random position
        if (y < minY) {
            recreateBubble(minY, maxY, (int) (Math.random() * (width - radius * 2) + radius));
        }
    }

    public void update(List<BubbleView> bubbles) {
        y += speed;
        oscillation += OSCILLATION_FREQUENCY;
        x += OSCILLATION_AMPLITUDE * (float) Math.sin(oscillation);
        y += OSCILLATION_AMPLITUDE * (float) Math.cos(oscillation);

        // handle collisions with other bubbles
        for (BubbleView other : bubbles) {
            if (other != this) {
                float dx = x - other.x;
                float dy = y - other.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                /*if (distance < radius + other.radius) {
                    // handle collision
                    float angle = (float) Math.atan2(dy, dx);
                    float newAngle = angle + (float) Math.toRadians(random.nextInt((int) COLLISION_DIRECTION_VARIANCE) - COLLISION_DIRECTION_VARIANCE / 2);
                    x = (int) (other.x + Math.cos(newAngle) * (radius + other.radius));
                    y = (int) (other.y + Math.sin(newAngle) * (radius + other.radius));
                    speed = (int) (Math.max(speed, other.speed) * COLLISION_SPEED_MULTIPLIER);
                    speed = (int) (Math.min(speed, MAX_SPEED));
                    oscillation = 0;
                }*/
            }
        }

        invalidate();
    }

    private void recreateBubble(int minY, int maxY, int x) {
        //this.x = x;
        y = maxY*2; //+ random.nextInt(maxY - minY + 1);
        radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        speed = minSpeed + random.nextInt(maxSpeed - minSpeed + 1);
        oscillation = random.nextFloat() * 2 * (float) Math.PI;
        oscillationStep = random.nextFloat() * 0.1f;
        int range = width - 2 * radius;
        //this.x = radius + random.nextInt(range);
        this.x = (width/2);
    }

    public void setColor(int c) {
        color = c;
    }
}