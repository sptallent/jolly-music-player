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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;

public class PlaylistAdapter extends BaseAdapter {

    Context context;
    HashMap<String, ArrayList<Song>> playlistList;
    ArrayList<String> playlists;
    ArrayList<String> playsNum;
    LayoutInflater playInf;
    TextView playName;
    TextView playSongNum;
    Activity activity;
    DatabaseHelper dbHelper;
    ArrayList<Playlist> allPlaylists;
    MusicUtils musicUtils;
    JollyUtils jollyUtils;
    ArrayList<Song> songList;

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            for(String key : playlistList.keySet()) {
                ArrayList<Song> temp = playlistList.get(key);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    notifyDataSetChanged();
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
            }

            for(Playlist p : dbHelper.getAllPlaylists()) {
                ArrayList<Song> temp = p.getPlaylistSongs();
                for (Song s : temp) {
                    if(s.getId() == song_id)
                        dbHelper.delSongFromPlaylist((int)p.getId(), (int)song_id);
                }
            }
            updatePlaylists();
        }
    };

    private BroadcastReceiver playlistChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updatePlaylists();
            Intent updateIntent = new Intent("playlistUpdated");
            sendUpdateIntent();
        }
    };

    public PlaylistAdapter(Context c, Activity a, HashMap<String, ArrayList<Song>> pList, ArrayList<String> playNames) {
        context = c;
        activity = a;
        jollyUtils = new JollyUtils(context);
        dbHelper = new DatabaseHelper(context);
        playInf = LayoutInflater.from(context);
        playlistList = pList;
        songList = new ArrayList<>();
        playlists = playNames;
        playsNum = new ArrayList<>();
        allPlaylists = new ArrayList<>();
        updateNums();
        musicUtils = new MusicUtils(context.getContentResolver(), context);
        LocalBroadcastManager.getInstance(context).registerReceiver(playlistChangedReceiver, new IntentFilter("playlistChanged"));
        LocalBroadcastManager.getInstance(context).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
    }

    public void sendUpdateIntent() {
        Intent updateIntent = new Intent("playlistUpdated");
        jollyUtils.sendLocalBroadcast(updateIntent);
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public String getPlaylistName(int i) {
        return playlists.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ConstraintLayout playLay = (ConstraintLayout) playInf.inflate(R.layout.artist, viewGroup, false);

        playName = playLay.findViewById(R.id.artistName);
        playSongNum = playLay.findViewById(R.id.artistSongNum);
        ImageView itemMore = playLay.findViewById(R.id.item_more);

        itemMore.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(context, R.style.popupMenuStyle);
            PopupMenu menu = new PopupMenu(wrapper, itemMore);
            menu.inflate(R.menu.menu_playlist);
            menu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.playlist_play:
                        ((MainActivity)activity).getMusicSrv().setList(allPlaylists.get(i).getPlaylistSongs());
                        ((MainActivity)activity).songPicked(0);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        return true;
                    case R.id.playlist_shuffle:
                        ((MainActivity)activity).getMusicSrv().setList(allPlaylists.get(i).getPlaylistSongs());
                        ((MainActivity)activity).getMusicSrv().toggleShuffle(true);
                        ((MainActivity)activity).songPicked(0);
                        ((MainActivity)activity).getMusicSrv().updateAll();
                        return true;
                    case R.id.playlist_play_next:
                        for(Song s : allPlaylists.get(i).getPlaylistSongs())
                            musicUtils.playNextAdd(s);
                        return true;
                    case R.id.remove_songs:
                        String rem_name = playlists.get(i);
                        Playlist p_rem = null;
                        for(Playlist j : dbHelper.getAllPlaylists()) {
                            if(j.getPlaylistName().equals(rem_name))
                                p_rem = j;
                        }
                        if (p_rem != null) {
                            dbHelper.delPlaylist((int)p_rem.getId());
                            dbHelper.addPlaylist(rem_name);
                            updatePlaylists();
                        }
                        return true;
                    case R.id.del_playlist:
                        String del_name = playlists.get(i);
                        Playlist p_del = null;
                        for(Playlist j : dbHelper.getAllPlaylists()) {
                            if(j.getPlaylistName().equals(del_name))
                                p_del = j;
                        }
                        if(p_del != null) {
                            dbHelper.delPlaylist((int)p_del.getId());
                            updatePlaylists();
                        }
                        return true;
                    default:
                        return false;
                }
            });
            menu.show();
        });

        if(playlists.get(i).length() > 20)
            playName.setText(playlists.get(i).substring(0,20)+"...");
        else
            playName.setText(playlists.get(i));
        if(Integer.parseInt(playsNum.get(i)) > 1 || Integer.parseInt(playsNum.get(i)) == 0)
            playSongNum.setText(playlistList.get(playlists.get(i)).size() + " " + context.getString(R.string.track_s));
        else
            playSongNum.setText(playlistList.get(playlists.get(i)).size() + " " + context.getString(R.string.track_1));
        playLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) activity).setPlaylistMap(playlistList);
                ((MainActivity) activity).setPlaylistViewList(playlists.get(i));
                ((MainActivity) activity).playlistViewOpenClose(true);
            }
        });
        return playLay;
    }

    public void updateNums() {
        playsNum = new ArrayList<>();
        for (int i = 0; i < playlists.size(); i++) {
            playsNum.add(String.valueOf(playlistList.get(playlists.get(i)).size()));//0
        }
    }

    public void updatePlaylists() {
        allPlaylists = dbHelper.getAllPlaylists();
        playlistList.clear();
        ArrayList<Song> temp = new ArrayList<>(songList);
        HashMap<String, Song> tempMap = new HashMap<>();
        for(Song s : temp) {
            tempMap.put(String.valueOf(s.getID()), s);
        }
        boolean checkFavorites = false;
        for(int i = 0; i < allPlaylists.size(); i++) {
            String playListName = allPlaylists.get(i).getPlaylistName();
            if(playListName.equals("Favorites"))
                checkFavorites = true;
            ArrayList<Song> playlistSongs = allPlaylists.get(i).getPlaylistSongs();
            for(int j = 0; j < playlistSongs.size(); j++) {
                if(tempMap.get(String.valueOf(playlistSongs.get(j).getID())) != null) {
                    playlistSongs.set(j, tempMap.get(String.valueOf(playlistSongs.get(j).getID())));
                }
            }
            playlistList.put(playListName, playlistSongs);
        }
        if(!checkFavorites) {
            dbHelper.addPlaylist("Favorites");
            playlistList.put("Favorites", new ArrayList<>());
        }
        playlists.clear();
        playlists.addAll(playlistList.keySet());
        Collections.sort(playlists, (o1, o2) -> o1.compareTo(o2));
        updateNums();
        notifyDataSetChanged();
    }

    public void setSongList(ArrayList<Song> sList) {
        songList = sList;
    }
}
