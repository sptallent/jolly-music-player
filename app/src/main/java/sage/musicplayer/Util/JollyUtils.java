package sage.musicplayer.Util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Calendar;

import static sage.musicplayer.Util.UIUtil.BitmapUtils.getBitmapFromDrawable;

public class JollyUtils {

    Context context;
    public final int READ_EXTERNAL_STORAGE = 501;
    public final int RECORD_AUDIO = 1023;
    public final int SELECT_IMAGE = 848;

    public JollyUtils(Context c) {
        context = c;
    }

    public void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public Drawable blurDrawable(Context context, Drawable drawable, float blurRadius) {
        // Convert the Drawable to a Bitmap
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        // Create a RenderScript context
        RenderScript rs = RenderScript.create(context);

        // Create a blurred bitmap
        Bitmap blurredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Allocation input = Allocation.createFromBitmap(rs, bitmap);
        Allocation output = Allocation.createFromBitmap(rs, blurredBitmap);
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);
        script.setRadius(blurRadius);
        script.forEach(output);
        output.copyTo(blurredBitmap);

        // Convert the blurred bitmap to a Drawable
        Drawable blurredDrawable = new BitmapDrawable(context.getResources(), blurredBitmap);

        // Release the resources
        input.destroy();
        output.destroy();
        script.destroy();
        rs.destroy();

        return blurredDrawable;
    }

    public long getTimeInMillis(int hours, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

}
