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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import sage.musicplayer.ListAdapter.GenreAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.BottomUpAnimController;

public class GenreTab extends Fragment {

    ListView genreListView;
    GenreAdapter genreAdapter;
    HashMap<String, ArrayList<Song>> genreList;
    ArrayList<String> genreNames;
    MusicUtils musicUtils;
    JollyUtils jollyUtils;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton shuffleButton;

    public BroadcastReceiver readPermGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setList();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_genre_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicUtils = new MusicUtils(getContext().getContentResolver(), getContext());
        jollyUtils = new JollyUtils(getContext());

        genreListView = view.findViewById(R.id.genreListView);
        genreList = new HashMap<>();
        genreNames = new ArrayList<>();
        genreAdapter = new GenreAdapter(getContext(), getActivity(), genreList, genreNames);
        genreListView.setAdapter(genreAdapter);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        BottomUpAnimController controller = new BottomUpAnimController(animation);
        genreListView.setLayoutAnimation(controller);
        genreListView.startLayoutAnimation();

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

        shuffleButton = view.findViewById(R.id.genre_shuffle_button);
        shuffleButton.setOnClickListener(view13 -> {
            Random rand = new Random();
            ArrayList<Song> tempSongs = new ArrayList<>();

            if(genreList.keySet().size() > 0) {
                List<String> keysAsArray = new ArrayList<>(genreList.keySet());
                String key = keysAsArray.get(rand.nextInt(keysAsArray.size()));
                if (key != null && genreList.get(key) != null)
                    tempSongs.addAll(genreList.get(key));
            }
            if(tempSongs.size() > 0) {
                ((MainActivity)getActivity()).getMusicSrv().setList(tempSongs);
                ((MainActivity)getActivity()).getMusicSrv().toggleShuffle(true);
                ((MainActivity) getActivity()).songPicked(rand.nextInt(tempSongs.size()));
            }else {
                Toast.makeText(getActivity(), R.string.no_songs, Toast.LENGTH_SHORT).show();
            }
        });

        genreListView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
                        shuffleButton.animate().alpha(0).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                shuffleButton.setVisibility(View.GONE);
                            }
                        }).start();
                    } else if (firstVisibleItem < mLastFirstVisibleItem) {
                        mIsScrollingUp = true;
                        shuffleButton.animate().alpha(1).setDuration(500).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                shuffleButton.setVisibility(View.VISIBLE);
                            }
                        }).start();
                    }

                    mLastFirstVisibleItem = firstVisibleItem;
                }
            }

        });

        setList();
    }


    public void setList() {
        class MyTask extends AsyncTask<Void, Void, String> {

            ArrayList<Song> sList;
            HashMap<String, ArrayList<Song>> genList;

            @Override
            protected String doInBackground(Void... voids) {
                sList = new ArrayList<>();
                genList = new HashMap<>();

                if(getActivity() != null) {
                    sList = musicUtils.getSongList(getActivity().getApplicationContext());
                    genList = musicUtils.getGenreList(sList);
                }
                Collections.sort(sList, (a, b) -> a.getTitle().compareTo(b.getTitle()));

                genreList = genList;
                genreNames.clear();
                genreNames.addAll(genreList.keySet());
                Collections.sort(genreNames, (s, s2) -> s.compareTo(s2));
                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                genreAdapter.setLists(genreList, genreNames);
                genreAdapter.notifyDataSetChanged();
                genreListView.startLayoutAnimation();
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
