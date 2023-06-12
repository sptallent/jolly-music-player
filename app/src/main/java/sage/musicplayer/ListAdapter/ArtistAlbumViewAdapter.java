package sage.musicplayer.ListAdapter;

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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import sage.musicplayer.R;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ArtistAlbumViewAdapter extends RecyclerView.Adapter<ArtistAlbumViewAdapter.ArtistAlbumViewHolder> {

    Context context;
    ArrayList<Album> albums;
    RequestOptions requestOptions;
    MusicUtils musicUtils;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            for(int i = 0; i < albums.size(); i++) {
                ArrayList<Song> temp = albums.get(i).getAlbumSongs();
                Album a = albums.get(i);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                if(temp.size() <= 0) {
                    albums.remove(i);
                    notifyItemRemoved(i);
                    notifyItemRangeChanged(i, albums.size());
                    break;
                }else{
                    a.setAlbumSongs(temp);
                    albums.set(i, a);
                    notifyItemChanged(i);
                }

            }
        }
    };

    public ArtistAlbumViewAdapter(Context c, ArrayList<Album> a) {
        context = c;
        albums = a;
        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(/*new CenterCrop(),*/new RoundedCorners(16));
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @NonNull
    @Override
    public ArtistAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_view_album, parent, false);
        return new ArtistAlbumViewHolder(view, context, requestOptions);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistAlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        /*if(position < albums.size()-1)
            holder.itemView.setPadding(8, 8, 8, 8);*/
        holder.bindAlbum(album);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public static class ArtistAlbumViewHolder extends RecyclerView.ViewHolder {

        TextView artist_view_album_item_text;
        ImageView artist_view_album_item_image;
        Context context;
        MusicUtils musicUtils;
        RequestOptions requestOptions;

        public ArtistAlbumViewHolder(@NonNull View itemView, Context c, RequestOptions ro) {
            super(itemView);
            context = c;
            requestOptions = ro;
            musicUtils = new MusicUtils(context.getContentResolver(), context);
            artist_view_album_item_text = itemView.findViewById(R.id.artist_view_album_item_text);
            artist_view_album_item_image = itemView.findViewById(R.id.artist_view_album_item_image);
        }

        public void bindAlbum(Album a) {
            if(a.getName() != null && !a.getName().equals(""))
                artist_view_album_item_text.setText(a.getName());
            else
                artist_view_album_item_text.setText("");
            if(a.getAlbum_art() != null && !a.getAlbum_art().equals(""))
                Glide.with(context).load(a.getAlbum_art()).transition(withCrossFade(2000)).apply(requestOptions).into(artist_view_album_item_image);
            else if(musicUtils.getAlbumArt(a.getAlbum_id()) != null)
                Glide.with(context).load(musicUtils.getAlbumArt(a.getAlbum_id())).transition(withCrossFade(2000)).apply(requestOptions).into(artist_view_album_item_image);
            else
                Glide.with(context).load(R.drawable.new_album_art).apply(requestOptions).into(artist_view_album_item_image);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent("openAlbum");
                Bundle bundle = new Bundle();
                bundle.putSerializable("album", a);
                intent.putExtras(bundle);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            });
        }
    }
}


