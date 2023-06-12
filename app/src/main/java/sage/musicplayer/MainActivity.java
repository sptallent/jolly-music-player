package sage.musicplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.os.HandlerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.LazyBitmapDrawableResource;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.savantech.seekarc.SeekArc;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.glide.transformations.BlurTransformation;
import sage.musicplayer.ListAdapter.AlbumViewAdapter;
import sage.musicplayer.ListAdapter.ArtistAlbumViewAdapter;
import sage.musicplayer.ListAdapter.PlaylistViewAdapter;
import sage.musicplayer.Util.AnimationHelper;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Interface.OnSongClickedListener;
import sage.musicplayer.ListAdapter.ArtistSongAdapter;
import sage.musicplayer.ListAdapter.GenreViewAdapter;
import sage.musicplayer.ListAdapter.SongQueueAdapter;
import sage.musicplayer.MainTab.AlbumsTab;
import sage.musicplayer.MainTab.ArtistTab;
import sage.musicplayer.MainTab.GenreTab;
import sage.musicplayer.MainTab.PlaylistTab;
import sage.musicplayer.MainTab.SettingsTab;
import sage.musicplayer.MainTab.SongTab;
import sage.musicplayer.Util.DrawableToBitmapConverter;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.Album;
import sage.musicplayer.Util.MusicUtil.JollyEqualizer;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;
import sage.musicplayer.Service.MusicController;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.UIUtil.BitmapUtils;
import sage.musicplayer.Util.UIUtil.BubbleLayout;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    SmartTabLayout tabs;
    ViewPager pager;
    FragmentPagerItemAdapter adapter;
    RecyclerView songListView;
    ImageButton playPauseButton;
    TextView totalSongs;
    TextView totalSongsText;
    TextView songViewTitleText;
    ImageView albumArtImage;
    SeekArc seekArc;
    TextView curSeekDur;
    TextView maxSeekDur;
    TextView nowPlayingArtist;
    TextView nowPlayingAlbum;
    ImageView cross;
    ConstraintLayout songView;
    ConstraintLayout songDisplay;
    private ArrayList<Song> songList;
    private TreeMap<String, ArrayList<Song>> artistMap;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;
    private MusicController controller;
    Timer timer;
    RelativeLayout rL;
    ImageView songViewPlayPause;
    ImageButton songViewNext;
    ImageButton songViewPrev;
    LinearLayout artistPlaylistView;
    ImageView cross2;
    ListView artistListView;
    TextView artistPlaylistViewTitleText;
    ListView artistPlaylistList;
    ArrayList<Album> albumList;
    MusicUtils musicUtils;
    boolean isTimerRunning;
    boolean timerWait;
    ImageView shuffleControl;
    ImageView heartControl;
    ImageView loopControl;
    boolean favorite;
    SlidingUpPanelLayout slidingUpPanel;
    TextView queueTop;
    ImageView expand;
    RecyclerView queueRecyclerView;
    ItemTouchHelper itemTouchHelper;
    SongQueueAdapter songQueueAdapter;
    AnimationHelper animationHelper;
    DatabaseHelper dbHelper;
    JollyEqualizer eq;
    BassBoost boost;
    Virtualizer virtualizer;
    DisplayMetrics displayMetrics;
    int screenHeight;
    int screenWidth;
    HashMap<String, ArrayList<Song>> genreList;
    LinearLayout genreView;
    RecyclerView genreViewList;
    ImageView genreViewCross;
    TextView genreViewTitleText;
    GenreViewAdapter genreViewAdapter;
    ArrayList<Song> genreCurrentSongs;
    HashMap<String, ArrayList<Song>> playlistMap;
    LinearLayout playlistView;
    RecyclerView playlistViewList;
    ImageView playlistViewCross;
    TextView playlistViewTitleText;
    PlaylistViewAdapter playlistViewAdapter;
    ArrayList<Song> playlistCurrentSongs;
    OnSongClickedListener onSongClickedListener;
    Album curAlbum;
    ArrayList<Song> curAlbumSongs;
    ConstraintLayout album_view;
    ImageView album_view_cross;
    ImageView top_album_view_image;
    RecyclerView album_view_list;
    AlbumViewAdapter albumViewAdapter;
    TextView album_view_album_name;
    TextView album_view_track_total;
    TextView album_view_dur_total;
    TextView album_view_released;
    FrameLayout playPauseContainer;
    ProgressBar displaySeekBar;
    RecyclerView artist_view_album_list;
    ArtistAlbumViewAdapter artist_album_view_adapter;
    ArrayList<Album> curArtistAlbums;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    BubbleLayout bubbleLayout;
    ImageView songDisplayAlbumArt;
    ImageButton displayNextControl;

    RequestOptions requestOptionsTransparent = new RequestOptions();
    RequestOptions requestOptionsBlur = new RequestOptions().transform(new BlurTransformation(22, 3));
    JollyUtils jollyUtils;
    AlertDialog alertDialog;

    ArrayList<Song> previousMusicList;
    int previousMusicPosition;
    int previousSong;
    boolean previousShuffleStatus;
    boolean previousLoopStatus;
    boolean previousIsPlayingStatus;
    String previousSongTitle;
    String previousSongArtist;

    ExecutorService executorService;
    Handler mainThreadHandler;

    Runnable startBubbleRunnable = new Runnable() {
        @Override
        public void run() {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    bubbleLayout.start();
                }
            });
            String prefThemeName = settings.getString("theme", "lightTheme");
            int color;
            if(prefThemeName.equals("lightTheme"))
                color = getResources().getColor(R.color.blackTransparent);
            else
                color = getResources().getColor(R.color.whiteTransparent);
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    bubbleLayout.setBubbleColor(color);
                }
            });
        }
    };

    Runnable mainBgOptionsRunnable = new Runnable() {
        @Override
        public void run() {
            Drawable drawable = getResources().getDrawable(R.drawable.splash);
            if(!settings.getBoolean("bgOptions", false))
                drawable = jollyUtils.blurDrawable(getApplicationContext(), drawable, 25);
            Glide.with(getApplicationContext()).load(drawable).override(screenWidth, screenHeight).centerCrop().into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    rL.setBackground(resource);
                }
            });
        }
    };

    Runnable drawableStateRunnable = new Runnable() {
        @Override
        public void run() {
            favorite = false;
            ArrayList<Playlist> tempAllPlaylists = dbHelper.getAllPlaylists();
            Playlist tempPlaylist = null;
            for (Playlist p : tempAllPlaylists) {
                if (p.getPlaylistName().equals("Favorites"))
                    tempPlaylist = p;
            }
            for (Song s : tempPlaylist.getPlaylistSongs()) {
                if (musicSrv != null && s.getID() == musicSrv.getSongId())
                    favorite = true;
            }

            if (!favorite) {
                String prefThemeName = settings.getString("theme", "lightTheme");
                if (prefThemeName.equals("lightTheme")) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            heartControl.setImageResource(R.drawable.star_empty_dark);
                        }
                    });
                    DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.blackPrimary));
                } else {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            heartControl.setImageResource(R.drawable.star_empty);
                        }
                    });
                    DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                }
            } else {
                String prefThemeName = settings.getString("theme", "lightTheme");
                if (prefThemeName.equals("lightTheme")) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            heartControl.setImageResource(R.drawable.star_dark);
                        }
                    });
                }else {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            heartControl.setImageResource(R.drawable.star_filled);
                        }
                    });
                }
                DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
            }
        }
    };

    private BroadcastReceiver showBubblesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean show_bubbles_status = settings.getBoolean("show_bubbles", true);
            if(show_bubbles_status)
                executorService.execute(startBubbleRunnable);
            else
                bubbleLayout.stop();
        }
    };

    private BroadcastReceiver requestReadPermReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            requestReadStorage();
        }
    };

    private BroadcastReceiver albumOpenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            setAlbumViewList((Album)bundle.getSerializable("album"));
            albumViewOpenClose(true);
        }
    };

    private BroadcastReceiver bgOptionsReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            executorService.execute(mainBgOptionsRunnable);
        }
    };

    private BroadcastReceiver eqInitReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int audio_session_id = intent.getIntExtra("audio_session_id", -1);

            eq = new JollyEqualizer(0, audio_session_id);
            boost = new BassBoost(0, audio_session_id);
            virtualizer = new Virtualizer(0, audio_session_id);
            if (eq != null)
                eq.setEnabled(true);
            if (boost != null)
                boost.setEnabled(true);
            if (virtualizer != null)
                virtualizer.setEnabled(true);
            if (!settings.getBoolean("eq_enabled", true))
                eq.setEnabled(false);
            if (((SettingsTab) adapter.getPage(0)) != null)
                ((SettingsTab) adapter.getPage(0)).setEq(eq, boost, virtualizer);
        }
    };

    private BroadcastReceiver songViewDeletedReceiver = new BroadcastReceiver() {
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
                    i--;//maybe just break
                }else{
                    a.setAlbumSongs(temp);
                    albumList.set(i, a);
                }

            }
            albumViewAdapter.notifyDataSetChanged();
        }
    };

    private BroadcastReceiver addPlayNextReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(songQueueAdapter != null && musicSrv != null) {
                songQueueAdapter.setCurrentlyPlayingPosition(musicSrv.getSongPosn());
                songQueueAdapter.notifyDataSetChanged();
            }
        }
    };

    private BroadcastReceiver songReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            String album_name = intent.getStringExtra("album_name");
            String album_art_path = intent.getStringExtra("album_art");
            long album_id = intent.getLongExtra("album_id", -1);
            String dur = intent.getStringExtra("dur");
            String genre = intent.getStringExtra("genre");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (musicSrv != null) {
                        musicSrv.updateEq();
                        boolean dynamic_eq_status = settings.getBoolean("deq", false);
                        if (dynamic_eq_status) {
                            mainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(((SettingsTab) adapter.getPage(0)) != null && genre != null && !genre.equals(""))
                                        ((SettingsTab) adapter.getPage(0)).setDEQPreset(genre);
                                }
                            });
                        }

                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (queueRecyclerView == null && musicSrv != null) {
                                    queueRecyclerView = findViewById(R.id.queue_list);
                                    songQueueAdapter = new SongQueueAdapter(getApplicationContext(), MainActivity.this, R.layout.song_queue, onSongClickedListener, musicSrv, musicSrv.getSongList());
                                    songQueueAdapter.setOnSongClickedListener(new OnSongClickedListener() {
                                        @Override
                                        public void onSongClicked(int posn) {
                                            getMusicSrv().setList(songQueueAdapter.getSongList());
                                            //getMusicSrv().updateAll();
                                            songPicked(posn);
                                        }

                                        @Override
                                        public void onSongClickedShuffle(int posn) {

                                        }
                                    });
                                    RecyclerView.LayoutManager queueLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
                                        @Override
                                        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
                                        }

                                        @Override
                                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                            int fromPosition = viewHolder.getAdapterPosition();
                                            int toPosition = target.getAdapterPosition();

                                            if (toPosition < 0 || toPosition >= songQueueAdapter.getSongList().size()) {
                                                return false;
                                            }

                                            ArrayList<Song> tempAdapterList = songQueueAdapter.getSongList();

                                            // swap items in adapter
                                            if (fromPosition < toPosition) {
                                                for (int i = fromPosition; i < toPosition; i++) {
                                                    Collections.swap(tempAdapterList, i, i + 1);
                                                }
                                            } else {
                                                for (int i = fromPosition; i > toPosition; i--) {
                                                    Collections.swap(tempAdapterList, i, i - 1);
                                                }
                                            }
                                            songQueueAdapter.setSongList(tempAdapterList);
                                            musicSrv.setSongList(tempAdapterList);
                                            songQueueAdapter.notifyItemMoved(fromPosition, toPosition);
                                            if(musicSrv.getSongPosn() == fromPosition)
                                                musicSrv.setSong(toPosition);//TODO: does this cause issues?
                                            return true;
                                        }

                                        @Override
                                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                                        }

                                        @Override
                                        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
                                            super.onSelectedChanged(viewHolder, actionState);
                                            if (actionState == 2) {
                                                slidingUpPanel.setTouchEnabled(false);
                                            }else {
                                                slidingUpPanel.setTouchEnabled(true);
                                            }
                                        }

                                        @Override
                                        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                                            super.clearView(recyclerView, viewHolder);
                                        }
                                    });
                                    itemTouchHelper.attachToRecyclerView(queueRecyclerView);
                                    queueRecyclerView.setLayoutManager(queueLayoutManager);
                                    queueRecyclerView.setAdapter(songQueueAdapter);
                                } else {
                                    songQueueAdapter.setSongList(musicSrv.getSongList());
                                    songQueueAdapter.notifyDataSetChanged();
                                    if(musicSrv.isPng() && musicSrv.getSongPosn() > -1) {
                                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            requestRecordAudio();
                                        }else {
                                            songQueueAdapter.setCurrentlyPlayingPosition(musicSrv.getSongPosn());
                                            songQueueAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        });
                    }

                    if (title != null && totalSongsText != null && musicSrv != null && !title.equals("Title")) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                totalSongsText.setText(title);
                                songViewTitleText.setText(title);
                            }
                        });
                    }
                    if (artist != null && musicSrv != null && totalSongs != null && !artist.equals("Artist")) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                totalSongs.setText(artist);
                            }
                        });
                    }

                    if (displayNextControl != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayNextControl.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        musicSrv.playNext();
                                    }
                                });
                            }
                        });
                    }

                    Bitmap albumArt = null;
                    Bitmap backgroundArt = null;

                    String prefThemeName = settings.getString("theme", "lightTheme");
                    int color;
                    if(prefThemeName.equals("lightTheme"))
                        color = getResources().getColor(R.color.blackTransparent);
                    else
                        color = getResources().getColor(R.color.whiteTransparent);

                    if (album_art_path != null && !album_art_path.equals("")) {
                        albumArt = BitmapFactory.decodeFile(album_art_path);
                        backgroundArt = albumArt;
                    }
                    if (albumArt == null && album_id != -1) {
                        Bitmap tempBit = musicUtils.getAlbumArt(album_id);
                        albumArt = tempBit;
                        backgroundArt = tempBit;
                    }
                    if (albumArt != null) {
                        // Create a Palette object from the bitmap.
                        Palette palette = Palette.from(albumArt).generate();

                        // Get the dominant color.
                        Palette.Swatch swatch = palette.getLightVibrantSwatch();
                        if (swatch == null)
                            swatch = palette.getLightMutedSwatch();
                        if (swatch == null)
                            color = palette.getDarkVibrantColor(getResources().getColor(R.color.whiteTransparent));
                        if (swatch != null) {
                            int dominantColor = swatch.getRgb();

                            // Extract the ARGB values of the dominant color.
                            int a = Color.alpha(dominantColor);
                            int r = Color.red(dominantColor);
                            int g = Color.green(dominantColor);
                            int b = Color.blue(dominantColor);

                            // Create a Color object from the dominant color.
                            color = Color.argb(a, r, g, b);
                        }
                    } else {
                        albumArt = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.new_album_art);
                        backgroundArt = BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.splash));
                    }
                    if (!settings.getBoolean("bg_change_status", false))
                        backgroundArt = BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.splash));
                    //Glide.with(context).clear(albumArtImage);

                    final Bitmap albumArtFinal = albumArt;
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(context).load(albumArtFinal).apply(RequestOptions.circleCropTransform()).transition(withCrossFade(2000)).into(albumArtImage);
                            Glide.with(context).load(albumArtFinal).apply(RequestOptions.circleCropTransform()).transition(withCrossFade(2000)).into(songDisplayAlbumArt);
                        }
                    });

                    //Glide.with(context).clear(rL);
                    Drawable drawable = new BitmapDrawable(getResources(), backgroundArt);
                    if (!settings.getBoolean("bgOptions", false))
                        drawable = jollyUtils.blurDrawable(getApplicationContext(), drawable, 25);
                    Glide.with(getApplicationContext()).load(drawable).override(screenWidth, screenHeight).centerCrop().into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            rL.setBackground(resource);
                        }
                    });

                    boolean show_bubbles_status = settings.getBoolean("show_bubbles", true);
                    final int colorFinal = color;
                    if (show_bubbles_status) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bubbleLayout.stop();
                                bubbleLayout.start();
                                bubbleLayout.setBubbleColor(colorFinal);
                            }
                        });
                    } else {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                bubbleLayout.stop();
                            }
                        });
                    }

                    if (musicSrv != null && songViewPlayPause != null && playPauseButton != null) {
                        if (musicSrv.isPng()) {
                            if (prefThemeName.equals("lightTheme")) {
                                mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        playPauseButton.setImageResource(R.drawable.pause_button_dark);
                                        songViewPlayPause.setImageResource(R.drawable.pause_button_dark);
                                    }
                                });
                            } else {
                                mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        playPauseButton.setImageResource(R.drawable.pausebutton);
                                        songViewPlayPause.setImageResource(R.drawable.pausebutton);
                                    }
                                });
                            }
                        } else {
                            //!musicSrv.isPng() && playPauseButton != null && playPauseButton.getDrawable() != null && playPauseButton.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.pausebutton).getConstantState())
                            if (prefThemeName.equals("lightTheme")) {
                                mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        playPauseButton.setImageResource(R.drawable.play_dark);
                                        songViewPlayPause.setImageResource(R.drawable.play_dark);
                                    }
                                });
                            } else {
                                mainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        playPauseButton.setImageResource(R.drawable.play_button);
                                        songViewPlayPause.setImageResource(R.drawable.play_button);
                                    }
                                });
                            }
                        }
                    }

                    if (maxSeekDur != null && musicSrv != null && musicSrv.getSongList().size() > 0) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                maxSeekDur.setText(musicUtils.convertFromMilli(Integer.parseInt(musicSrv.getSongList().get(musicSrv.getSongPosn()).getDuration())));
                            }
                        });
                    }
                    if (nowPlayingArtist != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                nowPlayingArtist.setText(artist);
                            }
                        });
                    }
                    if (album_name != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                nowPlayingAlbum.setText(album_name);
                            }
                        });
                    } else {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                nowPlayingAlbum.setText("");
                            }
                        });
                    }

                    if (musicSrv != null) {
                        if (musicSrv.isShuffle()) {
                            DrawableCompat.setTint(DrawableCompat.wrap(shuffleControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        } else {
                            if (prefThemeName.equals("lightTheme"))
                                DrawableCompat.setTint(DrawableCompat.wrap(shuffleControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.blackPrimary));
                            else
                                DrawableCompat.setTint(DrawableCompat.wrap(shuffleControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                        }
                    }

                    int curSongDur = 0;
                    if (musicSrv != null && musicSrv.getSongDur() != null)
                        curSongDur = (Integer.parseInt(musicSrv.getSongDur()) / 1000);
                    final int curSongDurFinal = curSongDur;
                    if (seekArc != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                seekArc.setMaxProgress(((float) curSongDurFinal));
                            }
                        });
                    }
                    if (displaySeekBar != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                displaySeekBar.setMax((curSongDurFinal));
                            }
                        });
                    }

                    if (timer == null) {
                        timer = new Timer();
                        try {
                            timer.schedule(timerTask, 0, 1000);
                        }catch(IllegalStateException e) {
                            e.printStackTrace();
                        }
                        isTimerRunning = true;
                    }
                }
            });

            executorService.execute(drawableStateRunnable);

        }
    };

    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if (musicSrv != null) {
                if (timerWait) {
                    try {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (musicSrv != null) {
                                    seekArc.setProgress(((float) ((musicSrv.getCurrentProgress()) / 1000)));
                                    displaySeekBar.setProgress(((musicSrv.getCurrentProgress()) / 1000));
                                    curSeekDur.setText(musicUtils.convertFromMilli(musicSrv.getCurrentProgress()));
                                }
                            }
                        });
                    }catch(NullPointerException ex) {
                        ex.printStackTrace();
                    }
                }

                                    /*try {
                                        if (musicSrv != null && musicSrv.getSongList().size() > 0 && musicSrv.getCurrentProgress() >= Integer.parseInt(musicSrv.getSongList().get(musicSrv.getSongPosn()).getDuration()))
                                            musicSrv.playNext();
                                    }catch(IllegalStateException ex){

                                    }*/
                //check for inconsistencies in the player controls
                if (musicSrv.isPng()) {
                    String prefThemeName = settings.getString("theme", "lightTheme");
                    if (prefThemeName.equals("lightTheme")) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                playPauseButton.setImageResource(R.drawable.pause_button_dark);
                                songViewPlayPause.setImageResource(R.drawable.pause_button_dark);
                            }
                        });
                    } else {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                playPauseButton.setImageResource(R.drawable.pausebutton);
                                songViewPlayPause.setImageResource(R.drawable.pausebutton);
                            }
                        });
                    }
                } else if (!musicSrv.isPng() && playPauseButton != null && playPauseButton.getDrawable() != null && playPauseButton.getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.pausebutton).getConstantState())) {
                    String prefThemeName = settings.getString("theme", "lightTheme");
                    if (prefThemeName.equals("lightTheme")) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                playPauseButton.setImageResource(R.drawable.play_dark);
                                songViewPlayPause.setImageResource(R.drawable.play_dark);
                            }
                        });
                    } else {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                playPauseButton.setImageResource(R.drawable.play_button);
                                songViewPlayPause.setImageResource(R.drawable.play_button);
                            }
                        });
                    }
                }
                if(musicSrv != null && songQueueAdapter != null && songQueueAdapter.getCurrentlyPlayingPosition() != musicSrv.getSongPosn()) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            songQueueAdapter.setCurrentlyPlayingPosition(musicSrv.getSongPosn());
                            songQueueAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);

        String prefThemeName = settings.getString("theme", "lightTheme");
        if(prefThemeName.equals("lightTheme"))
            setTheme(R.style.ThemeLight);
        else
            setTheme(R.style.ThemeDark);

        setContentView(R.layout.activity_main);

        editor = getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE).edit();
        if(settings.getString("media_player_songs", null) != null) {
            String encodedList = settings.getString("media_player_songs", null);
            if(encodedList != null) {
                try {
                    byte[] bytes = Base64.decode(encodedList, Base64.DEFAULT);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    previousMusicList = (ArrayList<Song>) objectInputStream.readObject();
                }catch(IOException|ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(settings.getInt("media_player_position", -1) > -1)
                previousMusicPosition = settings.getInt("media_player_position", -1);
            else
                previousMusicPosition = -1;
            if(settings.getBoolean("media_player_status", false))
                previousIsPlayingStatus = true;
            else
                previousIsPlayingStatus = false;
            if(settings.getBoolean("media_player_shuffle", false))
                previousShuffleStatus = true;
            else
                previousShuffleStatus = false;
            if(settings.getBoolean("media_player_loop", false))
                previousLoopStatus = true;
            else
                previousLoopStatus = false;
            if(settings.getInt("media_player_previous_song", -1) > -1)
                previousSong = settings.getInt("media_player_previous_song", -1);
            if(settings.getString("media_player_song_title", null) != null)
                previousSongTitle = settings.getString("media_player_song_title", null);
            else
                previousSongTitle = "";
            if(settings.getString("media_player_song_artist", null) != null)
                previousSongArtist = settings.getString("media_player_song_artist", null);
            else
                previousSongArtist = "";
        }else{
            previousMusicList = new ArrayList<Song>();
        }

        rL = findViewById(R.id.rL);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        jollyUtils = new JollyUtils(getApplicationContext());

        musicUtils = new MusicUtils(getContentResolver(), getApplicationContext());
        dbHelper = new DatabaseHelper(getApplicationContext());

        pager = findViewById(R.id.pager);
        adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), FragmentPagerItems.with(this).add(R.string.tab0, SettingsTab.class).add(R.string.tab1, SongTab.class).add(R.string.tab2, ArtistTab.class).add(R.string.tab3, PlaylistTab.class).add(R.string.tab5, AlbumsTab.class).add(R.string.tab4, GenreTab.class).create());
        pager.setAdapter(adapter);

        tabs = findViewById(R.id.tabs);

        tabs.setViewPager(pager);
        pager.setCurrentItem(1);

        songListView = findViewById(R.id.song_list);

        setController();

        tabs.bringToFront();
        pager.setOffscreenPageLimit(5);

        // NOW PLAYING VIEW/SONG VIEW
        songViewTitleText = findViewById(R.id.songViewTitleText);
        songViewTitleText.setSelected(true);

        albumArtImage = findViewById(R.id.albumArtImage);

        curSeekDur = findViewById(R.id.curSeekDur);
        maxSeekDur = findViewById(R.id.maxSeekDur);

        nowPlayingArtist = findViewById(R.id.nowPlayingArtist);
        nowPlayingAlbum = findViewById(R.id.nowPlayingAlbum);
        songDisplayAlbumArt = findViewById(R.id.songDisplayAlbumArt);
        displayNextControl = findViewById(R.id.displayNextControl);

        playPauseButton = findViewById(R.id.playPauseControl);

        displaySeekBar = findViewById(R.id.displaySeekBar);
        /*displaySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        heartControl = findViewById(R.id.heart_control);
        shuffleControl = findViewById(R.id.shuffle_control);
        loopControl = findViewById(R.id.loop_control);

        heartControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Playlist> playlists = dbHelper.getAllPlaylists();
                Playlist favPlaylist = null;
                for(Playlist p : playlists) {
                    if(p.getPlaylistName().equals("Favorites"))
                        favPlaylist = p;
                }
                if(favPlaylist == null) {
                    long p_id = dbHelper.addPlaylist("Favorites");
                    favPlaylist =  dbHelper.getPlaylist((int)p_id);
                }
                if(favorite) {
                    favorite = false;
                    String prefThemeName = settings.getString("theme", "lightTheme");
                    if(prefThemeName.equals("lightTheme")) {
                        heartControl.setImageResource(R.drawable.star_empty_dark);
                        DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.blackPrimary));
                    } else {
                        heartControl.setImageResource(R.drawable.star_empty);
                        DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT).show();
                    dbHelper.delSongFromPlaylist((int)favPlaylist.getId(), (int)musicSrv.getSongId());
                }else{
                    favorite = true;
                    String prefThemeName = settings.getString("theme", "lightTheme");
                    if(prefThemeName.equals("lightTheme"))
                        heartControl.setImageResource(R.drawable.star_dark);
                    else
                        heartControl.setImageResource(R.drawable.star_filled);
                    Toast.makeText(getApplicationContext(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT).show();
                    DrawableCompat.setTint(DrawableCompat.wrap(heartControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    dbHelper.addSongToPlaylist((int)favPlaylist.getId(), (int)musicSrv.getSongId());
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("playlistChanged"));
            }
        });
        shuffleControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable shuffle_icon;
                String prefThemeName = settings.getString("theme", "lightTheme");
                if(prefThemeName.equals("lightTheme"))
                    shuffle_icon = getResources().getDrawable(R.drawable.shuffle_dark);
                else
                    shuffle_icon = getResources().getDrawable(R.drawable.shuffle);
                if(musicSrv.isShuffle()) {
                    musicSrv.toggleShuffle(false);
                    if(prefThemeName.equals("lightTheme"))
                        DrawableCompat.setTint(shuffle_icon, ContextCompat.getColor(getApplicationContext(), R.color.blackPrimary));
                    else
                        DrawableCompat.setTint(shuffle_icon, ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                    shuffleControl.setImageDrawable(shuffle_icon);
                }else{
                    musicSrv.toggleShuffle(true);
                    DrawableCompat.setTint(shuffle_icon, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    shuffleControl.setImageDrawable(shuffle_icon);
                }
            }
        });
        loopControl.setOnClickListener(view -> {
            if(musicSrv != null) {
                if (musicSrv.isLoop()) {
                    musicSrv.toggleLoop(false);
                    if (prefThemeName.equals("lightTheme"))
                        DrawableCompat.setTint(DrawableCompat.wrap(loopControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.blackPrimary));
                    else
                        DrawableCompat.setTint(DrawableCompat.wrap(loopControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
                } else {
                    musicSrv.toggleLoop(true);
                    DrawableCompat.setTint(DrawableCompat.wrap(loopControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                }
            }
        });

        seekArc = findViewById(R.id.seekDur);
        timerWait = true;
        seekArc.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                timerWait = false;
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                timerWait = true;
            }

            @Override
            public void onProgressChanged(SeekArc seekArc, float progress) {
                curSeekDur.setText(musicUtils.convertFromMilli(((int)progress)*1000));
                musicSrv.seek(((int)progress)*1000);
            }

        });

        songViewPlayPause = findViewById(R.id.songViewPlayPause);
        songViewPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicSrv.isPng()) {
                    pause();
                } else {
                    start();
                }
            }
        });

        songViewNext = findViewById(R.id.songViewNext);
        songViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        songViewPrev = findViewById(R.id.songViewPrevious);
        songViewPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrev();
            }
        });

        slidingUpPanel = findViewById(R.id.slidingUpPanel);
        expand = findViewById(R.id.expand);
        queueTop = findViewById(R.id.queueTop);
        slidingUpPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    queueTop.setText(R.string.playning_next);
                    if(musicSrv != null && musicSrv.getSongPosn() > -1) {
                        queueRecyclerView.getLayoutManager().scrollToPosition(musicSrv.getSongPosn());
                        if(songQueueAdapter != null) {
                            if (songQueueAdapter.getCurrentlyPlayingPosition() != musicSrv.getSongPosn()) {
                                songQueueAdapter.setCurrentlyPlayingPosition(musicSrv.getSongPosn());
                            }
                            songQueueAdapter.notifyDataSetChanged();
                        }
                    }
                    //TypedValue value = new TypedValue();
                    //getTheme().resolveAttribute(R.attr.collapse, value, true);
                    //expand.setImageResource(value.data);//collapse
                    expand.setRotation(180f);
                }else if(newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                    queueTop.setText(R.string.song_queue);
                    //TypedValue value = new TypedValue();
                    //getTheme().resolveAttribute(R.attr.expand, value, true);
                    //expand.setImageResource(value.data);//expand
                    expand.setRotation(0f);
                }else if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                    queueTop.setText(R.string.song_queue);
                    //TypedValue value = new TypedValue();
                    //getTheme().resolveAttribute(R.attr.expand, value, true);
                    //expand.setImageResource(value.data);//expand
                    expand.setRotation(0f);
                }
            }
        });

        animationHelper = new AnimationHelper(getApplicationContext());

        artistPlaylistView = findViewById(R.id.artistPlaylistView);
        artistListView = findViewById(R.id.artistListView);

        genreView = findViewById(R.id.genreView);
        genreViewList = findViewById(R.id.genreViewList);
        genreViewCross = findViewById(R.id.genreViewCross);
        genreViewTitleText = findViewById(R.id.genreViewTitleText);
        genreViewCross.setOnClickListener(view -> genreViewOpenClose(false));

        playlistView = findViewById(R.id.playlistView);
        playlistViewList = findViewById(R.id.playlistViewList);
        playlistViewCross = findViewById(R.id.playlistViewCross);
        playlistViewTitleText = findViewById(R.id.playlistViewTitleText);
        playlistViewCross.setOnClickListener(v -> playlistViewOpenClose(false));

        //Playlist, Genre
        playlistMap = new HashMap<>();
        genreCurrentSongs = new ArrayList<>();
        playlistCurrentSongs = new ArrayList<>();
        genreViewAdapter = new GenreViewAdapter(getApplicationContext(), this, getMusicSrv());
        genreViewList.setLayoutManager(new LinearLayoutManager(this));
        genreViewList.setAdapter(genreViewAdapter);
        playlistViewAdapter = new PlaylistViewAdapter(getApplicationContext(), this, getMusicSrv());
        playlistViewList.setLayoutManager(new LinearLayoutManager(this));
        playlistViewList.setAdapter(playlistViewAdapter);

        //Album
        curAlbum = new Album();
        curAlbumSongs = new ArrayList<>();
        album_view = findViewById(R.id.album_view);
        album_view_cross = findViewById(R.id.album_view_cross);
        album_view_cross.setOnClickListener(v -> albumViewOpenClose(false));
        top_album_view_image = findViewById(R.id.top_album_view_image);
        albumViewAdapter = new AlbumViewAdapter(getApplicationContext(), this, getMusicSrv(), curAlbum, curAlbumSongs);
        album_view_list = findViewById(R.id.album_view_list);
        /*albumViewShuffle = findViewById(R.id.album_view_shuffle_button);
        albumViewShuffle.setOnClickListener(view13 -> {
            Random rand = new Random();
            ArrayList<Song> tempSongs = new ArrayList<>(curAlbumSongs);
            if(tempSongs.size() > 0) {
                getMusicSrv().setList(tempSongs);
                getMusicSrv().toggleShuffle(true);
                songPicked(rand.nextInt(tempSongs.size()));
            }else {
                Toast.makeText(getApplicationContext(), R.string.no_songs, Toast.LENGTH_SHORT).show();
            }
        });*/
        LinearLayoutManager album_lm = new LinearLayoutManager(this);
        album_lm.setStackFromEnd(false);
        album_lm.setReverseLayout(false);
        album_view_list.setLayoutManager(album_lm);
        album_view_list.setAdapter(albumViewAdapter);
        album_view_album_name = findViewById(R.id.album_view_album_name);
        album_view_dur_total = findViewById(R.id.album_view_dur_total);
        album_view_track_total = findViewById(R.id.album_view_track_total);
        album_view_released = findViewById(R.id.album_view_released);

        //Artist Album
        artist_view_album_list = findViewById(R.id.artist_view_album_list);
        curArtistAlbums = new ArrayList<>();
        artist_album_view_adapter = new ArtistAlbumViewAdapter(getApplicationContext(), curArtistAlbums);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        artist_view_album_list.setLayoutManager(llm);
        artist_view_album_list.setAdapter(artist_album_view_adapter);

        songView = findViewById(R.id.songView);

        totalSongs = findViewById(R.id.totalSongs);
        totalSongsText = findViewById(R.id.totalSongsText);
        totalSongs.setSelected(true);
        totalSongsText.setSelected(true);

        bubbleLayout = findViewById(R.id.bubbleLayout);
        bubbleLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                boolean show_bubbles_status = settings.getBoolean("show_bubbles", true);
                if(show_bubbles_status) {
                    bubbleLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            bubbleLayout.start();
                            String prefThemeName = settings.getString("theme", "lightTheme");
                            int color;
                            if(prefThemeName.equals("lightTheme"))
                                color = getResources().getColor(R.color.blackTransparent);
                            else
                                color = getResources().getColor(R.color.whiteTransparent);
                            bubbleLayout.setBubbleColor(color);
                        }
                    });
                }
            }
        });

        songList = new ArrayList<>();

        LocalBroadcastManager.getInstance(this).registerReceiver(songReceiver, new IntentFilter("sendUpdate"));
        LocalBroadcastManager.getInstance(this).registerReceiver(songViewDeletedReceiver, new IntentFilter("songViewDeleted"));
        LocalBroadcastManager.getInstance(this).registerReceiver(addPlayNextReceiver, new IntentFilter("addPlayNext"));
        LocalBroadcastManager.getInstance(this).registerReceiver(eqInitReceiver, new IntentFilter("initEq"));
        LocalBroadcastManager.getInstance(this).registerReceiver(albumOpenReceiver, new IntentFilter("openAlbum"));
        LocalBroadcastManager.getInstance(this).registerReceiver(bgOptionsReciever, new IntentFilter("updateBgOptions"));
        LocalBroadcastManager.getInstance(this).registerReceiver(requestReadPermReceiver, new IntentFilter("requestReadPerm"));
        LocalBroadcastManager.getInstance(this).registerReceiver(showBubblesReceiver, new IntentFilter("show_bubbles"));

        //adView = findViewById(R.id.adView);
        //AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        //adView.loadAd(adRequest);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
                executorService = Executors.newFixedThreadPool(10);
                executorService.execute(mainBgOptionsRunnable);
                requestReadStorage();
            }
        });
        t.start();
    }

    public void requestReadStorage() {
        requestReadStorage(true);
    }

    public void requestReadStorage(boolean changeDisplay) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show a message explaining why the permission is needed
                showRationaleDialog();
            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        jollyUtils.READ_EXTERNAL_STORAGE);
            }
        } else {
            // Permission has already been granted
            readPermGranted(changeDisplay);
        }
    }

    private void showRationaleDialog() {
        // Show a dialog explaining why the permission is needed
        Activity a = this;
        alertDialog = new AlertDialog.Builder(this, R.style.AlertDialogCustom)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.read_permission_required)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, jollyUtils.READ_EXTERNAL_STORAGE);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    public void readPermGranted(boolean changeDisplay) {
        Intent intent = new Intent("readPermGranted");
        jollyUtils.sendLocalBroadcast(intent);
        setAllLists(changeDisplay);
    }

    public void setAllLists(boolean changeDisplay) {
        class MyTask extends AsyncTask<Void, Void, String> {

            ArrayList<Song> sList;
            TreeMap<String, ArrayList<Song>> artList;
            ArrayList<Album> albList;
            HashMap<String, ArrayList<Song>> genList;
            int temp = -1;

            @Override
            protected String doInBackground(Void... voids) {
                sList = new ArrayList<>();
                artList = new TreeMap<>();
                albList = new ArrayList<>();
                genList = new HashMap<>();

                sList = musicUtils.getSongList(getApplicationContext());
                artList = musicUtils.getArtists(sList);
                albList = musicUtils.getAlbumList(sList);
                genList = musicUtils.getGenreList(sList);

                Collections.sort(sList, (a, b) -> a.getTitle().compareTo(b.getTitle()));

                temp = songList.size();
                songList = new ArrayList<>(sList);
                artistMap = new TreeMap<>(artList);
                albumList = new ArrayList<>(albList);
                genreList = new HashMap<>(genList);

                return "Task completed!";
            }

            @Override
            protected void onPostExecute(String result) {
                if(totalSongs.getText().toString().equals("0") || totalSongs.getText().toString().equals(String.valueOf(temp)))
                    totalSongs.setText(String.valueOf(songList.size()));

                if(changeDisplay) {
                    executorService.execute(displayRunnable);
                }
            }
        }
        // Use the following code to start the task
        new MyTask().execute();
    }

    private Runnable displayRunnable = new Runnable() {
        @Override
        public void run() {
            if(musicSrv != null && previousMusicList != null && previousMusicList.size() > 0) {
                musicSrv.setSongList(previousMusicList);
                if (previousSong > -1) {
                    musicSrv.toggleLoop(previousLoopStatus, false);
                    if (previousLoopStatus)
                        DrawableCompat.setTint(DrawableCompat.wrap(loopControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    musicSrv.toggleShuffle(previousShuffleStatus, false);
                    if (previousShuffleStatus)
                        DrawableCompat.setTint(DrawableCompat.wrap(shuffleControl.getDrawable()), ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                    songPicked(previousSong, false, MusicService.ACTION_PAUSE);//send action pause except on theme change?
                    if(previousMusicPosition > -1)
                        seekTo(previousMusicPosition);
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == jollyUtils.READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                readPermGranted(true);
            } else {
                // Permission is denied
                Toast.makeText(this, R.string.read_perm_require,
                        Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == jollyUtils.RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                if(musicSrv != null && songQueueAdapter != null && songQueueAdapter.getCurrentlyPlayingPosition() != musicSrv.getSongPosn()) {
                    songQueueAdapter.setCurrentlyPlayingPosition(musicSrv.getSongPosn());
                    songQueueAdapter.notifyDataSetChanged();
                }
            } else {
                // Permission is denied
                Toast.makeText(this, "Record Audio permissions are required in order to display visualizers.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();

            //pass list

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            playIntent.setAction(MusicService.ACTION_PLAY);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(playIntent);
            } else {
                startService(playIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void songPicked(int position) {
        songPicked(position, true, MusicService.ACTION_PLAY);
    }

    public void songPicked(int position, boolean now_playing_open, String action) {
        musicSrv.setSong(position);
        playIntent.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(playIntent);
        } else {
            startService(playIntent);
        }
        boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
        if (open_now_playing_status && now_playing_open)
            openCloseNowPlaying(true);
        setDisplayControls(position);
        if(action.equals(MusicService.ACTION_PLAY))
            musicSrv.playSong();
        else
            musicSrv.pauseSong();
    }

    public void setDisplayControls(int position) {
        cross = findViewById(R.id.cross);
        cross.setOnClickListener(v -> {
            openCloseNowPlaying(false);
            cross.bringToFront();
        });

        if (totalSongs != null && musicSrv != null) {
            String artist = musicSrv.getSongList().get(position).getArtist();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    totalSongs.setText(artist);
                }
            });
        }
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }

        playPauseContainer = findViewById(R.id.playPauseContainer);

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                playPauseContainer.setVisibility(View.VISIBLE);
                //playPauseButton = findViewById(R.id.playPauseControl);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.bringToFront();
                songDisplayAlbumArt.setVisibility(View.VISIBLE);
                displayNextControl.setVisibility(View.VISIBLE);
                displayNextControl.bringToFront();
                displaySeekBar.setVisibility(View.VISIBLE);
                playPauseButton.setOnClickListener(v -> {
                    if (musicSrv != null && musicSrv.isPng()) {
                        String prefThemeName = settings.getString("theme", "lightTheme");
                        if (prefThemeName.equals("lightTheme")) {
                            Glide.with(getApplicationContext()).load(R.drawable.play_dark).into(songViewPlayPause);
                            Glide.with(getApplicationContext()).load(R.drawable.play_dark).into(playPauseButton);
                        } else {
                            Glide.with(getApplicationContext()).load(R.drawable.playbutton).into(songViewPlayPause);
                            Glide.with(getApplicationContext()).load(R.drawable.playbutton).into(playPauseButton);
                        }
                        pause();
                    } else {
                        String prefThemeName = settings.getString("theme", "lightTheme");
                        if (prefThemeName.equals("lightTheme")) {
                            Glide.with(getApplicationContext()).load(R.drawable.pause_button_dark).into(playPauseButton);
                            Glide.with(getApplicationContext()).load(R.drawable.pause_button_dark).into(songViewPlayPause);
                        } else {
                            Glide.with(getApplicationContext()).load(R.drawable.pausebutton).into(songViewPlayPause);
                            Glide.with(getApplicationContext()).load(R.drawable.pausebutton).into(playPauseButton);
                        }
                        start();
                    }
                });

                if(musicSrv != null) {
                    totalSongsText.setText(musicSrv.getSongList().get(position).getTitle());
                    totalSongsText.setSelected(true);
                }

                songDisplay = rL.findViewById(R.id.songDisplay);
                songView = findViewById(R.id.songView);
                songDisplay.setOnClickListener(v -> {
                    openCloseNowPlaying(true);
                });
            }
        });
    }

    public void saveMusicInfo() {
        if (musicSrv != null) {
            if (musicSrv.getPosn() > -1)
                editor.putInt("media_player_position", displaySeekBar.getProgress()*1000);
            if (musicSrv.getSongList() != null && musicSrv.getSongList().size() > 0) {
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(musicSrv.getSongList());
                    String encodedList = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                    editor.putString("media_player_songs", encodedList);
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }
            editor.putBoolean("media_player_status", musicSrv.isPng());
            editor.putBoolean("media_player_shuffle", musicSrv.isShuffle());
            editor.putBoolean("media_player_loop", musicSrv.isLoop());
            if (musicSrv.getSongPosn() > -1) {
                editor.putInt("media_player_previous_song", musicSrv.getSongPosn());

                if (musicSrv.getSongList() != null && musicSrv.getSongList().size() > 0 && musicSrv.getSongList().get(musicSrv.getSongPosn()) != null) {
                    Song tempSong = musicSrv.getSongList().get(musicSrv.getSongPosn());
                    if (tempSong != null) {
                        editor.putString("media_player_song_title", tempSong.getTitle());
                        editor.putString("media_player_song_artist", tempSong.getArtist());
                    }
                }
            }
            editor.commit();

            //musicSrv.stopForeground(true);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);

        saveMusicInfo();

        if(musicSrv != null) {
            if(musicSrv.getPlayer() != null)
                musicSrv.getPlayer().reset();//release
            musicSrv.onDestroy();
            musicSrv = null;
        }
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            isTimerRunning = false;
        }

        if(eq != null)
            eq.release();

        //executorService.shutdown();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        /*stopService(playIntent);

        if(musicSrv != null) {
            if(musicSrv.getPlayer() != null)
                musicSrv.getPlayer().reset();//release
            musicSrv.onDestroy();
            musicSrv = null;
        }
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            isTimerRunning = false;
        }

        if(eq != null)
            eq.release();*/

        super.onRestart();
    }

    @Override
    public void start() {
        String prefThemeName = settings.getString("theme", "lightTheme");
        if (prefThemeName.equals("lightTheme")) {
            Glide.with(getApplicationContext()).load(R.drawable.pause_button_dark).into(playPauseButton);
            Glide.with(getApplicationContext()).load(R.drawable.pause_button_dark).into(songViewPlayPause);
        }else{
            Glide.with(getApplicationContext()).load(R.drawable.pausebutton).into(playPauseButton);
            Glide.with(getApplicationContext()).load(R.drawable.pausebutton).into(songViewPlayPause);
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                0);
        musicSrv.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        String prefThemeName = settings.getString("theme", "lightTheme");
        if(prefThemeName.equals("lightTheme")) {
            Glide.with(getApplicationContext()).load(R.drawable.play_dark).into(playPauseButton);
            Glide.with(getApplicationContext()).load(R.drawable.play_dark).into(songViewPlayPause);
        }else{
            Glide.with(getApplicationContext()).load(R.drawable.play_button).into(playPauseButton);
            Glide.with(getApplicationContext()).load(R.drawable.play_button).into(songViewPlayPause);
        }
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return musicSrv.getPlayer().getAudioSessionId();
    }

    private void setController() {
        controller = new MusicController(this);

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.rL));//findViewById(R.id.song_list)
        controller.setEnabled(true);
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        //controller.show(0);
    }

    @Override
    protected void onPause() {
        paused = true;

        if(alertDialog != null)
            alertDialog.dismiss();

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (paused) {
            setController();
            paused = false;
        }

        //executorService.execute(displayRunnable);

        super.onResume();
    }

    @Override
    protected void onStop() {
        controller.hide();

        saveMusicInfo();

        /*if(musicSrv != null) {
            if(musicSrv.getPlayer() != null)
                musicSrv.getPlayer().reset();//release
            musicSrv.onDestroy();
            musicSrv = null;
        }
        if(timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
            isTimerRunning = false;
        }

        if(eq != null)
            eq.release();*/

        super.onStop();
    }

    public ArrayList<Song> getMySongList() {
        return songList;
    }

    @Override
    public void onBackPressed() {
        if (slidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || slidingUpPanel.getPanelState() == SlidingUpPanelLayout.PanelState.DRAGGING) {
            slidingUpPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (genreView.getVisibility() == View.VISIBLE) {
            genreViewOpenClose(false);
        }else if(album_view.getVisibility() == View.VISIBLE) {
            albumViewOpenClose(false);
        } else if (playlistView.getVisibility() == View.VISIBLE) {
            playlistViewOpenClose(false);
        } else if (artistPlaylistView.getVisibility() == View.VISIBLE) {
            artistViewOpenClose(false);
        } else if (songView.getVisibility() == View.VISIBLE) {
            openCloseNowPlaying(false);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.do_you_want_to_exit)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finishAffinity();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show();
        }
        //moveTaskToBack(true);
    }

    public ArrayList<String> getArtistsNames() {
        ArrayList<String> artistNames = new ArrayList<String>();

        for(String s : artistMap.keySet()) {
            artistNames.add(s);
        }

        return artistNames;
    }

    public ArrayList<String> getArtistsNames(TreeMap<String, ArrayList<Song>> list) {
        ArrayList<String> artistNames = new ArrayList<String>();

        for(String s : list.keySet()) {
            artistNames.add(s);
        }

        return artistNames;
    }

    public void setArtistView(int position, TreeMap<String, ArrayList<Song>> list) {
        cross2 = findViewById(R.id.cross2);
        artistPlaylistView = findViewById(R.id.artistPlaylistView);

        artistPlaylistViewTitleText = findViewById(R.id.artistPlaylistViewTitleText);
        artistPlaylistList = findViewById(R.id.artist_playlist_list);

        int i = 0;
        curArtistAlbums.clear();
        artist_album_view_adapter.notifyDataSetChanged();
        for(String str : list.keySet()) {
            if(i == position) {
                artistPlaylistViewTitleText.setText(str);
                for(Album a : musicUtils.getAlbumList(list.get(str))) {
                    if(a.getArtist().equals(str))
                        curArtistAlbums.add(a);
                }
            }
            i++;
        }
        artist_album_view_adapter.notifyDataSetChanged();

        ArtistSongAdapter adap = new ArtistSongAdapter(getApplicationContext(), this, getMusicSrv(), list.get(getArtistsNames(list).get(position)));
        adap.setOnSongClickedListener(new OnSongClickedListener() {
            @Override
            public void onSongClicked(int posn) {
                boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
                if(open_now_playing_status)
                    albumViewOpenClose(false);
                getMusicSrv().setList(list.get(getArtistsNames(list).get(position)));
                //getMusicSrv().updateAll();
                songPicked(posn);
            }

            @Override
            public void onSongClickedShuffle(int posn) {
                if (!getMusicSrv().isShuffle())
                    getMusicSrv().toggleShuffle(true);
                onSongClicked(posn);
            }
        });
        artistPlaylistList.setAdapter(adap);

        cross2.setOnClickListener(v -> artistViewOpenClose(false));

        artistPlaylistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                musicSrv.setList(list.get(artistPlaylistViewTitleText.getText()));
                songPicked(position);
            }
        });
        artistViewOpenClose(true);
    }

    public MusicService getMusicSrv() {
        return musicSrv;
    }

    public void setGenreViewList(String key, HashMap<String, ArrayList<Song>> list) {
        genreViewTitleText.setText(key);
        genreViewAdapter.setGenreSongList(list.get(key));
        genreViewAdapter.setOnSongClickedListener(new OnSongClickedListener() {
            @Override
            public void onSongClicked(int posn) {
                boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
                if(open_now_playing_status)
                    genreViewOpenClose(false);
                getMusicSrv().setList(list.get(key));
                //getMusicSrv().updateAll();
                songPicked(posn);
            }

            @Override
            public void onSongClickedShuffle(int posn) {
                if (getMusicSrv().isShuffle())
                    getMusicSrv().toggleShuffle(true);
                onSongClicked(posn);
            }

        });
        genreViewAdapter.notifyDataSetChanged();
    }

    public void genreViewOpenClose(boolean openClose) {//true:open false:close
        Animation genreFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        genreFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                genreView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mainFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tabs.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                rL.startAnimation(genreFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                tabs.setVisibility(View.VISIBLE);
                pager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation genreFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        genreFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                genreView.setVisibility(View.GONE);
                rL.startAnimation(mainFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(openClose) {
            rL.startAnimation(mainFadeOut);
        }else{
            rL.startAnimation(genreFadeOut);
        }
    }

    //ALBUM
    public void setAlbumViewList(Album a) {
        curAlbum = a;
        curAlbumSongs.clear();

        ArrayList<Song> tempSongs = new ArrayList<>(a.getAlbumSongs());
        Collections.sort(tempSongs, (o1, o2) -> {
            try {
                return Integer.compare(Integer.parseInt(o1.getTrack_number()), Integer.parseInt(o2.getTrack_number()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        });

        curAlbumSongs.addAll(tempSongs);
        album_view_album_name.setText(a.getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            album_view_album_name.setFocusable(View.FOCUSABLE);
        album_view_album_name.setSelected(true);

        boolean transparent = settings.getBoolean("bgOptions", false);
        if (a.getAlbum_art() != null && !a.getAlbum_art().equals("")) {
            if(transparent) {
                Glide.with(getApplicationContext()).load(a.getAlbum_art()).transition(withCrossFade(2000)).into(top_album_view_image);
            }else {
                Glide.with(getApplicationContext()).load(a.getAlbum_art()).into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        resource = jollyUtils.blurDrawable(getApplicationContext(), resource, 25);
                        top_album_view_image.setImageDrawable(resource);
                    }
                });
            }
        }else if(musicUtils.getAlbumArt(a.getAlbum_id()) != null) {
            if(transparent) {
                Glide.with(getApplicationContext()).load(musicUtils.getAlbumArt(a.getAlbum_id())).transition(withCrossFade(2000)).into(top_album_view_image);
            }else{
                Glide.with(getApplicationContext()).load(jollyUtils.blurDrawable(getApplicationContext(), new BitmapDrawable(musicUtils.getAlbumArt(a.getAlbum_id())), 22)).transition(withCrossFade(2000)).into(top_album_view_image);
            }
        }else {
            Drawable drawable = getResources().getDrawable(R.drawable.new_album_art);
            if (!transparent)
                drawable = jollyUtils.blurDrawable(getApplicationContext(), drawable, 25);
            Glide.with(getApplicationContext()).load(drawable).transition(withCrossFade(2000)).into(top_album_view_image);
        }

        int album_songs_amount = 0;
        if(a.getAlbumSongs().size() > 0)
            album_songs_amount = a.getAlbumSongs().size();
        if(a.getTrack_count() == null || Integer.parseInt(a.getTrack_count()) <= 0)
            album_view_track_total.setText(album_songs_amount + " " + getString(R.string.tracks));
        else
            album_view_track_total.setText(album_songs_amount + getString(R.string.slash) + a.getTrack_count() + " " + getString(R.string.tracks));
        int dur_total = 0;
        for(Song s : a.getAlbumSongs())
            dur_total += Integer.parseInt(s.getDuration());
        album_view_dur_total.setText(getString(R.string.length) + " " + musicUtils.convertFromMilli(dur_total));
        if(a.getRelease_date() != null && !a.getRelease_date().equals(""))
            album_view_released.setText(getString(R.string.released) + " " + a.getRelease_date());
        else
            album_view_released.setText("");

        albumViewAdapter.setOnSongClickedListener(new OnSongClickedListener() {
            @Override
            public void onSongClicked(int posn) {
                boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
                if(open_now_playing_status)
                    albumViewOpenClose(false);
                getMusicSrv().setList(curAlbumSongs);
                //getMusicSrv().updateAll();
                songPicked(posn);
            }

            @Override
            public void onSongClickedShuffle(int posn) {
                if (!getMusicSrv().isShuffle())
                    getMusicSrv().toggleShuffle(true);
                onSongClicked(posn);
            }
        });
        albumViewAdapter.notifyDataSetChanged();
    }

    public void albumViewOpenClose(boolean openClose) {//true:open false:close
        Animation albumFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        albumFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(artistPlaylistView.getVisibility() == View.VISIBLE) {
                    artistPlaylistView.setVisibility(View.INVISIBLE);
                }
                album_view.setVisibility(View.VISIBLE);
                album_view.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mainFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(artistPlaylistView.getVisibility() != View.VISIBLE) {
                    tabs.setVisibility(View.GONE);
                    pager.setVisibility(View.GONE);
                }
                rL.startAnimation(albumFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (artistPlaylistView.getVisibility() == View.INVISIBLE) {
                    artistPlaylistView.setVisibility(View.VISIBLE);
                    artistPlaylistView.bringToFront();
                } else if (artistPlaylistView.getVisibility() == View.GONE) {
                    tabs.setVisibility(View.VISIBLE);
                    pager.setVisibility(View.VISIBLE);
                    tabs.bringToFront();
                    pager.bringToFront();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation albumFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        albumFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                album_view.setVisibility(View.GONE);
                rL.startAnimation(mainFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(openClose) {
            rL.startAnimation(mainFadeOut);
        }else{
            rL.startAnimation(albumFadeOut);
        }
    }

    //PLAYLIST
    public void setPlaylistMap(HashMap<String, ArrayList<Song>> pMap) {
        playlistMap = pMap;
    }

    public void setPlaylistViewList(String key) {
        playlistViewTitleText.setText(key);
        if(playlistMap.get(key) != null) {
            playlistViewAdapter.setPlaylistSongList(playlistMap.get(key));
            playlistViewAdapter.setPlaylistName(key);
            playlistViewAdapter.setOnSongClickedListener(new OnSongClickedListener() {
                @Override
                public void onSongClicked(int posn) {
                    boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
                    if(open_now_playing_status)
                        playlistViewOpenClose(false);
                    getMusicSrv().setList(playlistMap.get(key));
                    //getMusicSrv().updateAll();
                    songPicked(posn);
                }

                @Override
                public void onSongClickedShuffle(int posn) {
                    if (getMusicSrv().isShuffle())
                        getMusicSrv().toggleShuffle(true);
                    onSongClicked(posn);
                }
            });
            playlistViewAdapter.notifyDataSetChanged();
        }
    }

    public void playlistViewOpenClose(boolean openClose) {//true:open false:close
        Animation playlistFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        playlistFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                playlistView.setVisibility(View.VISIBLE);
                playlistView.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mainFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tabs.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                rL.startAnimation(playlistFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                tabs.setVisibility(View.VISIBLE);
                pager.setVisibility(View.VISIBLE);
                tabs.bringToFront();
                pager.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation playlistFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        playlistFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playlistView.setVisibility(View.GONE);
                rL.startAnimation(mainFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(openClose) {
            rL.startAnimation(mainFadeOut);
        }else{
            rL.startAnimation(playlistFadeOut);
        }
    }

    public void artistViewOpenClose(boolean openClose) {//true:open false:close
        Animation artistFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        artistFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                artistPlaylistView.setVisibility(View.VISIBLE);
                artistPlaylistView.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mainFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tabs.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                rL.startAnimation(artistFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                tabs.setVisibility(View.VISIBLE);
                pager.setVisibility(View.VISIBLE);
                tabs.bringToFront();
                pager.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation artistFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        artistFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                artistPlaylistView.setVisibility(View.GONE);
                rL.startAnimation(mainFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        if(openClose) {
            rL.startAnimation(mainFadeOut);
        }else{
            rL.startAnimation(artistFadeOut);
        }
    }

    public void openCloseNowPlaying(boolean openClose) {//true open, false close
        Animation nowPlayingFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        nowPlayingFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                songView.setVisibility(View.VISIBLE);
                slidingUpPanel.setVisibility(View.VISIBLE);
                //songView.bringToFront();
                //slidingUpPanel.bringToFront();
                songDisplay.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation mainFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mainFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tabs.setVisibility(View.GONE);
                pager.setVisibility(View.GONE);
                songDisplay.setVisibility(View.INVISIBLE);
                if(artistPlaylistView.getVisibility() == View.VISIBLE)
                    artistPlaylistView.setVisibility(View.INVISIBLE);
                if(genreView.getVisibility() == View.VISIBLE)
                    genreView.setVisibility(View.INVISIBLE);
                if(playlistView.getVisibility() == View.VISIBLE)
                    playlistView.setVisibility(View.INVISIBLE);
                if(album_view.getVisibility() == View.VISIBLE)
                    album_view.setVisibility(View.INVISIBLE);
                rL.startAnimation(nowPlayingFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation mainFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mainFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                rL.setVisibility(View.VISIBLE);
                songDisplay.setVisibility(View.VISIBLE);
                if (artistPlaylistView.getVisibility() == View.INVISIBLE) {
                    artistPlaylistView.setVisibility(View.VISIBLE);
                    artistPlaylistView.bringToFront();
                } else if (genreView.getVisibility() == View.INVISIBLE) {
                    genreView.setVisibility(View.VISIBLE);
                    genreView.bringToFront();
                } else if (playlistView.getVisibility() == View.INVISIBLE) {
                    playlistView.setVisibility(View.VISIBLE);
                    playlistView.bringToFront();
                } else if (album_view.getVisibility() == View.INVISIBLE) {
                    album_view.setVisibility(View.VISIBLE);
                    album_view.bringToFront();
                } else {
                    tabs.setVisibility(View.VISIBLE);
                    pager.setVisibility(View.VISIBLE);
                    tabs.bringToFront();
                    pager.bringToFront();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation nowPlayingFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        nowPlayingFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                songView.setVisibility(View.GONE);
                slidingUpPanel.setVisibility(View.GONE);
                rL.startAnimation(mainFadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        if(openClose) {
            rL.startAnimation(mainFadeOut);
        }else{
            rL.startAnimation(nowPlayingFadeOut);
        }
    }

    public void requestRecordAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO }, jollyUtils.RECORD_AUDIO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && data != null && data.getData() != null) {
            if(requestCode == 99) {
                //pick image request
                Intent intent = new Intent("pickImageRequest");
                intent.putExtra("uri", data.getData());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }

            if(requestCode == 157) {

            }

            if(requestCode == jollyUtils.SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();

                Intent intent = new Intent("changeEditImage");
                intent.putExtra("type", data.getStringExtra("type"));
                intent.putExtra("loc", selectedImageUri.toString());
                jollyUtils.sendLocalBroadcast(intent);
            }
        }

    }
}