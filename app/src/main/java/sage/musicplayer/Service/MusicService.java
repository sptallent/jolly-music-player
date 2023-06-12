package sage.musicplayer.Service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;

import static android.content.Intent.ACTION_DELETE;

public class MusicService extends android.app.Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {

    private boolean shuffle=false;
    private boolean loop = false;
    private Random rand;

    private long songID = 0;
    private String songTitle= "Title";
    private String songArtist= "Artist";
    private String albumName;
    private String album_art = "";
    private String genre = "";
    private long albumID;
    private String songDur;

    long queuedTime;

    private static final int NOTIFY_ID=1;
    private MediaPlayer player;
    private MediaSessionManager mediaManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;
    private MediaControllerCompat mediaController;
    private ArrayList<Song> songs;
    MusicUtils musicUtils;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();
    private int myBufferPosition;
    boolean timerWait;

    final public static String ACTION_PLAY = "action_play";
    final public static String ACTION_PAUSE = "action_pause";
    final public static String ACTION_NEXT = "action_next";
    final public static String ACTION_PREV = "action_prev";
    final public static String ACTION_STOP = "action_stop";
    final public static String ACTION_IDLE = "action_idle";

    Intent playIntent;
    SharedPreferences settings;

    MyMediaButtonReceiver receiver;
    private boolean isReceiverRegistered = false;

