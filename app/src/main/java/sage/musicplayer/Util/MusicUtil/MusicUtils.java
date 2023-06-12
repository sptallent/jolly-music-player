package sage.musicplayer.Util.MusicUtil;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import sage.musicplayer.ListAdapter.EditViewPagerAdapter;
import sage.musicplayer.ListAdapter.PlaylistAddNamesAdapter;
import sage.musicplayer.R;
import sage.musicplayer.Service.MusicService;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.DBUtil.DbFingerprint;
import sage.musicplayer.Util.DBUtil.DbMbid;
import sage.musicplayer.Util.JollyUtils;

public class MusicUtils {

    ContentResolver contentResolver;
    DatabaseHelper dbHelper;
    Context context;
    JollyUtils jollyUtils;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    ImageView editSongAlbumImage;
    ImageView editFPAlbumImage;
    PopupWindow popupWindow;
    ViewPager viewPager;

    private BroadcastReceiver changeEditImageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Uri loc = Uri.parse(intent.getStringExtra("loc"));
            ImageView imageView = null;
            if(popupWindow != null && popupWindow.isShowing() && viewPager != null) {
                if (viewPager.getCurrentItem() == 0 && editSongAlbumImage != null) {
                    imageView = editSongAlbumImage;
                } else if (viewPager.getCurrentItem() == 1 && editFPAlbumImage != null) {
                    imageView = editFPAlbumImage;
                }
                if (imageView != null) {
                    try {
                        Glide.with(context).load(getBitmapFromUri(loc)).placeholder(R.drawable.new_album_art).centerCrop().into(imageView);
                    }catch(IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    };

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public MusicUtils(ContentResolver cr, Context c) {
        contentResolver = cr;
        context = c;
        jollyUtils = new JollyUtils(context);
        dbHelper = new DatabaseHelper(context);

        settings = context.getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
        editor = context.getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE).edit();

        LocalBroadcastManager.getInstance(context).registerReceiver(changeEditImageReceiver, new IntentFilter("changeEditImage"));
    }

    public ArrayList<Song> removeSongFromList(int id, ArrayList<Song> songs) {
        ArrayList<Song> temp = songs;
        for(int i= 0; i < temp.size(); i++) {
            Song s = temp.get(i);
            if(s.getID() == (long)id) {
                temp.remove(i);
                break;
            }
        }
        return temp;
    }

    public Bitmap getAlbumArt(Long album_id)
    {
        Bitmap bm = null;

        try
        {
            final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);

            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        return bm;
    }

    public String convertFromMilli(int milli) {
        String temp;
        long min = TimeUnit.MILLISECONDS.toMinutes(milli);
        long sec = TimeUnit.MILLISECONDS.toSeconds(milli) - TimeUnit.MINUTES.toSeconds(min);

        if(sec == 0) {
            temp = min + ":00";
        }else if(sec < 10) {
            temp = min + ":0" + sec;
        }else {
            temp = min + ":" + sec;
        }
        return temp;
    }

    public static String getPathFromURI(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public ArrayList<Song> getSongList(Context c) {
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
                genreColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.GENRE);
            }
            int dateAddedColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

            int nameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int pathColumn;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            else
                pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH);

