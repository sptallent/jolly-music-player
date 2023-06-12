package sage.musicplayer.MainTab;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.appcompat.widget.PopupMenu;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import sage.musicplayer.ListAdapter.PlaylistAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.BottomUpAnimController;

public class PlaylistTab extends Fragment {

    DatabaseHelper dbHelper;
    PopupWindow plPopupWindow;
    ArrayList<String> playlistNames;
    ImageButton playlistAdd;
    TextInputEditText plNameInput;
    Button plAddButton;
    ListView playlistListView;
    PlaylistAdapter playlistAdapter;
    HashMap<String, ArrayList<Song>> playlistMap;
    MusicUtils musicUtils;

    JollyUtils jollyUtils;
    SwipeRefreshLayout swipeRefreshLayout;

    public BroadcastReceiver readPermGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setList();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playlistAdd = view.findViewById(R.id.playlist_add);

        playlistMap = new HashMap<>();
        playlistNames = new ArrayList<>();
        dbHelper = new DatabaseHelper(getContext());
        musicUtils = new MusicUtils(getContext().getContentResolver(), getContext());
        jollyUtils = new JollyUtils(getContext());

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pl_add_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        plPopupWindow = new PopupWindow(popupView, width, height, true);

        playlistListView = view.findViewById(R.id.playlistListView);
        playlistAdapter = new PlaylistAdapter(getContext(), getActivity(),playlistMap, playlistNames);
        playlistListView.setAdapter(playlistAdapter);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        BottomUpAnimController controller = new BottomUpAnimController(animation);
        playlistListView.setLayoutAnimation(controller);
        playlistListView.startLayoutAnimation();

        playlistAdd.setOnClickListener(view1 -> {
            if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                plPopupWindow.showAtLocation(getView(), Gravity.BOTTOM, 0, 0);

                plNameInput = plPopupWindow.getContentView().findViewById(R.id.pl_name_input);
                plAddButton = plPopupWindow.getContentView().findViewById(R.id.pl_create_button);

                plAddButton.setOnClickListener(view2 -> {
                    dbHelper.addPlaylist(plNameInput.getText().toString());
                    playlistAdapter.updatePlaylists();
                    plPopupWindow.dismiss();
                });
            }else {
                Toast.makeText(getActivity(), R.string.read_perm_require,
                        Toast.LENGTH_SHORT).show();
            }
        });

        playlistListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int mLastFirstVisibleItem = 0;
            private boolean mIsScrollingUp = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0) {
                    if (firstVisibleItem > mLastFirstVisibleItem) {
                        mIsScrollingUp = false;
                        playlistAdd.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                playlistAdd.setVisibility(View.GONE);
                            }
                        }).start();
                    } else if (firstVisibleItem < mLastFirstVisibleItem) {
                        mIsScrollingUp = true;
                        playlistAdd.animate().alpha(1).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                playlistAdd.setVisibility(View.VISIBLE);
                            }
                        }).start();
                    }

                    mLastFirstVisibleItem = firstVisibleItem;
                }
            }

        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(readPermGrantedReceiver, new IntentFilter("readPermGranted"));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ((MainActivity)getActivity()).requestReadStorage();
                } else {
                    // Permission has already been granted
                    setList();
                }
            }
        });
        setList();
    }

    public void setList() {
        class MyTask extends AsyncTask<Void, Void, String> {

            ArrayList<Song> sList;

            @Override
            protected String doInBackground(Void... voids) {
                sList = new ArrayList<>();

                if(getActivity() != null) {
                    sList = musicUtils.getSongList(getActivity().getApplicationContext());
                    Collections.sort(sList, (a, b) -> a.getTitle().compareTo(b.getTitle()));
                }

                if(sList.size() > 0) {
                    playlistAdapter.setSongList(sList);
                }
                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                playlistAdapter.updatePlaylists();
                playlistListView.startLayoutAnimation();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        // Use the following code to start the task
        new MyTask().execute();
    }

}
