package sage.musicplayer.ListAdapter.Holders;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.PopupMenu;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import sage.musicplayer.Interface.ClickListener;
import sage.musicplayer.ListAdapter.EditViewPagerAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private Context context;
    private TextView songView;
    private ImageView songImgView;
    private TextView artistView;
    private TextView durView;
    private ImageView moreOptions;
    private Song song;
    private MusicUtils musicUtils;
    private Bitmap albumArt;
    private Activity activity;
    int posn;
    ArrayList<Song> songs;
    MusicService musicService;
    RequestOptions requestOptions;
    DatabaseHelper dbHelper;
    JollyUtils jollyUtils;
    private ClickListener clickListener;
    EditViewPagerAdapter editAdapter;
    LayoutInflater inflater;
    View editViewMediaStore;
    View editViewFingerprint;
    String loc = null;

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri uri = intent.getParcelableExtra("uri");
            if (loc != null && uri != null) {
                if (loc.equals("media")) {
                    Glide.with(context).load(uri).placeholder(context.getResources().getDrawable(R.drawable.new_album_art)).into((ImageView) editViewMediaStore.findViewById(R.id.edit_album_image));
                    editViewMediaStore.setTag(uri);
                } else if (loc.equals("fp")) {
                    Glide.with(context).load(uri).placeholder(context.getResources().getDrawable(R.drawable.new_album_art)).into((ImageView) editViewFingerprint.findViewById(R.id.edit_fp_album_image));
                    editViewFingerprint.setTag(uri);
                }
                loc = null;
            }
        }
    };

    public SongViewHolder(Context c, Activity a,  View itemView, MusicService ms, ArrayList<Song> s, ClickListener cListener) {
        super(itemView);
        this.context = c;
        this.song = null;
        this.songView = itemView.findViewById(R.id.song_title);
        this.songImgView = itemView.findViewById(R.id.song_art);
        this.artistView = itemView.findViewById(R.id.song_artist);
        this.durView = itemView.findViewById(R.id.song_dur);
        this.moreOptions = itemView.findViewById(R.id.song_more);
        this.musicUtils = new MusicUtils(context.getContentResolver(), context);
        this.posn = 0;
        this.musicService = ms;
        this.songs = s;
        this.activity = a;

        requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(/*new CenterCrop(),*/new RoundedCorners(16));//20

        /*itemView.setSelected(true);
        itemView.setFocusable(true);
        itemView.setClickable(true);
        itemView.setLongClickable(true);*/

        jollyUtils = new JollyUtils(context);
        dbHelper = new DatabaseHelper(context);
        clickListener = cListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        editAdapter = new EditViewPagerAdapter();
        editViewMediaStore = inflater.inflate(R.layout.edit_view_media_store, null);
        editViewFingerprint = inflater.inflate(R.layout.edit_view_fingerprint, null);
        editAdapter.addView(editViewMediaStore, "MediaStore");
        editAdapter.addView(editViewFingerprint, "Fingerprint");

        LocalBroadcastManager.getInstance(context).registerReceiver(dataReceiver, new IntentFilter("pickImageRequest"));
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null) {
            clickListener.onItemClicked(getPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (clickListener != null) {
            return clickListener.onItemLongClicked(getPosition());
        }

        return false;
    }

    public void bindSong(Song s, final OnSongClickedListener l, int pos, ArrayList<Song> temp_songs) {
        this.song = s;
        this.songs = temp_songs;
        this.songView.setText(song.getTitle());
        this.albumArt = musicUtils.getAlbumArt(s.getAlbumID());
        //Glide.with(context).clear(songImgView);
        if(s.getAlbum_art() != null && !s.getAlbum_art().equals("")) {
            Glide.with(context).load(s.getAlbum_art()).transition(withCrossFade(2000)).apply(requestOptions).into(songImgView);//.diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        }else {
            if (albumArt != null) {
                Glide.with(context).load(albumArt).transition(withCrossFade(2000)).apply(requestOptions).into(songImgView);
            } else {
                Glide.with(context).load(R.drawable.new_album_art).transition(withCrossFade(2000)).apply(requestOptions).into(songImgView);
            }
        }
        this.artistView.setText(song.getArtist());
        try {
            this.durView.setText(this.musicUtils.convertFromMilli(Integer.parseInt(s.getDuration())));
        }catch(NumberFormatException e) {
            e.printStackTrace();
        }
        this.posn = pos;

        this.moreOptions.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(wrapper, moreOptions);
            popupMenu.getMenuInflater().inflate(R.menu.song_more_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.popup_play:
                        ((MainActivity)activity).getMusicSrv().setList(songs);
                        ((MainActivity)activity).songPicked(posn);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;
                    case R.id.popup_play_shuffle:
                        ((MainActivity)activity).getMusicSrv().setList(songs);
                        ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                        ((MainActivity)activity).songPicked(posn);
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

    public void setSongs(ArrayList<Song> s) {
        songs = s;
    }
}