    private BroadcastReceiver sleepTimerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), getString(R.string.sleep_timer_set), Toast.LENGTH_SHORT).show();
            pausePlayer();
        }
    };

    private BroadcastReceiver songQueueRemoveReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);
            /*
            int pos = musicUtils.getPositionInList(song_id, songs);
            while(pos != -1) {
                songs.remove(pos);
                pos = musicUtils.getPositionInList(song_id, songs);
            }*/
            //updateAll();
        }
    };

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, songs);
            while(pos != -1) {
                songs.remove(pos);
                pos = musicUtils.getPositionInList(song_id, songs);
            }
        }
    };

    private BroadcastReceiver addPlayNextReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Song song_obj = (Song)intent.getSerializableExtra("song_obj");
            addQueue(song_obj);
        }
    };

    private BroadcastReceiver songCheckDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);
            if(songPosn != -1) {
                if (song_id != -1 && songs.size() > 1 && (songs.get(songPosn).getID() == song_id)) {
                    playNext();
                }
            }
        }
    };

    public void onCreate() {
        super.onCreate();
        timerWait = true;
        songPosn = 0;
        player = new MediaPlayer();
        songs = new ArrayList<Song>();
        updateEq();
        rand = new Random();
        musicUtils = new MusicUtils(getContentResolver(), getApplicationContext());
        settings = getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
        this.queuedTime = -1;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(addPlayNextReciever, new IntentFilter("addPlayNext"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(songCheckDeletedReciever, new IntentFilter("checkDeletedSongPlaying"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(songQueueRemoveReciever, new IntentFilter("removeFromQueue"));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(sleepTimerReceiver, new IntentFilter("sleepTimerStop"));

        playIntent = new Intent(getApplicationContext(), MusicService.class);
        initMusicPlayer();
    }

    private void handleIntent( Intent intent ) {
        if( intent == null || intent.getAction() == null )
            return;
        String action = intent.getAction();
        if( action.equalsIgnoreCase( ACTION_PLAY ) ) {
            transportControls.play();
        } else if( action.equalsIgnoreCase( ACTION_PAUSE ) ) {
            transportControls.pause();
        } else if( action.equalsIgnoreCase( ACTION_PREV ) ) {
            transportControls.skipToPrevious();
        } else if( action.equalsIgnoreCase( ACTION_NEXT ) ) {
            transportControls.skipToNext();
        } else if( action.equalsIgnoreCase( ACTION_STOP ) ) {
            transportControls.stop();
        }
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent( getApplicationContext(), MusicService.class );
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder( icon, title, pendingIntent ).build();
    }

    public Notification buildNotification(String a) {
        String ID = "sage.musicplayer.Service.MusicService";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), ID);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Bitmap bitmap = null;
        if(album_art != null) {
            try { bitmap = BitmapFactory.decodeFile(album_art);} catch (Exception e) {e.printStackTrace();}
        }
        if(bitmap == null) {
            bitmap = musicUtils.getAlbumArt(albumID);
        }

        if(bitmap != null)
            builder.setLargeIcon(bitmap);
        else
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.new_album_art));
        NotificationCompat.Action action;
        if(a.equals(ACTION_PAUSE))
            action = generateAction(R.drawable.play_button, "Play", ACTION_PLAY);
        else
            action = generateAction(R.drawable.pause_button, "Pause", ACTION_PAUSE);

        // First, create the notification channel (if necessary)
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(ID, "Jolly Music Channel", NotificationManager.IMPORTANCE_NONE);
            notificationManager.createNotificationChannel(channel);
        }

        //set metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_TITLE, songTitle).putString(MediaMetadataCompat.METADATA_KEY_ARTIST, songArtist).build());

        //set playback state
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build());
        // Next, create the notification builder
        builder
                .setSmallIcon(R.drawable.logo_notify)
                .setContentTitle(songTitle)
                .setContentText(songArtist)
                .setColorized(true)//Enable colorization
                .setColor(Color.WHITE)
                .setContentIntent(pendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND).setVibrate(new long[]{0L});
        if(!a.equals(ACTION_IDLE)) {
            builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSession.getSessionToken()))
                    .addAction(generateAction(R.drawable.prev_button, "Prev", ACTION_PREV))
                    .addAction(action)
                    .addAction(generateAction(R.drawable.next_button, "Next", ACTION_NEXT));
        }


        Notification notification = builder.build();
        // Finally, build and show the notification
        boolean notificationsStatus = settings.getBoolean("notification_status", true);
        if(notificationsStatus) {
            startForeground(NOTIFY_ID, notification);
            //notificationManager.notify(0, notification);
        }else{
            notificationManager.cancelAll();
        }
        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mediaManager == null) {
            initMediaSessions();
            buildNotification(ACTION_IDLE);
        }
        handleIntent(intent);

        register();
        mediaSession.setMediaButtonReceiver(PendingIntent.getBroadcast(this, 0, new Intent(Intent.ACTION_MEDIA_BUTTON), PendingIntent.FLAG_UPDATE_CURRENT));

        return super.onStartCommand(intent, flags, startId);
    }

    public void register() {
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            receiver = new MyMediaButtonReceiver();
            registerReceiver(receiver, filter);
            isReceiverRegistered = true;
        }
    }

    private void initMediaSessions() {
        mediaManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "Jolly Player Session");
        transportControls = mediaSession.getController().getTransportControls();
        mediaController = new MediaControllerCompat(getApplicationContext(), mediaSession.getSessionToken());

        mediaSession.setActive(true);
        mediaSession.setCallback(new MediaSessionCompat.Callback(){
                                     @Override
                                     public void onPlay() {
                                         super.onPlay();
                                         go();
                                     }

                                     @Override
                                     public void onPause() {
                                         super.onPause();
                                         pausePlayer();
                                     }

                                     @Override
                                     public void onSkipToNext() {
                                         super.onSkipToNext();
                                         playNext();
                                         if(isPng())
                                             buildNotification(ACTION_PAUSE);
                                         else
                                             buildNotification(ACTION_PLAY);
                                     }

                                     @Override
                                     public void onSkipToPrevious() {
                                         super.onSkipToPrevious();
                                         playPrev();
                                         if(isPng())
                                             buildNotification(ACTION_PAUSE);
                                         else
                                             buildNotification(ACTION_PLAY);
                                     }

                                     @Override
                                     public void onStop() {
                                         super.onStop();
                                         pausePlayer();
                                         NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                         notificationManager.cancel(1);
                                         Intent intent = new Intent( getApplicationContext(), MusicService.class);
                                         stopService(intent);
                                     }

                                     @Override
                                     public void onSeekTo(long pos) {
                                         super.onSeekTo(pos);
                                     }
                                 }
        );

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    public void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() > 0) {
            mp.reset();
            if(getSongList() != null && getSongList().size() > 0)
                playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        mediaSession.release();
        player.stop();
        player.release();

        return false;
    }

    public MediaPlayer getPlayer() {
        return player;
    }

    public void playSong() {
        player.reset();

        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songID);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.setOnPreparedListener(this);
            player.prepare();
        } catch(Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        //player.prepareAsync();
        updateAll();
        buildNotification(ACTION_PLAY);

        mediaSession.setActive(true);
    }

    public void pauseSong() {
        player.reset();
        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songID);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
            player.setOnPreparedListener(this);
            player.prepare();
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        //player.prepareAsync();
        updateAll();
        buildNotification(ACTION_PAUSE);

        mediaSession.setActive(true);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        setBufferPosition(percent * getDur() / 100);
    }

    public void setSong(Song s) {
        songID = s.getID();
        songTitle = s.getTitle();
        songArtist = s.getArtist();
        albumName = s.getAlbum_name();
        songDur = s.getDuration();
        albumID = s.getAlbumID();
        album_art = s.getAlbum_art();
        genre = s.getGenre();
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
        setSong(songs.get(songPosn));
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
        //updateAll();
        buildNotification(ACTION_PAUSE);
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
        buildNotification(ACTION_PLAY);
    }

    public void playPrev() {
        Song s;
        if ((getCurrentProgress() / 1000) < 5) {
            //less than 5 seconds so we have to change to prev song

            songPosn--;
            if (songPosn < 0)
                songPosn = songs.size() - 1;
            s = songs.get(songPosn);
            setSong(s);
        }
        playSong();
    }

    public void playNext() {
        if (!loop) {
            if (shuffle) {
                int newSong = songPosn;
                if(getSongList().size() > 1) {
                    while (newSong == songPosn) {
                        newSong = rand.nextInt(songs.size());
                    }
                }
                songPosn = newSong;
                setSong(songs.get(songPosn));
            } else {
                songPosn++;
                if (songPosn >= songs.size())
                    songPosn = 0;
                setSong(songs.get(songPosn));
            }
        }
        playSong();
    }

    public void toggleShuffle(boolean shuff) {
        toggleShuffle(shuff, true);
    }

    public void toggleShuffle(boolean shuff, boolean display_toast){
        shuffle = shuff;
        String on_off;
        if(shuffle)
            on_off = getString(R.string.on);
        else
            on_off = getString(R.string.off);

        if(display_toast)
            Toast.makeText(getApplicationContext(), getString(R.string.shuffle) + " - " + on_off, Toast.LENGTH_SHORT).show();
    }

    public void toggleLoop(boolean loo) {
        toggleLoop(loo, true);
    }

    public void toggleLoop(boolean loo, boolean display_toast) {
        loop = loo;
        String on_off;
        if(loop)
            on_off = getString(R.string.on);
        else
            on_off = getString(R.string.off);

        if(display_toast)
            Toast.makeText(getApplicationContext(), getString(R.string.loop) + " - " + on_off, Toast.LENGTH_SHORT).show();
    }

    public boolean isShuffle() {
        return shuffle;
    }

    public boolean isLoop() {
        return loop;
    }

    @Override
    public void onDestroy() {
        unregister();
        stopForeground(true);
    }

    public void unregister() {
        if(receiver != null && isReceiverRegistered) {
            unregisterReceiver(receiver);
            receiver = null;
            isReceiverRegistered = false;
        }
    }

    public int getCurrentProgress() {
        return player.getCurrentPosition();
    }

    protected void setBufferPosition(int progress) {
        myBufferPosition = progress;
    }

    protected int getBufferPosition() {
        return myBufferPosition;
    }

    public int getSongPosn() {
        return songPosn;
    }

    public void updateEq() {
        Intent intent = new Intent("initEq");
        intent.putExtra("audio_session_id", player.getAudioSessionId());

        sendLocalBroadcast(intent);
    }

    public void updateAll() {
        Intent intent = new Intent("sendUpdate");
        intent.putExtra("title", songTitle);
        intent.putExtra("artist", songArtist);
        intent.putExtra("album_id", albumID);
        intent.putExtra("album_name", albumName);
        intent.putExtra("album_art", album_art);
        intent.putExtra("dur", String.valueOf(getBufferPosition()));
        intent.putExtra("genre", genre);
        intent.putExtra("isPng", isPng());

        sendLocalBroadcast(intent);
    }

    public ArrayList<Song> getSongList() {
        return songs;
    }
    public void setSongList(ArrayList<Song> s) {
        songs = s;
    }

    public static int getNotifyId() {
        return NOTIFY_ID;
    }

    public Song getSongListNext(int offset) {
        /*if (offset <= 0)// this condition and result duplicates songs on the queue list
            return getSongNext();*/
        int temp = songPosn;
        while(offset>0) {
            if((temp+1) < songs.size()-1)
                temp++;
            else
                temp = 0;
            offset--;
        }
        if(temp < 0 || temp > (songs.size()-1))
            temp = songPosn;//0
        return songs.get(temp);
    }

    public void addQueue(Song s) {
        s.setQueuedTime();
        int temp_posn = 0;
        if(getSongPosn() > -1) {
            temp_posn = getSongPosn() + 1;
            if(temp_posn > songs.size()-1)
                temp_posn = 0;
        }
        songs.add(temp_posn, s);
    }

    public String getSongDur() {
        return songDur;
    }

    public long getSongId() {
        return songID;
    }

    public class MyMediaButtonReceiver extends BroadcastReceiver {

        private static final String TAG = "MyMediaButtonReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }

        private void handleMediaKeyEvent(KeyEvent event) {
            int keyCode = event.getKeyCode();
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        if (isPng()) {
                            pausePlayer();
                        } else {
                            go();
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        playNext();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        playPrev();
                        break;
                    default:
                        Log.d(TAG, "Unhandled key code: " + keyCode);
                        break;
                }
            }
        }
    }

}