package sage.musicplayer.ListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import sage.musicplayer.R;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.ListAdapter.Holders.SongQueueViewHolder;

public class SongQueueAdapter extends RecyclerView.Adapter<SongQueueViewHolder> {

    private Context context;
    private int itemResource;
    private OnSongClickedListener onSongClickedListener;
    private MusicService musicService;
    private ArrayList<Song> songList;
    Activity activity;
    MusicUtils musicUtils;
    int currently_playing_position = -1;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);
            int pos = musicUtils.getPositionInList(song_id, songList);
            while(pos != -1) {
                songList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, songList.size());

                pos = musicUtils.getPositionInList(song_id, songList);
            }
        }
    };

    private BroadcastReceiver songQueueRemoveReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);
            int pos = intent.getIntExtra("pos", -1);

            if(pos > -1 && pos < songList.size()) {
                songList.remove(pos);
                if(pos < currently_playing_position) {
                    if(musicService.getSongPosn()-1 > -1)
                        musicService.setSong(musicService.getSongPosn()-1);
                    else
                        musicService.setSong(0);
                }
            }
            musicService.setList(songList);
            setCurrentlyPlayingPosition(musicService.getSongPosn());
            notifyDataSetChanged();
        }
    };

    public SongQueueAdapter(Context c, Activity a, int iR, OnSongClickedListener onSongClickL, MusicService ms, ArrayList<Song> s) {
        this.context = c;
        activity = a;
        this.itemResource = iR;
        this.onSongClickedListener = onSongClickL;
        this.musicService = ms;
        this.songList = s;
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
        LocalBroadcastManager.getInstance(context).registerReceiver(songQueueRemoveReciever, new IntentFilter("removeFromQueue"));
    }

    @NonNull
    @Override
    public SongQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.itemResource, parent, false);
        return new SongQueueViewHolder(this.context, activity, view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongQueueViewHolder holder, int i) {
        Song song = songList.get(i);
        if(i%2==0)
            holder.itemView.setBackground(context.getDrawable(R.drawable.listview_queue_item));
        else
            holder.itemView.setBackground(context.getDrawable(R.drawable.listview_queue_item2));
        holder.bindSong(song, songList, onSongClickedListener, i);

        if(musicService != null && currently_playing_position == i) {
            holder.startVisualizer(musicService.getPlayer());
            holder.updateInfo(musicService.isPng());
            if(musicService.isPng())
                musicService.buildNotification(musicService.ACTION_PLAY);
            else
                musicService.buildNotification(musicService.ACTION_PAUSE);
            ImageButton songMorePlayPause = holder.getSongMorePlayPauseView();
            songMorePlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences settings = context.getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
                    String prefThemeName = settings.getString("theme", "lightTheme");

                    if (musicService.isPng()) {
                        if(prefThemeName.equals("lightTheme")) {
                            Glide.with(context).load(R.drawable.pause_button_dark).into(songMorePlayPause);
                        }else {
                            Glide.with(context).load(R.drawable.pausebutton).into(songMorePlayPause);
                        }
                        musicService.pausePlayer();
                    } else {
                        if (prefThemeName.equals("lightTheme")) {
                            Glide.with(context).load(R.drawable.play_dark).into(songMorePlayPause);
                        } else {
                            Glide.with(context).load(R.drawable.playbutton).into(songMorePlayPause);
                        }
                        musicService.go();
                    }
                    musicService.updateAll();
                }
            });
        }else {
            holder.stopVisualizer();
        }
    }

    public void setCurrentlyPlayingPosition(int pos) {
        currently_playing_position = pos;
    }

    public int getCurrentlyPlayingPosition() {
        return currently_playing_position;
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void setOnSongClickedListener(OnSongClickedListener l) {
        onSongClickedListener = l;
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    public void setSongList(ArrayList<Song> adapterList) {
        this.songList = adapterList;
    }
}