            //add songs to list
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
                if(dbHelper.getMusicStore(thisId) != null)
                    s = dbHelper.getMusicStore(thisId);
                else
                    dbHelper.addMusicStore((int)thisId, thisTitle, thisArtist, thisAlbum, thisGenre, storeAlbumArtInternal(getAlbumArt(thisAlbumID)), fullPath);
                s.setDuration(thisDuration);
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
                }

            }
            while (musicCursor.moveToNext());
        }

        if(musicCursor != null)
            musicCursor.close();
        return songList;
    }

    public TreeMap<String, ArrayList<Song>> getArtists(ArrayList<Song> songList) {
        TreeMap<String, ArrayList<Song>> temp = new TreeMap<>();
        ArrayList<String> artistNames = new ArrayList<String>();

        String artist;
        int c = 0;
        for (Song song : songList) {
            artist = song.getArtist();
            for (String str : artistNames) {
                if (str.equals(artist))
                    c++;
            }
            if (c == 0)
                artistNames.add(artist);
            c = 0;
        }

        for (String artistName : artistNames) {
            temp.put(artistName, getSongsUsingArtist(artistName, songList));
        }

        return temp;
    }

    public ArrayList<Album> getAlbumList(ArrayList<Song> songList) {
        ArrayList<Album> tempAlbums = new ArrayList<>();
        ArrayList<Song> tempSongs = new ArrayList<>(songList);

        for(Song s : tempSongs) {
            Album tempA = new Album();
            if(s.getAlbum_name() != null)
                tempA.setArtist(s.getArtist());
            if(s.getAlbum_name() != null)
                tempA.setName(s.getAlbum_name());

            boolean exists = false;
            int index = -1;
            for(int i = 0; i < tempAlbums.size(); i++) {
                if((tempA.getArtist() != null && tempA.getName() != null && tempAlbums.get(i).getArtist() != null && tempAlbums.get(i).getName() != null) && (tempA.getArtist().equals(tempAlbums.get(i).getArtist()) && tempA.getName().equals(tempAlbums.get(i).getName()))) {
                    exists = true;
                    index = i;
                    break;
                }
            }

            if(exists) {
                tempA = tempAlbums.get(index);
                tempAlbums.remove(index);
            }else {
                tempA.setAlbum_art(s.getAlbum_art());
                tempA.setAlbum_art(s.getAlbum_art());
                tempA.setAlbum_id(s.getAlbumID());
                tempA.setMbid(s.getAlbum_mbid());
                tempA.setArtist_mbid(s.getArtist_mbid());
                tempA.setTrack_count(s.getTrack_total());
                tempA.setRelease_date(s.getRelease_date());
            }
            tempA.addSong(s);
            tempAlbums.add(tempA);
        }

        return tempAlbums;
    }

    public HashMap<String, ArrayList<Song>> getGenreList(ArrayList<Song> songList) {
        HashMap<String, ArrayList<Song>> tempList = new HashMap<>();

        for(Song s : songList) {
            if (s.getGenre() != null) {
                for (String genre : s.getGenre().split(",")) {
                    if (genre.length() > 0) {
                        genre = genre.substring(0, 1).toUpperCase() + genre.substring(1);
                        ArrayList<Song> sList;
                        if (tempList.containsKey(genre)) {
                            sList = tempList.get(genre);
                            sList.add(s);
                            tempList.remove(genre);
                            tempList.put(genre, sList);
                        } else {
                            sList = new ArrayList<>();
                            sList.add(s);
                            tempList.put(genre, sList);
                        }
                    }
                }
            }
        }

        return tempList;
    }

    public Song getSong(long id) {
        Song temp = new Song();

        return temp;
    }

    public void deleteTracks(Context context, Activity activity, long [] list, MusicService musicService) {
        for (long l : list) {
            if (musicService != null && musicService.isPng()) {
                if (musicService.getSongId() == l) {
                    Toast.makeText(context, context.getResources().getString(R.string.unable_to_delete), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        Dexter.withContext(context)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        String message = "";
                        String title = "";
                        if (list.length > 1) {
                            message = "Permanently delete these songs from device";
                            title = "Are you sure you want to permanently delete multiple songs from your device?";
                        } else {
                            message = "Permanently delete song from device";
                            title = "Are you sure you want to permanently delete this song from your device?";
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogCustom)
                                .setTitle(title)
                                .setMessage(message)
                                .setNegativeButton(R.string.cancel, (dialog, i) -> dialog.dismiss())
                                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                                    for (long l : list) {
                                        Intent deleteIntent = new Intent("songViewDeleted");
                                        deleteIntent.putExtra("song_id", l);
                                        deleteIntent.putExtra("song_title", getSong(l).getTitle());

                                        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, l);
                                        ArrayList<Uri> songUriList = new ArrayList<>();
                                        songUriList.add(songUri);
                                        try {
                                            context.getContentResolver().delete(songUri, null, null);
                                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                                String filePath = getFilePathFromId(context, l);
                                                if(filePath != null) {
                                                    File file = new File(filePath);
                                                    boolean deleted = file.delete();
                                                }
                                            }
                                        }catch(SecurityException e) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                PendingIntent pi = MediaStore.createDeleteRequest(context.getContentResolver(), songUriList);
                                                try {
                                                    activity.startIntentSenderForResult(pi.getIntentSender(), 157, null, 0, 0, 0);
                                                } catch (IntentSender.SendIntentException ex) {
                                                    ex.printStackTrace();
                                                }

                                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                try {
                                                    context.getContentResolver().delete(songUri, null, null);
                                                } catch (SecurityException ex) {
                                                    openDocument(activity, songUri);
                                                }
                                            }
                                        }
                                        jollyUtils.sendLocalBroadcast(deleteIntent);
                                        Song temp = getFingerprintInfo(l);
                                        dbHelper.delMbid(temp.getMbid());
                                        dbHelper.delFingerprint(temp.getFingerprint());
                                        dbHelper.delMusicStore((int)l);
                                    }
                                }).create();
                        alertDialog.show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // Permission is denied, do something
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void openDocument(Activity a, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setDataAndType(uri, "audio/*");
        a.startActivityForResult(intent, 66);
    }

    public int getPositionInList(long s_id, ArrayList<Song> sList) {
        int pos = -1;
        for(int i = 0; i < sList.size(); i++) {
            long long1 = s_id;
            long long2  = sList.get(i).getID();
            if(long1 == long2) {
                pos = i;
                break;
            }
        }
        return pos;
    }

    public void playNextAdd(Song song_obj) {
        Intent intent = new Intent("addPlayNext");
        intent.putExtra("song_obj", (Serializable)song_obj);
        jollyUtils.sendLocalBroadcast(intent);
    }

    public void showPlaylists(long[] song_ids, View itemView) {
        PopupWindow plAvailablePopupWindow;
        LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View availablePlaylistView = inflater.inflate(R.layout.pl_display_names, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = 300;
        plAvailablePopupWindow = new PopupWindow(availablePlaylistView, width, height, true);

        ArrayList<Playlist> pList = dbHelper.getAllPlaylists();

        ListView playlistAddNames = plAvailablePopupWindow.getContentView().findViewById(R.id.playlistAddNames);
        PlaylistAddNamesAdapter addNamesAdapter = new PlaylistAddNamesAdapter(context, pList, song_ids, plAvailablePopupWindow);
        playlistAddNames.setAdapter(addNamesAdapter);

        plAvailablePopupWindow.showAtLocation(itemView, Gravity.CENTER, 0, 0);
    }

    public ArrayList<Song> getSongsUsingArtist(String artist, ArrayList<Song> songList) {
        ArrayList<Song> temp = new ArrayList<>();

        for (Song song : songList) {
            if (song.getArtist().equals(artist))
                temp.add(song);
        }

        return temp;
    }

    public void showEditDialog(long s_id, Activity activity, View itemView) {
        View editViewMediaStore;
        View editViewFingerprint;
        LayoutInflater inflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        EditViewPagerAdapter editAdapter = new EditViewPagerAdapter();
        editViewMediaStore = inflater.inflate(R.layout.edit_view_media_store, null);
        editViewFingerprint = inflater.inflate(R.layout.edit_view_fingerprint, null);
        editAdapter.addView(editViewMediaStore, "MediaStore");
        editAdapter.addView(editViewFingerprint, "Fingerprint");

        View popupView = inflater.inflate(R.layout.edit_song_popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        Button editCloseButton = popupView.findViewById(R.id.edit_close_button);
        Button editSaveButton = popupView.findViewById(R.id.edit_save_button);
        editCloseButton.setOnClickListener(v -> {
            popupWindow.dismiss();
        });

        viewPager = popupView.findViewById(R.id.edit_view_pager);
        TabLayout tabLayout = popupView.findViewById(R.id.edit_tab_layout);
        viewPager.setAdapter(editAdapter);
        tabLayout.setupWithViewPager(viewPager);

        editSongAlbumImage = editViewMediaStore.findViewById(R.id.edit_album_image);
        editSongAlbumImage.setOnClickListener(v -> {
            //Context wrapper = new ContextThemeWrapper(activity.getApplicationContext(), R.style.popupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuInflater menu_inflater = popupMenu.getMenuInflater();
            menu_inflater.inflate(R.menu.menu_album_art_change, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.menu_edit_choose:
                        selectImage("media", activity);
                        break;
                    case R.id.menu_edit_remove:
                        Glide.with(activity.getApplicationContext()).load(context.getResources().getDrawable(R.drawable.new_album_art)).placeholder(R.drawable.new_album_art).into(editSongAlbumImage);
                        deleteAlbumArt(s_id);
                        editViewMediaStore.setTag("none");
                        break;
                    /*case R.id.menu_edit_set:
                        setAsDefaultBackground(editSongAlbumImage.getDrawable());
                        break;*/
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });
        EditText editSongTitle = editViewMediaStore.findViewById(R.id.edit_song_title);
        EditText editSongArtist = editViewMediaStore.findViewById(R.id.edit_artist_name);
        EditText editSongAlbum = editViewMediaStore.findViewById(R.id.edit_album_title);
        EditText editGenre = editViewMediaStore.findViewById(R.id.edit_genre);
        TextView editPath = editViewMediaStore.findViewById(R.id.edit_path);

        Song s = getMusicStoreInfo(s_id);
        editSongTitle.setText(s.getTitle());
        editSongArtist.setText(s.getArtist());
        editSongAlbum.setText(s.getAlbum_name());
        editGenre.setText(s.getGenre());
        editPath.setText(s.getPath());
        Glide.with(activity.getApplicationContext()).load(s.getAlbum_art()).placeholder(activity.getApplicationContext().getResources().getDrawable(R.drawable.new_album_art)).into(editSongAlbumImage);

        editFPAlbumImage = editViewFingerprint.findViewById(R.id.edit_fp_album_image);
        editFPAlbumImage.setOnClickListener(v -> {
            //Context wrapper = new ContextThemeWrapper(activity.getApplicationContext(), R.style.popupMenuStyle);
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuInflater menu_inflater = popupMenu.getMenuInflater();
            menu_inflater.inflate(R.menu.menu_album_art_change, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch(item.getItemId()) {
                    case R.id.menu_edit_choose:
                        selectImage("fp", activity);
                        break;
                    case R.id.menu_edit_remove:
                        Glide.with(context).load(context.getResources().getDrawable(R.drawable.new_album_art)).placeholder(R.drawable.new_album_art).into(editFPAlbumImage);
                        deleteFPAlbumArt(s_id);
                        editViewFingerprint.setTag("none");
                        break;
                    /*case R.id.menu_edit_set:
                        setAsDefaultBackground(editSongAlbumImage.getDrawable());
                        break;*/
                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
        });
        EditText editFPSongTitle = editViewFingerprint.findViewById(R.id.edit_fp_song_title);
        EditText editFPSongArtist = editViewFingerprint.findViewById(R.id.edit_fp_artist_name);
        EditText editFPSongAlbum = editViewFingerprint.findViewById(R.id.edit_fp_album_title);
        EditText editFPGenre = editViewFingerprint.findViewById(R.id.edit_fp_genre);
        EditText editFPRelease = editViewFingerprint.findViewById(R.id.edit_fp_release);
        EditText editTrackNum = editViewFingerprint.findViewById(R.id.edit_fp_track);
        TextView editFP = editViewFingerprint.findViewById(R.id.edit_fp);

        Song s_fp = getFingerprintInfo(s_id);
        editFPSongTitle.setText(s_fp.getTitle());
        editFPSongArtist.setText(s_fp.getArtist());
        editFPSongAlbum.setText(s_fp.getAlbum_name());
        editFPGenre.setText(s_fp.getGenre());
        editFPRelease.setText(s_fp.getRelease_date());
        editTrackNum.setText(s_fp.getTrack_number());
        if(s_fp.getFingerprint() != null) {
            editFP.setText(context.getString(R.string.fingerprint_text) + s_fp.getFingerprint());

            editFPSongTitle.setEnabled(true);
            editFPSongArtist.setEnabled(true);
            editFPSongAlbum.setEnabled(true);
            editFPGenre.setEnabled(true);
            editFPRelease.setEnabled(true);
            editTrackNum.setEnabled(true);
            editFPAlbumImage.setClickable(true);
            editFPAlbumImage.setFocusable(true);
        }else {
            editFP.setText(R.string.fingerprint_text_not_available);

            editFPSongTitle.setEnabled(false);
            editFPSongArtist.setEnabled(false);
            editFPSongAlbum.setEnabled(false);
            editFPGenre.setEnabled(false);
            editFPRelease.setEnabled(false);
            editTrackNum.setEnabled(false);
            editFPAlbumImage.setClickable(false);
            editFPAlbumImage.setFocusable(false);
        }
        Glide.with(activity.getApplicationContext()).load(s_fp.getAlbum_art()).placeholder(activity.getApplicationContext().getResources().getDrawable(R.drawable.new_album_art)).into(editFPAlbumImage);

        editSaveButton.setOnClickListener(v -> {
            storeMusicInfo(s_id, editSongTitle.getText().toString(), editSongArtist.getText().toString(), editSongAlbum.getText().toString(), editGenre.getText().toString(), getBitmapFromImageView(editSongAlbumImage), s.getPath());
            storeFPInfo(s_id, editFPSongTitle.getText().toString(), editFPSongArtist.getText().toString(), editFPSongAlbum.getText().toString(), editFPGenre.getText().toString(), getBitmapFromImageView(editFPAlbumImage), editFPRelease.getText().toString(), editTrackNum.getText().toString());
            jollyUtils.sendLocalBroadcast(new Intent("readPermGranted"));
            popupWindow.dismiss();
        });

        popupWindow.setFocusable(true);
        popupWindow.update();
        popupWindow.showAtLocation(itemView.getRootView(), Gravity.CENTER, 0, 0);
    }

    public Bitmap getBitmapFromImageView(ImageView iv) {
        // Get the drawable from the ImageView
        Drawable drawable = iv.getDrawable();

        // Create a bitmap with the same dimensions as the drawable
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // Create a canvas to draw the drawable onto the bitmap
        Canvas canvas = new Canvas(bitmap);

        // Draw the drawable onto the bitmap
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void deleteSongFromDevice(long s_id, Context c, Activity activity, MusicService musicService) {
        long[] songs = new long[1];
        songs[0] = s_id;

        deleteTracks(c, activity, songs, musicService);
    }



    public Song getMusicStoreInfo(long s_id) {
        Song temp = dbHelper.getMusicStore(s_id);
        if(temp == null)
            temp = new Song();
        else
            return temp;

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection_song = new String[0];
        String pathColumn;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            pathColumn = MediaStore.Audio.Media.DATA;
        else
            pathColumn = MediaStore.Audio.Media.RELATIVE_PATH;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            projection_song = new String[]{
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.GENRE,
                    pathColumn,
                    MediaStore.Audio.Media.DISPLAY_NAME
            };
        }

        String selection = MediaStore.Audio.Media._ID + " = ?";
        String[] selectionArgs = { String.valueOf(s_id) };

        Cursor cursor = contentResolver.query(uri, projection_song, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(0);
            String album = cursor.getString(1);
            String artist = cursor.getString(2);
            String album_id = cursor.getString(3);
            String genre = "";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                genre = cursor.getString(4);
            }
            String path = cursor.getString(5)+cursor.getString(6);
            temp.setTitle(title);
            temp.setAlbum_name(album);
            temp.setArtist(artist);
            temp.setAlbum_ID(Long.parseLong(album_id));
            temp.setGenre(genre);
            temp.setPath(path);

            Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUriWithId = ContentUris.withAppendedId(albumArtUri, Long.parseLong(album_id));

            contentResolver = context.getContentResolver();

            String[] projection_album = { MediaStore.Images.Media.DATA };

            cursor = contentResolver.query(albumArtUriWithId, projection_album, null, null, null);
        }
        cursor.close();

        return temp;
    }

    public Song getFingerprintInfo(long s_id) {
        Song temp = getMusicStoreInfo(s_id);
        temp.setTitle("");
        temp.setAlbum_name("");
        temp.setArtist("");
        temp.setAlbum_ID(-1);
        temp.setGenre("");
        DbFingerprint dbFingerprint;
        DbMbid dbMbid = null;
        dbFingerprint = dbHelper.getFingerprintByPath(temp.getPath());
        if(!temp.getPath().equals("") && dbFingerprint != null) {
            temp.setFingerprint(dbFingerprint.getFingerprint());
            dbMbid = dbHelper.getMbid(dbFingerprint.getMbid());
        }

        if(dbMbid != null) {
            if(dbMbid.getTitle() != null && !dbMbid.getTitle().equals(""))
                temp.setTitle(dbMbid.getTitle());
            if(dbMbid.getAlbum_name() != null && !dbMbid.getAlbum_name().equals(""))
                temp.setAlbum_name(dbMbid.getAlbum_name());
            if(dbMbid.getAlbum_art() != null && !dbMbid.getAlbum_art().equals(""))
                temp.setAlbum_art(dbMbid.getAlbum_art());
            if(dbMbid.getArtist_name() != null && !dbMbid.getArtist_name().equals(""))
                temp.setArtist(dbMbid.getArtist_name());
            if(dbMbid.getGenre() != null && !dbMbid.getGenre().equals(""))
                temp.setGenre(dbMbid.getGenre());
            if(dbMbid.getRelease_date() != null && !dbMbid.getRelease_date().equals(""))
                temp.setRelease_date(dbMbid.getRelease_date());
            if(dbMbid.getTrack_num() != 0 && dbMbid.getTrack_num() != -1)
                temp.setTrack_number(String.valueOf(dbMbid.getTrack_num()));
            if(dbMbid.getTrack_total() != 0 && dbMbid.getTrack_total() != -1)
                temp.setTrack_total(String.valueOf(dbMbid.getTrack_total()));
            if(dbMbid.getMbid() != null)
                temp.setMbid(dbMbid.getMbid());
        }
        return temp;
    }

    public static String getFilePathFromId(Context context, long id) {
        String[] projection = { MediaStore.Audio.Media.DATA };
        String selection = MediaStore.Audio.Media._ID + "=?";
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            String filePath = cursor.getString(column_index);
            cursor.close();
            return filePath;
        }
        return null;
    }

    private void storeMusicInfo(long s_id, String song_title, String artist_name, String album_name, String genre, Bitmap albumArt, String path) {
        try {
            if(dbHelper.getMusicStore(s_id) != null)
                dbHelper.delMusicStore((int)s_id);
            dbHelper.addMusicStore((int)s_id, song_title, artist_name, album_name, genre, storeAlbumArtInternal(albumArt), path);
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void storeFPInfo(long s_id, String song_title, String artist_name, String album_name, String genre, Bitmap art_path, String release_date, String track_num) {
        try {
            Song s = getFingerprintInfo(s_id);
            dbHelper.getFingerprint(s.getFingerprint());
            dbHelper.delMbid(s.getMbid());
            int track_number;
            try {
                track_number = Integer.parseInt(track_num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                track_number = -1;
            }
            dbHelper.addRecording(s.getMbid(), song_title, artist_name, album_name, genre, release_date, storeAlbumArtInternal(art_path), track_number, Integer.parseInt(s.getTrack_total()));
        }catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void deleteAlbumArt(long s_id) {
        dbHelper.delAlbumArt(s_id);
    }

    public void deleteFPAlbumArt(long s_id) {
        Song s = getFingerprintInfo(s_id);
        dbHelper.delAlbumArtFP(s.getMbid());
    }

    private void selectImage(String l, Activity activity) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.putExtra("type", l);
            intent.setType("image/*");
            //loc = l;
            activity.startActivityForResult(intent, jollyUtils.SELECT_IMAGE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, jollyUtils.READ_EXTERNAL_STORAGE);
        }
    }

    public void removeFromPlaylist(int p_id, int s_id) {
        dbHelper.delSongFromPlaylist(p_id, s_id);
    }

    public String generateUniqueFileName() {
        // Get the current timestamp in milliseconds
        long timestamp = System.currentTimeMillis();

        // Generate a random UUID
        String uuid = UUID.randomUUID().toString();

        // Format the timestamp as a string using a SimpleDateFormat
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestampString = dateFormat.format(new Date(timestamp));

        // Combine the timestamp and UUID to create a unique string for the file name
        String fileName = timestampString + "_" + uuid;

        return fileName;
    }

    public String storeAlbumArtInternal(Bitmap art) {
        File internalStorage = context.getDir("JollyAlbumArt", Context.MODE_PRIVATE);
        File filePath = new File(internalStorage, generateUniqueFileName() + ".jpg");
        String artPath = filePath.toString();
        if (art == null) {
            System.out.println("Image is null... help!");
        } else {
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(filePath);
                art.compress(Bitmap.CompressFormat.JPEG, 40, fos);//80
                fos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                artPath = null;
            }
        }
        return artPath;
    }

    public String getImagePath(Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // If the URI is a Document URI, we need to check its authority to determine the document type.
            String documentId = DocumentsContract.getDocumentId(uri);
            if (MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getAuthority().equals(uri.getAuthority())) {
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{documentId.split(":")[1]};
                String[] projection = {MediaStore.Images.Media.DATA};
                try (Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        filePath = cursor.getString(columnIndex);
                    }
                }
            } else if (MediaStore.Video.Media.EXTERNAL_CONTENT_URI.getAuthority().equals(uri.getAuthority())) {
                String selection = MediaStore.Video.Media._ID + "=?";
                String[] selectionArgs = new String[]{documentId.split(":")[1]};
                String[] projection = {MediaStore.Video.Media.DATA};
                try (Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                        filePath = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (filePath == null) {
            // If we couldn't get the file path using the storage access framework, we'll use the old method of using the MediaStore.
            String[] projection = {MediaStore.Images.Media.DATA};
            try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            }
        }
        return filePath;
    }

}
