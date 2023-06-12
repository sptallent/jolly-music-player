package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

@SuppressWarnings("SuspiciousNameCombination")
public class CustomRelativeLayout extends RelativeLayout {

    public CustomRelativeLayout(Context context) {
        super(context);
    }

    public CustomRelativeLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
