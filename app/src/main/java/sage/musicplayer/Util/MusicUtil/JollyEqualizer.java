package sage.musicplayer.Util.MusicUtil;

import android.media.audiofx.Equalizer;

public class JollyEqualizer extends Equalizer {
    public JollyEqualizer(int priority, int audioSession) throws IllegalArgumentException, IllegalStateException, RuntimeException, UnsupportedOperationException {
        super(priority, audioSession);
    }
}
