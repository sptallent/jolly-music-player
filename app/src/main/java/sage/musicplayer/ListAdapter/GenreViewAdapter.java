package sage.musicplayer.ListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.io.Serializable;
import java.util.ArrayList;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class GenreViewAdapter extends RecyclerView.Adapter<GenreViewAdapter.GenreViewHolder> {

    private ArrayList<Song> genreSongList;
    private Context context;
    RequestOptions requestOptions;
    private OnSongClickedListener onSongClickedListener;
    MusicUtils musicUtils;
    Activity activity;
    MusicService musicService;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, genreSongList);
            while(pos != -1) {
                genreSongList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, genreSongList.size());

                pos = musicUtils.getPositionInList(song_id, genreSongList);
            }
        }
    };

    public GenreViewAdapter(Context c, Activity a, MusicService ms) {
        context = c;
        activity = a;
        musicService = ms;
        genreSongList = new ArrayList<>();
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(/*new CenterCrop(),*/new RoundedCorners(16));
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.genre_view_item, parent, false);
        return new GenreViewHolder(context, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Song s = genreSongList.get(position);
        holder.bindSong(s, onSongClickedListener, position);
    }

    @Override
    public int getItemCount() {
        if(genreSongList != null)
            return genreSongList.size();
        return 0;
    }

    public void setGenreSongList(ArrayList<Song> songs) {
        genreSongList = songs;
    }

    public void setOnSongClickedListener(OnSongClickedListener l) {
        onSongClickedListener = l;
    }

    class GenreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        public GenreViewHolder(Context c, @NonNull View itemView) {
            super(itemView);
            iView = itemView;
            context = c;
            musicUtils = new MusicUtils(context.getContentResolver(), context);
            songName = itemView.findViewById(R.id.genre_song_title);
            songArtist = itemView.findViewById(R.id.genre_song_artist);
            albumArt = itemView.findViewById(R.id.genreViewArt);
            songDur = itemView.findViewById(R.id.genre_song_dur);
            itemMore = itemView.findViewById(R.id.song_more);
            itemView.setOnClickListener(this);
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
                inflater.inflate(R.menu.song_more_options_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()) {
                        case R.id.popup_play:
                            ((MainActivity)activity).getMusicSrv().setList(genreSongList);
                            ((MainActivity)activity).songPicked(pos);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            break;
                        case R.id.popup_play_shuffle:
                            ((MainActivity)activity).getMusicSrv().setList(genreSongList);
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
                        case R.id.popup_add_playlist:
                            long[] add_songs = new long[1];
                            add_songs[0] = s.getID();
                            musicUtils.showPlaylists(add_songs, itemView);
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
