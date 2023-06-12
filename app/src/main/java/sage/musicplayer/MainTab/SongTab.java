package sage.musicplayer.MainTab;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import sage.musicplayer.Interface.ClickListener;
import sage.musicplayer.ListAdapter.PlaylistAddNamesAdapter;
import sage.musicplayer.ListAdapter.SongAdapter;
import sage.musicplayer.MainActivity;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.AcoustAPIHelper;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.MusicInfo;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.BottomUpAnimController;
import sage.musicplayer.Util.UIUtil.CircleProgressBar;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class SongTab extends Fragment implements ClickListener {

    private RecyclerView songRecyclerView;
    private SongAdapter songAdapter;
    private ArrayList<Song> songList;
    private ArrayList<Song> querySongList;
    private MusicService musicService;
    private MusicUtils musicUtils;

    private SearchView mainSearchView;
    private LinearLayout manipulationMenu;
    private ImageButton searchButton;
    private ImageButton sortButton;
    private ImageButton fingerprintButton;
    private ImageButton shuffleButton;
    private ImageButton closeMenuButton;
    private boolean menuOpen = true;
    AcoustAPIHelper acoustAPIHelper;
    private PopupWindow popupWindow;

    private ImageView fp_album;
    private ProgressBar fp_progress;
    private CircleProgressBar fp_button_progress;
    private TextView fp_song;
    private TextView fp_artist;
    private TextView fp_album_title;
    private TextView fp_track_num;
    private TextView fp_progress_text;
    private TextView fp_genre;
    private TextView fp_release_date;
    private TextView fp_file_name;
    private Button fp_cancel_button;
    private DrawableCrossFadeFactory factory = new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    DatabaseHelper dbHelper;

    SwipeRefreshLayout swipeRefreshLayout;

    View popupView;

    Context context;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;
    JollyUtils jollyUtils;

    public BroadcastReceiver readPermGrantedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setList();
        }
    };

    private BroadcastReceiver songViewDeletedReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long song_id = intent.getLongExtra("song_id", -1);
            songList = musicUtils.removeSongFromList((int)song_id, songList);
        }
    };

    private BroadcastReceiver scanFingerprintReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long l = intent.getLongExtra("song_id", -1);
            Song s = musicUtils.getSong(l);
            ArrayList<Song> t = new ArrayList<>();
            t.add(s);
            scanFingerprint(t);
        }
    };

    private BroadcastReceiver fpFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int count = bundle.getInt("count");
            int count_max = bundle.getInt("count_max");
            MusicInfo mi = (MusicInfo) bundle.getSerializable("mi");
            if(mi != null && getContext() != null) {
                String song_title = mi.getSong_title();
                String artist_name = mi.getArtist_name();
                String album_name = mi.getAlbum_title();
                String track_num = mi.getTrack_number();
                String track_count = mi.getAlbum_track_count();
                String release_date = mi.getRelease_date();
                String genre = mi.getGenre();
                long id = mi.getLocal_id();
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                String file_name = mi.getPath();//new File(uri.getPath()).getAbsolutePath();

                if (file_name != null && !file_name.isEmpty())
                    fp_file_name.setText(file_name);
                else
                    fp_file_name.setText("");

                if (count != count_max) {
                    if(fp_progress_text != null)
                        fp_progress_text.setText(count + " " + getContext().getResources().getString(R.string.slash) + " " + count_max);
                    if (fp_cancel_button != null && fp_cancel_button.getText() != getResources().getString(R.string.cancel_scan))
                        fp_cancel_button.setText(getResources().getString(R.string.cancel_scan));
                } else {
                    fp_progress_text.setText(getResources().getString(R.string.done));
                    fp_cancel_button.setText(getResources().getString(R.string.close));

                    fp_button_progress.setProgress(0);
                    fp_button_progress.setMax(100);
                    fp_button_progress.setVisibility(View.GONE);
                }
                if (song_title != null && !song_title.equals(""))
                    fp_song.setText(song_title);
                else
                    fp_song.setText(getResources().getString(R.string.not_found));
                if (artist_name != null && !artist_name.equals(""))
                    fp_artist.setText(artist_name);
                else
                    fp_artist.setText(getResources().getString(R.string.not_found));
                if (album_name != null && !album_name.equals(""))
                    fp_album_title.setText(album_name);
                else
                    fp_album_title.setText(getResources().getString(R.string.not_found));
                if (track_num != null && !track_num.equals("") && !track_count.equals("") && !track_num.equals("0") && !track_count.equals("0"))
                    fp_track_num.setText(track_num + " " + getString(R.string.slash) + " " + track_count);
                else
                    fp_track_num.setText(getResources().getString(R.string.not_found));
                if (genre != null && !genre.equals(""))
                    fp_genre.setText(genre);
                else
                    fp_genre.setText(getResources().getString(R.string.not_found));
                if (release_date != null && !release_date.equals(""))
                    fp_release_date.setText(release_date);
                else
                    fp_release_date.setText(getResources().getString(R.string.not_found));
                if (mi.getAlbum_art() != null)
                    Glide.with(context).load(mi.getAlbum_art()).transition(withCrossFade(2000)).error(R.drawable.new_album_art).into(fp_album);
                else
                    Glide.with(context).load(R.drawable.new_album_art).transition(withCrossFade(2000)).into(fp_album);
                if (count_max != fp_progress.getProgress()) {
                    fp_progress.setMax(count_max);
                    fp_button_progress.setMax(count_max);
                }
                fp_progress.setProgress(count);
                fp_button_progress.setProgress(count);
            }else{
                fp_progress.setMax(count_max);
                fp_progress.setProgress(count_max);

                try {
                    fp_progress_text.setText(getResources().getString(R.string.done));
                }catch(IllegalStateException e) {
                    e.printStackTrace();
                }
                fp_cancel_button = popupView.findViewById(R.id.fp_cancel_button);
                fp_cancel_button.setText(context.getString(R.string.cancel));

                fp_button_progress.setProgress(count_max);
                fp_button_progress.setMax(count_max);
                fp_button_progress.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();
        //LocalBroadcastManager.getInstance(getContext()).registerReceiver(songViewDeletedReciever, new IntentFilter("songViewDeleted"));
        musicUtils = new MusicUtils(getContext().getContentResolver(), getContext());
        jollyUtils = new JollyUtils(getContext());

        songList = ((MainActivity) getActivity()).getMySongList();
        querySongList = new ArrayList<>();
        musicService = ((MainActivity) getActivity()).getMusicSrv();

        songRecyclerView = getView().findViewById(R.id.song_list);
        //songRecyclerView.setHasFixedSize(true);
        //songRecyclerView.setItemViewCacheSize(25);
        //songRecyclerView.setDrawingCacheEnabled(true);
        //songRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        songAdapter = new SongAdapter(getContext(), getActivity(), songList, R.layout.song, musicService, this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        songRecyclerView.setLayoutManager(layoutManager);
        songRecyclerView.setAdapter(songAdapter);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
        BottomUpAnimController controller = new BottomUpAnimController(animation);
        songRecyclerView.setLayoutAnimation(controller);
        songRecyclerView.startLayoutAnimation();

        mainSearchView = getView().findViewById(R.id.main_search_view);
        mainSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")) {
                    songAdapter.setSongs(songList);
                } else {
                    for (Song s : songList) {
                        String title = s.getTitle();
                        String artist = s.getArtist();
                        for (String j : newText.split(" ")) {
                            if (title.toUpperCase().contains(j.toUpperCase()) || artist.toUpperCase().contains(j.toUpperCase())) {
                                querySongList.add(s);
                            }
                        }
                    }
                    songAdapter.setSongs(new ArrayList<>(querySongList));
                }
                songAdapter.notifyDataSetChanged();
                querySongList.clear();
                return false;
            }

        });
        mainSearchView.setOnCloseListener(() -> {
            mainSearchView.setVisibility(View.GONE);
            return false;
        });

        manipulationMenu = getView().findViewById(R.id.manipulation_menu);
        closeMenuButton = getView().findViewById(R.id.song_tab_close);
        sortButton = getView().findViewById(R.id.song_tab_sort);
        fingerprintButton = getView().findViewById(R.id.song_tab_fingerprint);
        searchButton = getView().findViewById(R.id.song_tab_search);
        shuffleButton = getView().findViewById(R.id.song_tab_shuffle);

        songRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    // Scrolling up
                    manipulationMenu.animate()
                            .alpha(0)
                            .setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    manipulationMenu.setVisibility(View.GONE);
                                }
                            })
                            .start();
                } else {
                    // Scrolling down
                    manipulationMenu.animate().alpha(1).setDuration(500)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    manipulationMenu.setVisibility(View.VISIBLE);
                                }
                            })
                            .start();
                }
            }
        });

        closeMenuButton.setOnClickListener(view1 -> {
            float searchEndX, sortEndX, fingerprintEndX, shuffleEndX, fpProgressEndX;
            int animationDuration = 500;
            if (!closeMenuButton.isEnabled()) {
                // Animation is already running, ignore this click
                return;
            }
            closeMenuButton.setEnabled(false);
            if (menuOpen) {
                closeMenuButton.animate().rotationBy(45f).setDuration(animationDuration).start();
                searchEndX = closeMenuButton.getX() - searchButton.getX();
                sortEndX = closeMenuButton.getX() - sortButton.getX();
                fingerprintEndX = closeMenuButton.getX() - fingerprintButton.getX();
                shuffleEndX = closeMenuButton.getX() - shuffleButton.getX();
                fpProgressEndX = closeMenuButton.getX() - fp_button_progress.getX();
                menuOpen = false;
            } else {
                closeMenuButton.animate().rotationBy(-45f).setDuration(animationDuration).start();
                searchEndX = searchButton.getX() - closeMenuButton.getX();
                sortEndX = sortButton.getX() - closeMenuButton.getX();
                fingerprintEndX = fingerprintButton.getX() - closeMenuButton.getX();
                shuffleEndX = shuffleButton.getX() - closeMenuButton.getX();
                fpProgressEndX = fp_button_progress.getX() - closeMenuButton.getX();
                menuOpen = true;
            }
            searchButton.animate().translationX(searchEndX).setDuration(animationDuration)
                    .withEndAction(() -> closeMenuButton.setEnabled(true)).start();
            sortButton.animate().translationX(sortEndX).setDuration(animationDuration).start();
            fingerprintButton.animate().translationX(fingerprintEndX).setDuration(animationDuration).start();
            shuffleButton.animate().translationX(shuffleEndX).setDuration(animationDuration).start();
            fp_button_progress.animate().translationX(fpProgressEndX).setDuration(animationDuration).start();
        });

        shuffleButton.setOnClickListener(view13 -> {
            Random rand = new Random();
            if(songAdapter.getSongs().size() > 0) {
                ((MainActivity)getActivity()).getMusicSrv().setList(songAdapter.getSongs());
                ((MainActivity)getActivity()).getMusicSrv().toggleShuffle(true);
                ((MainActivity) getActivity()).songPicked(rand.nextInt(songAdapter.getSongs().size()));
            }else {
                Toast.makeText(getActivity(), R.string.no_songs, Toast.LENGTH_SHORT).show();
            }
        });

        sortButton.setOnClickListener(view12 -> {
            Context wrapper = new ContextThemeWrapper(getContext(), R.style.popupMenuStyle);
            PopupMenu sortMenu = new PopupMenu(wrapper, view12);
            sortMenu.getMenuInflater().inflate(R.menu.popup_menu_sort, sortMenu.getMenu());
            sortMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.popup_sort_title_asc:
                        Collections.sort(songList, (song, song2) -> song.getTitle().compareTo(song2.getTitle()));
                        Collections.reverse(songList);
                        songAdapter.notifyDataSetChanged();
                        break;
                    case R.id.popup_sort_title_desc:
                        Collections.sort(songList, (song, song2) -> song.getTitle().compareTo(song2.getTitle()));
                        songAdapter.notifyDataSetChanged();
                        break;
                    case R.id.popup_sort_date_asc:
                        Collections.sort(songList, (song, song2) -> {
                            if (Integer.parseInt(song.getDateAdded()) == Integer.parseInt(song2.getDateAdded()))
                                return 0;
                            else if (Integer.parseInt(song.getDateAdded()) < Integer.parseInt(song2.getDateAdded()))
                                return -1;
                            else
                                return 1;
                        });
                        songAdapter.notifyDataSetChanged();
                        break;
                    case R.id.popup_sort_date_desc:
                        Collections.sort(songList, (song, song2) -> {
                            if (Integer.parseInt(song.getDateAdded()) == Integer.parseInt(song2.getDateAdded()))
                                return 0;
                            else if (Integer.parseInt(song.getDateAdded()) < Integer.parseInt(song2.getDateAdded()))
                                return -1;
                            else
                                return 1;
                        });
                        Collections.reverse(songList);
                        songAdapter.notifyDataSetChanged();
                        break;
                    case R.id.popup_sort_dur_longest:
                        Collections.sort(songList, new Comparator<Song>() {
                            @Override
                            public int compare(Song song, Song song2) {
                                if (Integer.parseInt(song.getDuration()) == Integer.parseInt(song2.getDuration()))
                                    return 0;
                                else if (Integer.parseInt(song.getDuration()) < Integer.parseInt(song2.getDuration()))
                                    return -1;
                                else
                                    return 1;
                            }
                        });
                        Collections.reverse(songList);
                        songAdapter.notifyDataSetChanged();
                        break;
                    case R.id.popup_sort_dur_short:
                        Collections.sort(songList, new Comparator<Song>() {
                            @Override
                            public int compare(Song song, Song song2) {
                                if (Integer.parseInt(song.getDuration()) == Integer.parseInt(song2.getDuration()))
                                    return 0;
                                else if (Integer.parseInt(song.getDuration()) < Integer.parseInt(song2.getDuration()))
                                    return -1;
                                else
                                    return 1;
                            }
                        });
                        songAdapter.notifyDataSetChanged();
                        break;
                }
                return true;
            });
            sortMenu.show();
        });
        searchButton.setOnClickListener(view13 -> {
            if (mainSearchView.getVisibility() == View.GONE) {
                mainSearchView.setIconifiedByDefault(true);
                mainSearchView.setFocusable(true);
                mainSearchView.setIconified(false);
                mainSearchView.requestFocusFromTouch();
                mainSearchView.setVisibility(View.VISIBLE);
            } else {
                mainSearchView.setIconifiedByDefault(false);
                mainSearchView.setFocusable(false);
                mainSearchView.setIconified(true);
                mainSearchView.setVisibility(View.GONE);
            }
        });

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.fp_popup, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        popupWindow = new PopupWindow(popupView, width, height, true);

        fp_album = popupView.findViewById(R.id.fp_album);
        fp_artist = popupView.findViewById(R.id.fp_artist);
        fp_song = popupView.findViewById(R.id.fp_song);
        fp_album_title = popupView.findViewById(R.id.fp_album_title);
        fp_track_num = popupView.findViewById(R.id.fp_track_num);
        fp_genre = popupView.findViewById(R.id.fp_genre);
        fp_progress = popupView.findViewById(R.id.fp_progress);
        fp_progress_text = popupView.findViewById(R.id.fp_progress_text);
        fp_release_date = popupView.findViewById(R.id.fp_release_date);
        fp_file_name = popupView.findViewById(R.id.fp_file_name);
        fp_cancel_button = popupView.findViewById(R.id.fp_cancel_button);

        fp_cancel_button.setOnClickListener(v -> {
            acoustAPIHelper.stop();
            acoustAPIHelper = null;
            popupWindow.dismiss();
            fp_progress.setProgress(0);
            fp_button_progress.setProgress(0);
            fp_button_progress.setVisibility(View.GONE);
            fp_artist.setText("");
            fp_song.setText("");
            fp_album.setImageResource(R.drawable.new_album_art);
            fp_album_title.setText("");
            fp_track_num.setText("");
            fp_file_name.setText("");
            fp_release_date.setText("");
            fp_genre.setText("");
            jollyUtils.sendLocalBroadcast(new Intent("readPermGranted"));
        });

        fingerprintButton.setOnClickListener(view14 -> {
            if(songList.size() > 0) {
                if (acoustAPIHelper == null || !acoustAPIHelper.isRunning()) {
                    acoustAPIHelper = new AcoustAPIHelper();
                    ArrayList<Song> acoustSongList = new ArrayList<>(songList);
                    acoustAPIHelper.getSongInfo(getContext(), acoustSongList);
                    fp_button_progress.setVisibility(View.VISIBLE);
                    fp_button_progress.bringToFront();
                }
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }else{
                Toast.makeText(getActivity(), R.string.read_perm_require,
                        Toast.LENGTH_SHORT).show();
            }
        });

        fp_button_progress = getActivity().findViewById(R.id.fp_button_progress);
        fp_button_progress.setColor(ContextCompat.getColor(getContext(), (R.color.colorSecondaryAccent)));
        fp_button_progress.setBackgroundColor(ContextCompat.getColor(getContext(), (R.color.transparent)));
        fp_button_progress.bringToFront();

        dbHelper = new DatabaseHelper(getContext());

        IntentFilter intentFilter = new IntentFilter("fpFound");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(fpFoundReceiver, intentFilter);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(scanFingerprintReciever, new IntentFilter("scanFingerprint"));
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


                songList = sList;
                songAdapter.setSongs(songList);
                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                songAdapter.notifyDataSetChanged();
                songRecyclerView.startLayoutAnimation();
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

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            MainActivity act = ((MainActivity) getActivity());
            act.getMusicSrv().setList(songAdapter.getSongs());
            act.getMusicSrv().updateAll();
            act.songPicked(position);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        if(actionMode == null)
            actionMode = getActivity().startActionMode(actionModeCallback);
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        songAdapter.toggleSelection(position);
        int count = songAdapter.getSelectedItemCount();

        if(count == 0) {
            actionMode.finish();
        }else{
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.popup_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            ArrayList<Song> tempSongs = new ArrayList<>();
            long[] tempIDs;
            List<Integer> itemsSelected = songAdapter.getSelected();
            tempIDs = new long[itemsSelected.size()];
            for(int i = 0; i < itemsSelected.size(); i++) {
                tempSongs.add(songAdapter.getSongs().get(itemsSelected.get(i)));//songList
                tempIDs[i] = songAdapter.getSongs().get(itemsSelected.get(i)).getID();//songList
            }

            switch (item.getItemId()) {
                case R.id.popup_play:
                    ((MainActivity)getActivity()).getMusicSrv().setList(tempSongs);
                    ((MainActivity)getActivity()).songPicked(0);
                    ((MainActivity)getActivity()).getMusicSrv().updateAll();
                    break;
                case R.id.popup_play_shuffle:
                    Collections.shuffle(tempSongs);
                    ((MainActivity)getActivity()).getMusicSrv().setList(tempSongs);
                    ((MainActivity)getActivity()).getMusicSrv().toggleShuffle(true);
                    ((MainActivity)getActivity()).songPicked(0);
                    ((MainActivity)getActivity()).getMusicSrv().updateAll();
                    break;
                case R.id.popup_delete:
                    long[] del_songs = new long[tempSongs.size()];
                    for(int i = 0; i < tempSongs.size(); i++)
                        del_songs[i] = tempSongs.get(i).getID();
                    musicUtils.deleteTracks(getContext(), getActivity(), del_songs, ((MainActivity)getActivity()).getMusicSrv());
                    break;
                case R.id.popup_play_next:
                    for(Integer i : itemsSelected)
                        playNextAdd(songList.get(i));
                    break;
                case R.id.popup_add_playlist:
                    showPlaylists(tempIDs);
                    break;
                case R.id.popup_scan_fingerprint:
                    scanFingerprint(tempSongs);
                    break;
                default:
                    return false;
            }
            mode.finish();
            return true;
        }

        public void playNextAdd(Song song_obj) {
            Intent intent = new Intent("addPlayNext");
            intent.putExtra("song_obj", (Serializable)song_obj);
            sendLocalBroadcast(intent);
        }

        public void showPlaylists(long[] song_ids) {
            PopupWindow plAvailablePopupWindow;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View availablePlaylistView = inflater.inflate(R.layout.pl_display_names, null);
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = 300;
            plAvailablePopupWindow = new PopupWindow(availablePlaylistView, width, height, true);

            ArrayList<Playlist> pList = dbHelper.getAllPlaylists();

            ListView playlistAddNames = plAvailablePopupWindow.getContentView().findViewById(R.id.playlistAddNames);
            PlaylistAddNamesAdapter addNamesAdapter = new PlaylistAddNamesAdapter(getContext(), pList, song_ids, plAvailablePopupWindow);
            playlistAddNames.setAdapter(addNamesAdapter);

            plAvailablePopupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            songAdapter.clearSelection();
            actionMode = null;
        }

        public void sendLocalBroadcast(Intent intent) {
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }
    }

    public void scanFingerprint(ArrayList<Song> temp) {
        if(acoustAPIHelper == null || !acoustAPIHelper.isRunning()) {
            acoustAPIHelper = new AcoustAPIHelper();
            ArrayList<Song> acoustSongList = new ArrayList<>(temp);
            acoustAPIHelper.getSongInfo(getContext(), acoustSongList);
            fp_button_progress.setVisibility(View.VISIBLE);
        }
        popupWindow.showAtLocation(getView(), Gravity.CENTER, 0, 0);
    }

}


