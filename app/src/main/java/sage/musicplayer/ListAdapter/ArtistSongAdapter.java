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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;

public class ArtistSongAdapter extends BaseAdapter implements View.OnClickListener {

    ArrayList<Song> songList;
    MusicUtils musicUtils;
    MusicService musicService;
    Activity activity;
    Context context;
    private OnSongClickedListener onSongClickedListener;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            int pos = musicUtils.getPositionInList(song_id, songList);
            while(pos != -1) {
                songList.remove(pos);
                notifyDataSetChanged();

                pos = musicUtils.getPositionInList(song_id, songList);
            }
        }
    };

    public ArtistSongAdapter(Context c, Activity a, MusicService ms, ArrayList<Song> songs) {
        songList = songs;
        musicService = ms;
        activity = a;
        context = c;
        musicUtils = new MusicUtils(c.getContentResolver(), context);
        LocalBroadcastManager.getInstance(c).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int position) {
        return songList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnSongClickedListener(OnSongClickedListener l) {
        onSongClickedListener = l;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        ConstraintLayout artistSongLay = (ConstraintLayout)LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_songs, parent, false);

        TextView songView = (TextView) artistSongLay.findViewById(R.id.artist_song_title);
        TextView artistView = (TextView) artistSongLay.findViewById(R.id.artist_song_artist);
        TextView durView = (TextView) artistSongLay.findViewById(R.id.artist_song_dur);
        ImageView itemMore = artistSongLay.findViewById(R.id.song_more);
        //get song using position
        Song currSong = songList.get(position);

        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getAlbum_name());
        durView.setText(convertFromMilli(Integer.parseInt(currSong.getDuration())));

        itemMore.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), itemMore);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.song_more_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.popup_play:
                        ((MainActivity)activity).getMusicSrv().setList(songList);
                        ((MainActivity)activity).songPicked(position);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;
                    case R.id.popup_play_shuffle:
                        ((MainActivity)activity).getMusicSrv().setList(songList);
                        ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                        ((MainActivity)activity).songPicked(position);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        break;
                    case R.id.popup_delete:
                        musicUtils.deleteSongFromDevice(currSong.getID(), v.getContext(), activity, musicService);
                        break;
                    case R.id.popup_play_next:
                        musicUtils.playNextAdd(currSong);
                        break;
                    case R.id.popup_add_playlist:
                        long[] add_songs = new long[1];
                        add_songs[0] = currSong.getID();
                        musicUtils.showPlaylists(add_songs, v);
                        break;
                    case R.id.popup_edit_tags:
                        musicUtils.showEditDialog(currSong.getID(), activity, v);
                        break;
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });

        artistSongLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onSongClickedListener != null) {
                    onSongClickedListener.onSongClicked(position);
                }
            }
        });

        return artistSongLay;
    }

    public String convertFromMilli(int milli) {
        String temp;
        long min = TimeUnit.MILLISECONDS.toMinutes(milli);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milli) - TimeUnit.MINUTES.toSeconds(min);

        if(sec == 0)
            temp = min + ":00";
        else if(sec < 10)
            temp = min + ":0" + sec;
        else
            temp = min + ":" + sec;

        return temp;
    }

    @Override
    public void onClick(View v) {

    }
}