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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import sage.musicplayer.ListAdapter.AlbumAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.R;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Util.UIUtil.BottomUpAnimController;

public class AlbumsTab extends Fragment {

    private MusicUtils musicUtils;
    private ArrayList<Album> albumList;
    private RecyclerView gridRecyclerView;
    private AlbumAdapter albumAdapter;
    JollyUtils jollyUtils;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageButton shuffleButton;

    public BroadcastReceiver readPermGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setAlbumList();
        }
    };

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);

            for(int i = 0; i < albumList.size(); i++) {
                ArrayList<Song> temp = albumList.get(i).getAlbumSongs();
                Album a = albumList.get(i);
                int pos = musicUtils.getPositionInList(song_id, temp);
                while (pos != -1) {
                    temp.remove(pos);
                    pos = musicUtils.getPositionInList(song_id, temp);
                }
                if(temp.size() <= 0) {
                    albumList.remove(i);
                    albumAdapter.notifyItemRemoved(i);
                    albumAdapter.notifyItemRangeChanged(i, albumList.size());
                    break;
                }else{
                    a.setAlbumSongs(temp);
                    albumList.set(i, a);
                    albumAdapter.notifyItemChanged(i);
                }

            }
        }
    };

    public AlbumsTab() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jollyUtils = new JollyUtils(getContext());
        musicUtils  = new MusicUtils(getContext().getContentResolver(), getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_albums_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumList = new ArrayList<>();

        gridRecyclerView = view.findViewById(R.id.albumRecyclerView);
        albumAdapter = new AlbumAdapter(getContext(), getActivity(), albumList);
        RecyclerView.LayoutManager manager = new GridLayoutManager(getContext(), 2);
        gridRecyclerView.setLayoutManager(manager);
        gridRecyclerView.setAdapter(albumAdapter);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        BottomUpAnimController controller = new BottomUpAnimController(animation);
        gridRecyclerView.setLayoutAnimation(controller);
        gridRecyclerView.startLayoutAnimation();

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    ((MainActivity)getActivity()).requestReadStorage();
                } else {
                    // Permission has already been granted
                    setAlbumList();
                }
            }
        });

        shuffleButton = view.findViewById(R.id.album_shuffle_button);
        shuffleButton.setOnClickListener(view13 -> {
            Random rand = new Random();
            ArrayList<Song> tempSongs = new ArrayList<>();

            if(albumList.size() > 0) {
                Album album = albumList.get(rand.nextInt(albumList.size()));
                if(album.getAlbumSongs().size() > 0)
                    tempSongs.addAll(album.getAlbumSongs());
            }
            if(tempSongs.size() > 0) {
                ((MainActivity)getActivity()).getMusicSrv().setList(tempSongs);
                ((MainActivity)getActivity()).getMusicSrv().toggleShuffle(true);
                ((MainActivity) getActivity()).songPicked(rand.nextInt(tempSongs.size()));
            }else {
                Toast.makeText(getActivity(), R.string.no_songs, Toast.LENGTH_SHORT).show();
            }
        });

        gridRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scrolling up
                    shuffleButton.animate().alpha(0).setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    shuffleButton.setVisibility(View.GONE);
                                }
                            }).start();
                } else {
                    // Scrolling down
                    shuffleButton.animate().alpha(1).setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    shuffleButton.setVisibility(View.VISIBLE);
                                }
                            }).start();
                }
            }
        });

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(readPermGrantedReceiver, new IntentFilter("readPermGranted"));

        setAlbumList();
    }

    public void setAlbumList() {
        class MyTask extends AsyncTask<Void, Void, String> {

            ArrayList<Song> sList;
            ArrayList<Album> albList;

            @Override
            protected String doInBackground(Void... voids) {
                sList = new ArrayList<>();
                albList = new ArrayList<>();

                if(getActivity() != null) {
                    sList = musicUtils.getSongList(getActivity().getApplicationContext());
                    albList = musicUtils.getAlbumList(sList);
                }
                Collections.sort(albumList, new Comparator<Album>() {
                    @Override
                    public int compare(Album album, Album album2) {
                        return album.getName().compareTo(album2.getName());
                    }
                });

                albumList = albList;
                albumAdapter.setAlbumList(albumList);
                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                albumAdapter.notifyDataSetChanged();
                gridRecyclerView.startLayoutAnimation();
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
