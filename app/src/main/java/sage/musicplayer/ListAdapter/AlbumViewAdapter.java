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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.MainActivity;
import sage.musicplayer.R;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;

public class AlbumViewAdapter extends RecyclerView.Adapter<AlbumViewAdapter.AlbumViewHolder> {

    Context context;
    Activity activity;
    Album album;
    ArrayList<Song> albumSongs;
    MusicUtils musicUtils;
    MusicService musicService;
    private OnSongClickedListener onSongClickedListener;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, albumSongs);
            while(pos != -1) {
                albumSongs.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, albumSongs.size());

                pos = musicUtils.getPositionInList(song_id, albumSongs);
            }
            album.setAlbumSongs(albumSongs);
            notifyDataSetChanged();
        }
    };

    public AlbumViewAdapter(Context c, Activity act, MusicService mu, Album a, ArrayList<Song> aSongs) {
        context = c;
        activity = act;
        musicService = mu;
        album = a;
        albumSongs = aSongs;
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_view_item, parent, false);
        return new AlbumViewHolder(context, itemView);
    }

    public void setOnSongClickedListener(OnSongClickedListener l) {
        onSongClickedListener = l;
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        Song s = albumSongs.get(position);
        if(position % 2 > 0)
            holder.itemView.setBackground(AppCompatResources.getDrawable(context, R.drawable.listview_item_color_trans));
        else
            holder.itemView.setBackground(AppCompatResources.getDrawable(context, R.drawable.listview_item_color));

        holder.itemMore.setOnClickListener(v -> {
            //Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(v.getContext(), holder.itemMore);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.song_more_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.popup_play:
                        ((MainActivity)activity).getMusicSrv().setList(albumSongs);
                        ((MainActivity)activity).songPicked(position);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;
                    case R.id.popup_play_shuffle:
                        ((MainActivity)activity).getMusicSrv().setList(albumSongs);
                        ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                        ((MainActivity)activity).songPicked(position);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;
                    case R.id.popup_delete:
                        musicUtils.deleteSongFromDevice(s.getID(), v.getContext(), activity, musicService);
                        break;
                    case R.id.popup_play_next:
                        musicUtils.playNextAdd(s);
                        break;
                    case R.id.popup_add_playlist:
                        long[] add_songs = new long[1];
                        add_songs[0] = s.getID();
                        musicUtils.showPlaylists(add_songs, holder.itemView);
                        break;
                    case R.id.popup_edit_tags:
                        musicUtils.showEditDialog(s.getID(), activity, holder.itemView);
                        break;
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });
        holder.bindSong(s, album, position, onSongClickedListener);
    }

    @Override
    public int getItemCount() {
        return albumSongs.size();
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Context context;
        TextView albumViewTrackNum;
        TextView albumViewSongName;
        TextView albumViewArtistName;
        TextView albumViewSongDur;
        ImageView itemMore;
        int pos;
        Song song;

        public AlbumViewHolder(Context c, View itemView) {
            super(itemView);
            context = c;
            albumViewTrackNum = itemView.findViewById(R.id.album_view_track_num);
            albumViewSongName = itemView.findViewById(R.id.album_view_song_name);
            albumViewArtistName = itemView.findViewById(R.id.album_view_artist_name);
            albumViewSongDur = itemView.findViewById(R.id.album_view_song_dur);
            itemMore = itemView.findViewById(R.id.album_view_more);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(onSongClickedListener != null && song != null) {
                onSongClickedListener.onSongClicked(pos);
            }
        }

        public void bindSong(Song s, Album album, int p, OnSongClickedListener l) {
            pos = p;
            song = s;
            setOnSongClickedListener(l);
            if(song.getTrack_number() != null && !song.getTrack_number().equals(""))
                albumViewTrackNum.setText(song.getTrack_number());
            else
                albumViewTrackNum.setText(" ");

            if(song.getTitle() != null && !song.getTitle().equals(""))
                albumViewSongName.setText(song.getTitle());
            else
                albumViewSongName.setText(R.string.unknown_song);

            if(song.getArtist() != null && !song.getArtist().equals(""))
                albumViewArtistName.setText(song.getArtist());
            else
                albumViewArtistName.setText(R.string.unknown_artist);

            if(song.getDuration() != null && !song.getDuration().equals(""))
                albumViewSongDur.setText(musicUtils.convertFromMilli(Integer.parseInt(song.getDuration())));
            else
                albumViewSongDur.setText("");
        }
    }

}
