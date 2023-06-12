package sage.musicplayer.ListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sage.musicplayer.Interface.ClickListener;
import sage.musicplayer.R;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.ListAdapter.Holders.SongViewHolder;

public class SongAdapter extends RecyclerView.Adapter<SongViewHolder>{

    private ArrayList<Song> songs;
    private Context context;
    private int itemResource;
    private OnSongClickedListener onSongClickedListener;
    private MusicUtils musicUtils;
    private MusicService musicService;
    private Map<Integer, Boolean> selected;
    private boolean selectionMode = false;
    private ClickListener clickListener;
    private Activity activity;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, songs);
            while(pos != -1) {
                songs.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, songs.size());

                pos = musicUtils.getPositionInList(song_id, songs);
            }
        }
    };

    public SongAdapter(Context c, Activity a, ArrayList<Song> theSongs, int iR, MusicService ms, ClickListener cListener) {
        this.songs = theSongs;
        musicUtils = new MusicUtils(c.getContentResolver(), c);
        this.context = c;
        this.musicService = ms;
        this.itemResource = iR;
        this.activity = a;

        selected = new LinkedHashMap<>();
        clickListener = cListener;

        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    public void toggleSelection(int position) {
        if (selected.containsKey(position)) {
            selected.remove(position);
        } else {
            selected.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelection() {
        List<Integer> selection = getSelected();
        selected.clear();
        for(Integer i : selection)
            notifyItemChanged(i);
    }

    public int getSelectedItemCount() {
        return selected.size();
    }

    public boolean isSelected(int position) {
        return getSelected().contains(position);
    }

    public List<Integer> getSelected() {
        List<Integer> items = new ArrayList<>(selected.size());
        for(int i = 0; i < selected.size(); i++)
            items.add((Integer)selected.keySet().toArray()[i]);
        return items;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(this.itemResource, parent, false);
        return new SongViewHolder(this.context, activity, view, musicService, songs, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder songViewHolder, int i) {
        Song song = this.songs.get(i);
        songViewHolder.itemView.setSelected(isSelected(i));
        songViewHolder.bindSong(song, onSongClickedListener, i, getSongs());
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void setSongs(ArrayList<Song> s) {
        songs = s;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }
}
