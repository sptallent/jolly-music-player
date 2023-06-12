package sage.musicplayer.ListAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

import sage.musicplayer.R;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.MusicUtil.Playlist;

public class PlaylistAddNamesAdapter extends BaseAdapter {

    Context context;
    ArrayList<Playlist> playlists;
    LayoutInflater playNamesInf;
    TextView playlistAddNameText;
    DatabaseHelper dbHelper;
    long[] song_id_list;
    PopupWindow popupWindow;

    public PlaylistAddNamesAdapter(Context c, ArrayList<Playlist> pList, long[] sList, PopupWindow pl_popup) {
        context = c;
        playlists = pList;
        song_id_list = sList;
        popupWindow = pl_popup;
        playNamesInf = LayoutInflater.from(pl_popup.getContentView().getContext());
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return playlists.size();
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
        LinearLayout playNameLay = (LinearLayout) playNamesInf.inflate(R.layout.playlist_name, parent, false);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        playNameLay.setLayoutParams(params);
        playlistAddNameText = playNameLay.findViewById(R.id. playlistAddNameText);
        playlistAddNameText.setText(playlists.get(position).getPlaylistName());

        playlistAddNameText.setOnClickListener(v -> {
            for(long l : song_id_list)
                dbHelper.addSongToPlaylist((int)playlists.get(position).getId(), (int)l);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("playlistChanged"));
            popupWindow.dismiss();
        });

        return playNameLay;
    }
}
