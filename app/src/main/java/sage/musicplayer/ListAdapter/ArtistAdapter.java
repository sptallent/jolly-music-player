package sage.musicplayer.ListAdapter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.TreeMap;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;

public class ArtistAdapter extends BaseAdapter {

    TreeMap<String, ArrayList<Song>> artistHashMap;
    LayoutInflater artistInf;
    TextView artistName;
    TextView artistSongNum;
    ArrayList<String> artists;
    Context context;
    Activity activity;
    MusicUtils musicUtils;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            for (String key : artistHashMap.keySet()) {
                ArrayList<Song> temp = artistHashMap.get(key);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                if (temp.size() <= 0) {
                    artistHashMap.remove(key);
                    for(int i = 0; i < artists.size(); i++) {
                        if(artists.get(i).equals(key)) {
                            artists.remove(i);
                            break;
                        }
                    }
                    notifyDataSetChanged();
                    break;
                } else {
                    artistHashMap.put(key, temp);
                    notifyDataSetChanged();
                }
            }
        }
    };

    public ArtistAdapter(Context c, Activity a, TreeMap<String, ArrayList<Song>> artistMap) {
        context = c;
        activity = a;

        artistHashMap = artistMap;
        artistInf = LayoutInflater.from(c);

        artists = new ArrayList<>();
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @Override
    public int getCount() {
        return artistHashMap.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map to song layout
        ConstraintLayout artistLay = (ConstraintLayout) artistInf.inflate(R.layout.artist, parent, false);
        artistName = (TextView) artistLay.findViewById(R.id.artistName);
        artistSongNum = (TextView) artistLay.findViewById(R.id.artistSongNum);
        ImageView itemMore = (ImageView) artistLay.findViewById(R.id.item_more);

        if(artists.get(position).length() > 20)
            artistName.setText(artists.get(position).substring(0,20)+"...");
        else
            artistName.setText(artists.get(position));
        if(artistHashMap.get(artists.get(position)).size() > 1 || artistHashMap.get(artists.get(position)).size() == 0)
            artistSongNum.setText(artistHashMap.get(artists.get(position)).size() + " " + context.getString(R.string.track_s));
        else
            artistSongNum.setText(artistHashMap.get(artists.get(position)).size() + " " + context.getString(R.string.track_1));

        artistLay.setOnClickListener(v -> ((MainActivity)activity).setArtistView(position, artistHashMap));
        itemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
                PopupMenu menu = new PopupMenu(wrapper, itemMore);
                menu.inflate(R.menu.menu_artist);
                menu.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()) {
                        case R.id.artist_play:
                            ((MainActivity)activity).getMusicSrv().setList(artistHashMap.get(artists.get(position)));
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.artist_shuffle:
                            ((MainActivity)activity).getMusicSrv().setList(artistHashMap.get(artists.get(position)));
                            ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.artist_play_next:
                            for(Song s : artistHashMap.get(artists.get(position)))
                                musicUtils.playNextAdd(s);
                            return true;
                        case R.id.artist_add_playlist:
                            long[] add_songs = new long[artistHashMap.get(artists.get(position)).size()];
                            for(int i = 0; i < artistHashMap.get(artists.get(position)).size(); i++)
                                add_songs[i] = artistHashMap.get(artists.get(position)).get(i).getID();
                            musicUtils.showPlaylists(add_songs, v);
                            return true;
                        case R.id.artist_delete:
                            long[] del_songs = new long[artistHashMap.get(artists.get(position)).size()];
                            for(int i = 0; i < artistHashMap.get(artists.get(position)).size(); i++)
                                del_songs[i] = artistHashMap.get(artists.get(position)).get(i).getID();
                            musicUtils.deleteTracks(context, activity, del_songs, ((MainActivity)activity).getMusicSrv());
                            return true;
                    }
                    return false;
                });
                menu.show();
            }
        });
        return artistLay;
    }

    public void setArtistHashMap(TreeMap<String, ArrayList<Song>> artistHashMap) {
        this.artistHashMap = artistHashMap;
        artists.clear();
        artists.addAll(artistHashMap.keySet());
    }
}
