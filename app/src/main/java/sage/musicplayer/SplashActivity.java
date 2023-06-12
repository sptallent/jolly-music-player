package sage.musicplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.HandlerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.wasabeef.glide.transformations.BlurTransformation;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.DBUtil.DbFingerprint;
import sage.musicplayer.Util.DBUtil.DbMbid;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Song;

public class SplashActivity extends AppCompatActivity {

    LinearLayout splashContainer;
    RelativeLayout splashBg;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    DisplayMetrics displayMetrics;
    int screenHeight;
    int screenWidth;
    JollyUtils jollyUtils;
    ExecutorService executorService;
    Handler mainThreadHandler;
    ProgressBar progressBar;
    //TextView progressText;
    MusicUtils musicUtils;
    DatabaseHelper dbHelper;
    AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UiModeManager uiModeManager = (UiModeManager) getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        settings = getApplicationContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
        editor = settings.edit();
        String prefThemeName = settings.getString("theme", null);
        if (prefThemeName == null) {
            if (uiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
                prefThemeName = "darkTheme";
            } else {
                prefThemeName = "lightTheme";
            }
            editor.putString("theme", prefThemeName);
            editor.apply();//commit
        }
        if (prefThemeName.equals("lightTheme"))
            setTheme(R.style.ThemeLight);
        else if (prefThemeName.equals("darkTheme"))
            setTheme(R.style.ThemeDark);

        setContentView(R.layout.activity_splash);
        //getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//prevents adding to back stack

        splashBg = findViewById(R.id.splashBg);

        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        jollyUtils = new JollyUtils(getApplicationContext());

