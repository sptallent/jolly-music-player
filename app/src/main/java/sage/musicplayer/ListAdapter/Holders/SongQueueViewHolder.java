package sage.musicplayer.ListAdapter.Holders;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;

import androidx.annotation.ColorInt;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chibde.visualizer.BarVisualizer;
import com.chibde.visualizer.LineBarVisualizer;
import com.chibde.visualizer.LineVisualizer;

import java.util.ArrayList;
import java.util.TreeMap;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.VisualizerView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SongQueueViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private Context context;
    private TextView songQueueView;
    private ImageView songQueueImgView;
    private TextView artistQueueView;
    private TextView durQueueView;
    private Song song;
    private MusicUtils musicUtils;
    private Bitmap queueAlbumArt;
    private TextView queue_num;
    RequestOptions requestOptions;
    private OnSongClickedListener onSongClickedListener;
    private int posn;
    private ImageView itemMore;
    private ImageButton song_more_play_pause;
    private BarVisualizer visualizerView;
    private Activity activity;
    ArrayList<Song> songList;
    JollyUtils jollyUtils;

    public BroadcastReceiver songReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isPng = intent.getBooleanExtra("isPng", false);
            updateInfo(isPng);
        }
    };

    public SongQueueViewHolder(Context c, Activity a, View itemView) {
        super(itemView);
        this.context = c;
        this.activity = a;
        this.song = null;
        this.songQueueView = itemView.findViewById(R.id.song_queue_title);
        this.songQueueImgView = itemView.findViewById(R.id.song_queue_art);
        this.artistQueueView = itemView.findViewById(R.id.song_queue_artist);
        this.durQueueView = itemView.findViewById(R.id.song_queue_dur);
        this.visualizerView = itemView.findViewById(R.id.visualizerView);
        this.queue_num = itemView.findViewById(R.id.queue_num);
        this.musicUtils = new MusicUtils(context.getContentResolver(), context);
        this.posn = 0;
        this.itemMore = itemView.findViewById(R.id.song_more);
        this.song_more_play_pause = itemView.findViewById(R.id.song_more_play_pause);
        jollyUtils = new JollyUtils(this.context);
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(/*new CenterCrop(),*/new RoundedCorners(16));

        itemView.setOnClickListener(this);
        LocalBroadcastManager.getInstance(context).registerReceiver(songReceiver, new IntentFilter("sendUpdate"));
    }

    public void bindSong(Song s, ArrayList<Song> sList,  final OnSongClickedListener l, int pos) {
        this.song = s;
        songList = sList;
        this.songQueueView.setText(song.getTitle());
        this.queueAlbumArt = musicUtils.getAlbumArt(s.getAlbumID());
        //Glide.with(context).clear(songQueueImgView);
        if(s.getAlbum_art() != null && !s.getAlbum_art().equals("")) {
            Glide.with(context).load(s.getAlbum_art()).transition(withCrossFade(2000)).apply(requestOptions).into(songQueueImgView);//.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        }else {
            if (queueAlbumArt != null) {
                Glide.with(context).load(queueAlbumArt).transition(withCrossFade(2000)).apply(requestOptions).into(songQueueImgView);
            } else {
                Glide.with(context).load(R.drawable.new_album_art).transition(withCrossFade(2000)).apply(requestOptions).into(songQueueImgView);
            }
        }
        this.artistQueueView.setText(song.getArtist());
        try {
            this.durQueueView.setText(this.musicUtils.convertFromMilli(Integer.parseInt(s.getDuration())));
        }catch(NumberFormatException e) {
            e.printStackTrace();
        }
        this.posn = pos;
        queue_num.setText(String.valueOf(this.posn+1));
        onSongClickedListener = l;

        itemMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), itemMore);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.menu_queue, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.popup_play:
                        ((MainActivity)activity).getMusicSrv().setList(songList);
                        ((MainActivity)activity).songPicked(posn);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;

                    case R.id.popup_add_playlist:
                        long[] add_songs = new long[1];
                        add_songs[0] = s.getID();
                        musicUtils.showPlaylists(add_songs, itemView);
                        break;
                    /*case R.id.popup_goto_artist:
                        TreeMap<String, ArrayList<Song>> tempMap =((MainActivity)activity).getArtists();
                        int i = 0;
                        for(String key : tempMap.keySet()) {
                            if(key.equalsIgnoreCase(song.getArtist())) {
                                ((MainActivity) activity).openCloseNowPlaying(false);
                                ((MainActivity)activity).setArtistView(i);
                                break;
                            }
                            i++;
                        }
                        break;
                    case R.id.popup_goto_album:
                        ArrayList<Album> tempList = ((MainActivity)activity).getAlbumList();
                        for(Album a : tempList) {
                            for(Song stemp : a.getAlbumSongs()) {
                                if(stemp.getID() == song.getID()) {
                                    ((MainActivity) activity).openCloseNowPlaying(false);
                                    ((MainActivity) activity).setAlbumViewList(a);
                                    break;
                                }
                            }
                        }
                        break;*/
                    case R.id.popup_remove_from_queue:
                        Intent intent = new Intent("removeFromQueue");
                        intent.putExtra("song_id", s.getID());
                        intent.putExtra("pos", pos);
                        jollyUtils.sendLocalBroadcast(intent);
                        break;
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });
    }

    public ImageButton getSongMorePlayPauseView() {
        return song_more_play_pause;
    }

    public void startVisualizer(MediaPlayer mp) {
        // set custom color to the line.

        try {
            visualizerView.setColor(ContextCompat.getColor(context, R.color.colorAccent));
            visualizerView.setDensity(100);
            //visualizerView.setStrokeWidth(3);
            if (mp.getAudioSessionId() != 0)
                visualizerView.setPlayer(mp.getAudioSessionId());
            //visualizerView.link(mp);
            visualizerView.setVisibility(View.VISIBLE);
        }catch(RuntimeException ex) {
            ex.printStackTrace();
        }
        song_more_play_pause.setVisibility(View.VISIBLE);
        durQueueView.setVisibility(View.GONE);
        itemMore.setVisibility(View.GONE);
        queue_num.setTextColor(context.getColor(R.color.colorAccent));
    }

    public void stopVisualizer() {
        visualizerView.setVisibility(View.GONE);
        song_more_play_pause.setVisibility(View.GONE);
        itemMore.setVisibility(View.VISIBLE);
        durQueueView.setVisibility(View.VISIBLE);

        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
        String prefThemeName = settings.getString("theme", "lightTheme");
        int res_id = -1;
        if(prefThemeName.equals("lightTheme"))
            res_id = R.color.blackPrimary;
        else
            res_id = R.color.colorPrimary;
        int color = context.getColor(res_id);
        queue_num.setTextColor(color);
    }

    @Override
    public void onClick(View v) {
        if(onSongClickedListener != null && song != null) {
            onSongClickedListener.onSongClicked(posn);
        }
    }

    public void updateInfo(boolean isPng) {
        SharedPreferences settings = context.getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
        String prefThemeName = settings.getString("theme", "lightTheme");

        if (isPng) {
            if(prefThemeName.equals("lightTheme")) {
                Glide.with(context).load(R.drawable.pause_button_dark).into(song_more_play_pause);
            }else {
                Glide.with(context).load(R.drawable.pausebutton).into(song_more_play_pause);
            }
        } else {
            if (prefThemeName.equals("lightTheme")) {
                Glide.with(context).load(R.drawable.play_dark).into(song_more_play_pause);
            } else {
                Glide.with(context).load(R.drawable.playbutton).into(song_more_play_pause);
            }
        }
    }

}
