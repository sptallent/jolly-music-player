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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;

public class GenreAdapter extends BaseAdapter {

    Context context;
    HashMap<String, ArrayList<Song>> genreList;
    ArrayList<String> genres;
    ArrayList<String> genresNum;
    LayoutInflater genreInf;
    TextView genreName;
    TextView genreSongNum;
    Activity activity;
    MusicUtils musicUtils;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            HashMap<String, ArrayList<Song>> tempList = new HashMap<>();
            for (String key : genreList.keySet()) {
                ArrayList<Song> temp = genreList.get(key);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                if (temp.size() <= 0) {
                    for(int i = 0; i < genres.size(); i++) {
                        if(genres.get(i).equals(key)) {
                            genres.remove(i);
                            i--;//maybe just break
                        }
                    }
                }
                tempList.put(key, temp);
            }
            genreList = tempList;
            notifyDataSetChanged();
        }
    };

    public GenreAdapter(Context c, Activity a, HashMap<String, ArrayList<Song>> gList, ArrayList<String> genNames) {
        context = c;
        activity = a;
        genreInf = LayoutInflater.from(context);
        genreList = gList;
        genres = genNames;
        genresNum = new ArrayList<>();
        for (int i = 0; i < genres.size(); i++) {
            genresNum.add(0, String.valueOf(genreList.get(genres.get(i)).size()));
        }
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    @Override
    public int getCount() {
        return genreList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ConstraintLayout genreLay = (ConstraintLayout) genreInf.inflate(R.layout.artist, viewGroup, false);

        genreName = genreLay.findViewById(R.id.artistName);
        genreSongNum = genreLay.findViewById(R.id.artistSongNum);
        ImageView itemMore = genreLay.findViewById(R.id.item_more);

        if(genres.get(position).length() > 20)
            genreName.setText(genres.get(position).substring(0,20)+"...");
        else
            genreName.setText(genres.get(position));
        if(Integer.parseInt(genresNum.get(position)) > 1 || Integer.parseInt(genresNum.get(position)) == 0)
            genreSongNum.setText(genreList.get(genres.get(position)).size() + " " + context.getString(R.string.track_s));
        else
            genreSongNum.setText(genreList.get(genres.get(position)).size() + " " + context.getString(R.string.track_1));
        genreLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) activity).setGenreViewList(genres.get(position), genreList);
                ((MainActivity) activity).genreViewOpenClose(true);
            }
        });
        itemMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
                PopupMenu menu = new PopupMenu(wrapper, itemMore);
                menu.inflate(R.menu.menu_genre);
                menu.setOnMenuItemClickListener(item -> {
                    switch(item.getItemId()) {
                        case R.id.genre_play:
                            ((MainActivity)activity).getMusicSrv().setList(genreList.get(genres.get(position)));
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.genre_shuffle:
                            ((MainActivity)activity).getMusicSrv().setList(genreList.get(genres.get(position)));
                            ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                            ((MainActivity)activity).songPicked(0);
                            ((MainActivity)activity).getMusicSrv().updateAll();
                            return true;
                        case R.id.genre_play_next:
                            for(Song s : genreList.get(genres.get(position)))
                                musicUtils.playNextAdd(s);
                            return true;
                        case R.id.genre_add_playlist:
                            long[] add_songs = new long[genreList.get(genres.get(position)).size()];
                            for(int i = 0; i < genreList.get(genres.get(position)).size(); i++)
                                add_songs[i] = genreList.get(genres.get(position)).get(i).getID();
                            musicUtils.showPlaylists(add_songs, v);
                            return true;
                        case R.id.genre_delete:
                            long[] del_songs = new long[genreList.get(genres.get(position)).size()];
                            for(int i = 0; i < genreList.get(genres.get(position)).size(); i++)
                                del_songs[i] = genreList.get(genres.get(position)).get(i).getID();
                            musicUtils.deleteTracks(context, activity, del_songs, ((MainActivity)activity).getMusicSrv());
                            return true;
                    }
                    return false;
                });
                menu.show();
            }
        });
        return genreLay;
    }

    public void setLists(HashMap<String, ArrayList<Song>> gList, ArrayList<String> genNames) {
        genreList = gList;
        genres = genNames;
        genresNum = new ArrayList<>();
        for (int i = 0; i < genres.size(); i++) {
            genresNum.add(0, String.valueOf(genreList.get(genres.get(i)).size()));
        }
    }
}
