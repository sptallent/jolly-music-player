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
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import sage.musicplayer.ListAdapter.ArtistAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.BottomUpAnimController;

public class ArtistTab extends Fragment {

    TreeMap<String, ArrayList<Song>> artistMap;
    ListView artistListView;
    JollyUtils jollyUtils;
    MusicUtils musicUtils;
    ArtistAdapter artistAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton shuffleButton;

    public BroadcastReceiver readPermGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLists();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        musicUtils = new MusicUtils(getContext().getContentResolver(), getContext());
        jollyUtils = new JollyUtils(getContext());

        artistMap = new TreeMap<>();

        artistListView = getView().findViewById(R.id.artistListView);
        artistAdapter = new ArtistAdapter(getContext(), getActivity(), artistMap);
        artistListView.setAdapter(artistAdapter);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        BottomUpAnimController controller = new BottomUpAnimController(animation);
        artistListView.setLayoutAnimation(controller);
        artistListView.startLayoutAnimation();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ((MainActivity)getActivity()).requestReadStorage();
                } else {
                    // Permission has already been granted
                    setLists();
                }
            }
        });

        shuffleButton = view.findViewById(R.id.artist_shuffle_button);
        shuffleButton.setOnClickListener(view13 -> {
            Random rand = new Random();
            ArrayList<Song> tempSongs = new ArrayList<>();

            if(artistMap.keySet().size() > 0) {
                List<String> keysAsArray = new ArrayList<>(artistMap.keySet());
                String key = keysAsArray.get(rand.nextInt(keysAsArray.size()));
                if (key != null && artistMap.get(key) != null)
                    tempSongs.addAll(artistMap.get(key));
            }
            if(tempSongs.size() > 0) {
                ((MainActivity)getActivity()).getMusicSrv().setList(tempSongs);
                ((MainActivity)getActivity()).getMusicSrv().toggleShuffle(true);
                ((MainActivity) getActivity()).songPicked(rand.nextInt(tempSongs.size()));
            }else {
                Toast.makeText(getActivity(), R.string.no_songs, Toast.LENGTH_SHORT).show();
            }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(readPermGrantedReceiver, new IntentFilter("readPermGranted"));

        artistListView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
                        shuffleButton.animate().alpha(0).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
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

        setLists();
    }

    public ArrayList<String> getArtistsNames(TreeMap<String, ArrayList<Song>> artMap) {
        ArrayList<String> artistNames = new ArrayList<String>();

        for(String s : artMap.keySet()) {
            artistNames.add(s);
        }

        return artistNames;
    }

    public void setLists() {
        class MyTask extends AsyncTask<Void, Void, String> {

            ArrayList<Song> sList;
            TreeMap<String, ArrayList<Song>> artList;
            ArrayList<Album> albList;

            @Override
            protected String doInBackground(Void... voids) {
                sList = new ArrayList<>();
                artList = new TreeMap<>();
                albList = new ArrayList<>();

                if(getActivity() != null) {
                    sList = musicUtils.getSongList(getActivity().getApplicationContext());
                    artList = musicUtils.getArtists(sList);
                    albList = musicUtils.getAlbumList(sList);
                }
                Collections.sort(sList, (a, b) -> a.getTitle().compareTo(b.getTitle()));

                artistMap = artList;
                artistAdapter.setArtistHashMap(artistMap);
                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                artistAdapter.notifyDataSetChanged();
                artistListView.startLayoutAnimation();
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
