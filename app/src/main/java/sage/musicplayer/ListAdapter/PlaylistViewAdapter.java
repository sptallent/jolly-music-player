package sage.musicplayer.ListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import java.io.Serializable;
import java.util.ArrayList;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class PlaylistViewAdapter extends RecyclerView.Adapter<PlaylistViewAdapter.PlaylistViewHolder> {

    private ArrayList<Song> playlistSongList;
    private Context context;
    RequestOptions requestOptions;
    private OnSongClickedListener onSongClickedListener;
    MusicUtils musicUtils;
    MusicService musicService;
    Activity activity;
    String playlistName = "";
    DatabaseHelper dbHelper;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, playlistSongList);
            while(pos != -1) {
                playlistSongList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, playlistSongList.size());

                pos = musicUtils.getPositionInList(song_id, playlistSongList);
            }
        }
    };

    private BroadcastReceiver playlistUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyDataSetChanged();
        }
    };

    public PlaylistViewAdapter(Context c, Activity a, MusicService ms) {
        context = c;
        activity = a;
        musicService = ms;
        dbHelper = new DatabaseHelper(context);
        playlistSongList = new ArrayList<>();
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(/*new CenterCrop(),*/new RoundedCorners(16));
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
        LocalBroadcastManager.getInstance(context).registerReceiver(playlistUpdatedReceiver, new IntentFilter("playlistUpdated"));
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout itemView = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_view_item, parent, false);
        return new PlaylistViewHolder(context, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Song s = playlistSongList.get(position);
        holder.bindSong(s, onSongClickedListener, position);
    }

    @Override
    public int getItemCount() {
        return playlistSongList.size();
    }

    public void setPlaylistSongList(ArrayList<Song> songs) {
        playlistSongList = songs;
    }

    public void setPlaylistName(String name) {
        playlistName = name;
    }

    public void setOnSongClickedListener(OnSongClickedListener l) {
        onSongClickedListener = l;
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView songName;
        TextView songArtist;
        ImageView albumArt;
        TextView songDur;
        Context context;
        MusicUtils musicUtils;
        Song song;
        int pos;
        View iView;
        ImageView itemMore;

        public PlaylistViewHolder(Context c, @NonNull View itemView) {
            super(itemView);
            iView = itemView;
            context = c;
            musicUtils = new MusicUtils(context.getContentResolver(), context);
            songName = itemView.findViewById(R.id.playlist_song_title);
            songArtist = itemView.findViewById(R.id.playlist_song_artist);
            albumArt = itemView.findViewById(R.id.playlistViewArt);
            songDur = itemView.findViewById(R.id.playlist_song_dur);
            itemMore = itemView.findViewById(R.id.song_more);
            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
        }

        public void bindSong(Song s, OnSongClickedListener l, int p) {
            song = s;
            pos = p;
            if(s.getTitle() != null)
                songName.setText(s.getTitle());
            else
                songName.setText(R.string.not_found);

            if(s.getArtist() != null)
                songArtist.setText(s.getArtist());

            if(s.getDuration() != null)
                songDur.setText(musicUtils.convertFromMilli(Integer.parseInt(s.getDuration())));

            if(s.getAlbum_art() != null && !s.getAlbum_art().equals(""))
                Glide.with(context).load(s.getAlbum_art()).transition(withCrossFade(2000)).apply(requestOptions).into(albumArt);
            else if(s.getAlbumID() != -1)
                Glide.with(context).load(musicUtils.getAlbumArt((s.getAlbumID()))).transition(withCrossFade(2000)).apply(requestOptions).into(albumArt);
            else
                Glide.with(context).load(R.drawable.new_album_art).transition(withCrossFade(2000)).apply(requestOptions).into(albumArt);
            setOnSongClickedListener(l);

            itemMore.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), itemMore);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.playlist_more_options_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()) {
                        case R.id.popup_play:
                            ((MainActivity)activity).getMusicSrv().setList(playlistSongList);
                            ((MainActivity)activity).songPicked(pos);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            break;
                        case R.id.popup_play_shuffle:
                            ((MainActivity)activity).getMusicSrv().setList(playlistSongList);
                            ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                            ((MainActivity)activity).songPicked(pos);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            break;
                        case R.id.popup_delete:
                            musicUtils.deleteSongFromDevice(song.getID(), v.getContext(), activity, musicService);
                            break;
                        case R.id.popup_play_next:
                            musicUtils.playNextAdd(s);
                            break;
                        case R.id.remove_from_playlist:
                            Playlist tempPlaylist = dbHelper.getPlaylistByName(playlistName);
                            if(tempPlaylist != null && tempPlaylist.getId() != -1) {
                                musicUtils.removeFromPlaylist((int)tempPlaylist.getId(), (int)s.getID());
                                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("playlistChanged"));
                                playlistSongList.remove(p);
                                notifyItemRemoved(p);
                                notifyItemRangeChanged(p, playlistSongList.size());
                            }
                            break;
                        case R.id.popup_edit_tags:
                            musicUtils.showEditDialog(s.getID(), activity, itemView);
                            break;
                        default:
                            return false;
                    }
                    return true;
                });
                popupMenu.show();
            });
        }

        @Override
        public void onClick(View view) {
            if(onSongClickedListener != null && song != null) {
                onSongClickedListener.onSongClicked(pos);
            }
        }

        public void playNextAdd(Song song_obj) {
            Intent intent = new Intent("addPlayNext");
            intent.putExtra("song_obj", (Serializable)song_obj);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

}