        //executorService.execute(backgroundRunnable);
        boolean transparent = settings.getBoolean("bgOptions", false);
        Drawable drawable = getResources().getDrawable(R.drawable.splash);
        if(!transparent)
            drawable = jollyUtils.blurDrawable(getApplicationContext(), drawable, 25);
        Glide.with(getApplicationContext()).load(drawable).override(screenWidth, screenHeight).centerCrop().into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                splashBg.setBackground(resource);
            }
        });

        splashContainer = findViewById(R.id.splash_container);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(splashContainer, "alpha", 1f, 0f);
        fadeOut.setDuration(250);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(splashContainer, "alpha", 0f, 1f);
                fadeIn.setDuration(250);
                splashContainer.setVisibility(View.VISIBLE);
                fadeIn.start();
            }
        });
        fadeOut.start();

        progressBar = findViewById(R.id.splash_progress);
        //progressText = findViewById(R.id.splash_progress_text);


        executorService = Executors.newFixedThreadPool(3);
        mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
        musicUtils = new MusicUtils(getContentResolver(), getApplicationContext());
        dbHelper = new DatabaseHelper(getApplicationContext());

        mainThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestReadStorage();
            }
        }, 500);
    }

    public void moveToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.do_you_want_to_exit)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SplashActivity.this.finishAffinity();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .create()
                .show();
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
                        moveToMain();
                    }
                })
                .create();
        alertDialog.show();
    }

    public void requestReadStorage() {
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
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    getSongList(getApplicationContext());
                }
            });
        }
    }

    public void getSongList(Context c) {
        ArrayList<Song> songList = new ArrayList<>();
        ContentResolver musicResolver = c.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {

            //get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.DURATION);
            int albumColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
            int artColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);//int artColumn = musicCursor.getColumnIndex(MediaStore.Images.Media._ID);
            int genreColumn = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                genreColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.GENRE);
            }
            int dateAddedColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

            int nameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int pathColumn;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            else
                pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH);

            int curMax = 100;
            if(musicCursor.getCount() > -1) {
                curMax = musicCursor.getCount();
                final int max = curMax;
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setMax(max);
                        //progressText.setText("0 / " + max);
                    }
                });
            }
            //add songs to list
            int d = 0;
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(durationColumn);
                long thisAlbumID = musicCursor.getLong(artColumn);
                String thisDateAdded = musicCursor.getString(dateAddedColumn);
                String fullPath = musicCursor.getString(pathColumn)+musicCursor.getString(nameColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisGenre = "";
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    thisGenre = musicCursor.getString(genreColumn);
                }
                Song s = new Song(thisId, thisTitle, thisArtist, thisDuration, thisAlbumID);
                s.setGenre(thisGenre);
                s.setAlbum_name(thisAlbum);
                if(dbHelper.getMusicStore(thisId) == null) {
                    Bitmap bm = musicUtils.getAlbumArt(thisAlbumID);
                    if(bm != null)
                        dbHelper.addMusicStore((int) thisId, thisTitle, thisArtist, thisAlbum, thisGenre, musicUtils.storeAlbumArtInternal(bm), fullPath);
                    else
                        dbHelper.addMusicStore((int) thisId, thisTitle, thisArtist, thisAlbum, thisGenre, musicUtils.storeAlbumArtInternal(BitmapFactory.decodeResource(getResources(), R.drawable.new_album_art)), fullPath);
                }
                s.setDuration(thisDuration);
                /*
                s.setDateAdded(thisDateAdded);
                s.setPath(fullPath);
                s.setAlbum_ID(thisAlbumID);

                DbFingerprint dbFingerprint;
                DbMbid dbMbid = null;
                dbFingerprint = dbHelper.getFingerprintByPath(fullPath);
                if(!fullPath.equals("") && dbFingerprint != null) {
                    s.setFingerprint(dbFingerprint.getFingerprint());
                    dbMbid = dbHelper.getMbid(dbFingerprint.getMbid());
                }

                if(dbMbid != null) {
                    if(dbMbid.getTitle() != null && !dbMbid.getTitle().equals(""))
                        s.setTitle(dbMbid.getTitle());
                    if(dbMbid.getAlbum_name() != null && !dbMbid.getAlbum_name().equals(""))
                        s.setAlbum_name(dbMbid.getAlbum_name());
                    if(dbMbid.getAlbum_art() != null && !dbMbid.getAlbum_art().equals(""))
                        s.setAlbum_art(dbMbid.getAlbum_art());
                    if(dbMbid.getArtist_name() != null && !dbMbid.getArtist_name().equals(""))
                        s.setArtist(dbMbid.getArtist_name());
                    if(dbMbid.getGenre() != null && !dbMbid.getGenre().equals(""))
                        s.setGenre(s.getGenre() + ", " + dbMbid.getGenre());
                    if(dbMbid.getMbid() != null && !dbMbid.getMbid().equals(""))
                        s.setMbid(dbMbid.getMbid());
                    if(dbMbid.getRelease_date() != null && !dbMbid.getRelease_date().equals(""))
                        s.setRelease_date(dbMbid.getRelease_date());
                    if(dbMbid.getTrack_num() != 0 && dbMbid.getTrack_num() != -1)
                        s.setTrack_number(String.valueOf(dbMbid.getTrack_num()));
                    if(dbMbid.getTrack_total() != 0 && dbMbid.getTrack_total() != -1)
                        s.setTrack_total(String.valueOf(dbMbid.getTrack_total()));
                }

                if(settings.getBoolean("include_ringtones", false)) {
                    if(!s.isNull())
                        songList.add(s);
                }else{
                    if(!s.isNull() && !s.getPath().contains("/Notifications/"))
                        songList.add(s);
                }*/

                d++;
                final int e = d;
                final int max = curMax;
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(e);
                        //progressText.setText(e + " / " + max);
                    }
                });
            }
            while (musicCursor.moveToNext());
        }

        if(musicCursor != null)
            musicCursor.close();

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                moveToMain();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == jollyUtils.READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        getSongList(getApplicationContext());
                    }
                });
            } else {
                // Permission is denied
                Toast.makeText(this, R.string.read_perm_require,
                        Toast.LENGTH_SHORT).show();
                moveToMain();
            }
        }
    }

}
