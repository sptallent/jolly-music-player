package sage.musicplayer.Util;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import sage.musicplayer.R;

public class AnimationHelper {

    private Context context;
    Animation topDown;
    Animation bottomUp;
    Animation opacity_appear;
    Animation opacity_disappear;

    public AnimationHelper(Context c) {
        this.context = c;
        topDown = AnimationUtils.loadAnimation(this.context, R.anim.fade_out);
        bottomUp = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        opacity_appear = new AlphaAnimation(0.0f, 1.0f);
        opacity_disappear = new AlphaAnimation(1.0f, 0.0f);
    }

    public void opacity(final View change, boolean appear) {
        if(appear) {
            opacity_appear.setDuration(1000);
            change.startAnimation(opacity_appear);
        }else {
            opacity_disappear.setDuration(1000);
            change.startAnimation(opacity_disappear);
        }
    }

    public void bottomUp(final View appear, final View disappear) {
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                appear.setVisibility(View.VISIBLE);
                disappear.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        appear.startAnimation(bottomUp);
    }

    public void topDown(final View appear, final View disappear) {
        topDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Animation opacity = new AlphaAnimation(0.0f, 1.0f);
                opacity.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        appear.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                opacity.setDuration(500);
                appear.startAnimation(opacity);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        disappear.startAnimation(topDown);
    }
}
