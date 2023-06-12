package sage.musicplayer.Util.UIUtil;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;

public class BottomUpAnimController extends LayoutAnimationController {

    public BottomUpAnimController(Animation animation) {
        super(animation, 0.5f);
        setOrder(LayoutAnimationController.ORDER_NORMAL);
    }

    @Override
    protected long getDelayForView(View view) {
        return view.getTop();
    }
}