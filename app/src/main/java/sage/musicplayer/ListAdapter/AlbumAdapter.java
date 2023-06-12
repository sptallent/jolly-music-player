package sage.musicplayer.ListAdapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import java.util.ArrayList;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.R;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumHolder> {

    private ArrayList<Album> albumList;
    private Context context;
    private Activity activity;
    private MusicUtils musicUtils;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            for(int i = 0; i < albumList.size(); i++) {
                ArrayList<Song> temp = albumList.get(i).getAlbumSongs();
                Album a = albumList.get(i);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                if(temp.size() <= 0) {
                    albumList.remove(i);
                    notifyItemRemoved(i);
                    notifyItemRangeChanged(i, albumList.size());
                    break;
                }else{
                    a.setAlbumSongs(temp);
                    albumList.set(i, a);
                    notifyItemChanged(i);
                }

            }
        }
    };

    public AlbumAdapter(Context c, Activity a, ArrayList<Album> aList) {
        this.albumList = aList;
        this.context = c;
        this.activity = a;
        musicUtils = new MusicUtils(this.context.getContentResolver(), this.context);
    }

    @NonNull
    @Override
    public AlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
        return new AlbumHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumHolder holder, @SuppressLint("RecyclerView") int position) {
        Album a = albumList.get(position);
        holder.album = a;
        if(a.getName() != null && !a.getName().equals(""))
            holder.name.setText(a.getName());
        else
            holder.name.setText(R.string.not_found);

        if(a.getArtist() != null && !a.getArtist().equals(""))
            holder.artist.setText(a.getArtist());
        else
            holder.name.setText("");

        if(a.getAlbum_art() != null && !a.getAlbum_art().equals(""))
            Glide.with(context).load(a.getAlbum_art()).transition(withCrossFade(2000)).into(holder.art);
        else if(musicUtils.getAlbumArt(a.getAlbum_id()) != null)
            Glide.with(context).load(musicUtils.getAlbumArt(a.getAlbum_id())).transition(withCrossFade(2000)).into(holder.art);
        else
            Glide.with(context).load(R.drawable.new_album_art).transition(withCrossFade(2000)).into(holder.art);

        holder.itemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
                PopupMenu menu = new PopupMenu(wrapper, holder.itemMore);
                menu.inflate(R.menu.menu_album);
                menu.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()) {
                        case R.id.album_play:
                            ((MainActivity)activity).getMusicSrv().setList(albumList.get(position).getAlbumSongs());
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.album_shuffle:
                            ((MainActivity)activity).getMusicSrv().setList(albumList.get(position).getAlbumSongs());
                            ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.album_play_next:
                            for(Song s : albumList.get(position).getAlbumSongs())
                                musicUtils.playNextAdd(s);
                            return true;
                        case R.id.album_add_playlist:
                            long[] add_songs = new long[albumList.get(position).getAlbumSongs().size()];
                            for(int i = 0; i < albumList.get(position).getAlbumSongs().size(); i++)
                                add_songs[i] = albumList.get(position).getAlbumSongs().get(i).getID();
                            musicUtils.showPlaylists(add_songs, v);
                            return true;
                        case R.id.album_delete:
                            long[] del_songs = new long[albumList.get(position).getAlbumSongs().size()];
                            for(int i = 0; i < albumList.get(position).getAlbumSongs().size(); i++)
                                del_songs[i] = albumList.get(position).getAlbumSongs().get(i).getID();
                            musicUtils.deleteTracks(context, activity, del_songs, ((MainActivity)activity).getMusicSrv());
                            return true;
                    }
                    return false;
                });
                menu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public void setAlbumList(ArrayList<Album> albumList) {
        this.albumList = albumList;
    }

    class AlbumHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView artist;
        ImageView art;
        Album album;
        ImageView itemMore;

        private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long song_id = intent.getLongExtra("song_id", -1);

                ArrayList<Song> temp = album.getAlbumSongs();
                int pos = musicUtils.getPositionInList(song_id, temp);
                while(pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                album.setAlbumSongs(temp);
            }
        };

        public AlbumHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_album_name);
            artist = itemView.findViewById(R.id.item_album_artist);
            art = itemView.findViewById(R.id.item_album_img);
            itemMore = itemView.findViewById(R.id.album_more);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent("openAlbum");
                Bundle bundle = new Bundle();
                bundle.putSerializable("album", album);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            });
        }
    }
}


