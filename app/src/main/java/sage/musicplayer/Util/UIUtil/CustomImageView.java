package sage.musicplayer.Util.UIUtil;

import android.content.Context;
import android.util.AttributeSet;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {

    public CustomImageView(Context context) {
        super(context);
    }

    public CustomImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        setMeasuredDimension(height, height);
    }
}
