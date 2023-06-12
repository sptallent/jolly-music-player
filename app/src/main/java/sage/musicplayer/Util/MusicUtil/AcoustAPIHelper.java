package sage.musicplayer.Util.MusicUtil;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.geecko.fpcalc.FpCalc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.DBUtil.DbFingerprint;
import sage.musicplayer.Util.DBUtil.DbMbid;
import sage.musicplayer.R;

import static java.net.HttpURLConnection.HTTP_OK;

public class AcoustAPIHelper {

    AcoustThread acoustThread;
    boolean is_running = false;

    public AcoustAPIHelper() {

    }

    public void getSongInfo(Context context, ArrayList<Song> songs) {
        acoustThread = new AcoustThread(context);
        is_running = true;
        acoustThread.execute(songs);
        //String temp_url = acoust_base_url + "?client=" + API_KEY + "&meta=recordingids&format=json&duration=" + dur + "&fingerprint=" + fp;
    }

    public boolean isRunning() {
        return is_running;//acoustThread.getStatus() == AsyncTask.Status.RUNNING
    }

    public void stop() {
        is_running = false;
        acoustThread.cancel(true);
    }

}

class AcoustThread extends AsyncTask<ArrayList<Song>, MusicInfo, String> {

    private MusicInfoGrabber musicInfoGrabber;
    Context context;
    DatabaseHelper dbHelper = null;
    private final String API_KEY = "INSERT_KEY";
    final private String acoust_base_url = "https://api.acoustid.org/v2/lookup";
    int count = 0;
    int count_max = 100;

    public AcoustThread(Context c) {
        context = c;
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    protected void onProgressUpdate(MusicInfo... values) {
        super.onProgressUpdate(values);

        if(values != null && values[0] != null && values[0].getPath() == context.getResources().getString(R.string.waiting_for_internet)) {
            Intent intent = new Intent(context, AcoustAPIHelper.class);
            intent.setAction("fpFound");
            Bundle bundle = new Bundle();
            bundle.putInt("count", count);
            bundle.putInt("count_max", count_max);
            MusicInfo tempMi = new MusicInfo();
            tempMi.setPath(context.getResources().getString(R.string.waiting_for_internet));
            bundle.putSerializable("mi", values[0]);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            return;
        }

        count++;
        Intent intent;
        Bundle bundle;
        if(values[0] != null) {
            intent = new Intent(context, AcoustAPIHelper.class);
            intent.setAction("fpFound");
            bundle = new Bundle();
            bundle.putInt("count", count);
            bundle.putInt("count_max", count_max);
            bundle.putSerializable("mi", values[0]);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }else {
            intent = new Intent(context, AcoustAPIHelper.class);
            intent.setAction("fpFound");
            bundle = new Bundle();
            bundle.putInt("count", count_max);
            bundle.putInt("count_max", count_max);
            bundle.putSerializable("mi", values[0]);
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    @Override
    protected String doInBackground(ArrayList<Song>... songList) {

        for(ArrayList<Song> theSongs : songList) {
            count_max = theSongs.size();
            for (Song s : theSongs) {

                while(!hasInternetConnection()) {
                    try {
                        MusicInfo tempMi = new MusicInfo();
                        tempMi.setPath(context.getResources().getString(R.string.waiting_for_internet));
                        publishProgress(tempMi);

                        Thread.sleep(5000);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                File path = Environment.getRootDirectory();
                StatFs stat = new StatFs(path.getPath());
                long availableBlocks = stat.getAvailableBlocks();
                long blockSize = stat.getBlockSize();
                long totalFreeSpace = availableBlocks * blockSize;
                long totalFreeSpaceInMb = totalFreeSpace/ 1048576;
                if(totalFreeSpaceInMb < 300) {
                    MusicInfo tempMi = new MusicInfo();
                    tempMi.setPath("Free disk space is low");
                    publishProgress(tempMi);
                    try {
                        Thread.sleep(5000);
                    }catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                if (!isCancelled()) {
                    JSONObject jsonObject;
                    musicInfoGrabber = new MusicInfoGrabber();
                    MusicInfo mi;
                    String mbid = "";
                    String dur;
                    String fp = "";
                    DbFingerprint dbFingerprint = null;
                    DbMbid dbMbid = null;

                    dbFingerprint = dbHelper.getFingerprintByPath(s.getPath());
                    if(dbFingerprint != null)
                        dbMbid = dbHelper.getMbid(dbFingerprint.getMbid());

                    if (dbFingerprint != null && dbMbid != null) {
                        dbMbid = dbHelper.getMbid(dbFingerprint.getMbid());
                        if (dbMbid != null) {
                            MusicInfo tempMi = new MusicInfo();
                            tempMi.setSong_title(dbMbid.getTitle());
                            tempMi.setArtist_name(dbMbid.getArtist_name());
                            tempMi.setAlbum_title(dbMbid.getAlbum_name());
                            tempMi.setTrack_number(String.valueOf(dbMbid.getTrack_num()));
                            tempMi.setAlbum_track_count(String.valueOf(dbMbid.getTrack_total()));
                            tempMi.setRelease_date(dbMbid.getRelease_date());
                            tempMi.setGenre(dbMbid.getGenre());
                            tempMi.setLocal_id(s.getID());
                            tempMi.setPath(s.getPath());
                            tempMi.setAlbum_art(dbMbid.getAlbum_art());
                            publishProgress(tempMi);
                            continue;
                        }
                    }else {
                        try {
                            String[] calcArgs = {"-length", "16", MusicUtils.getPathFromURI(context, ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, s.getID()))};
                            String result = FpCalc.fpCalc(calcArgs);

                            if (result != null && result.contains("=") && result.contains("FINGERPRINT")) {
                                dur = result.substring(result.indexOf("=") + 1, result.indexOf("FINGERPRINT"));
                                fp = result.substring(result.lastIndexOf("=") + 1);

                                String temp_url = acoust_base_url + "?client=" + API_KEY + "&meta=recordingids&format=json&duration=" + dur + "&fingerprint=" + fp;

                                URL url = new URL(temp_url);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setConnectTimeout(5000);
                                conn.setRequestMethod("GET");
                                conn.addRequestProperty("User-Agent", "Music Player(Android)");

                                InputStreamReader is;
                                int status_code = conn.getResponseCode();
                                if (status_code == HTTP_OK) {
                                    is = new InputStreamReader(conn.getInputStream());
                                    BufferedReader br = new BufferedReader(is);
                                    String line;
                                    StringBuilder sb = new StringBuilder();
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line);
                                    }
                                    jsonObject = new JSONObject(sb.toString());
                                    String mbid_map;
                                    JSONArray results = null;
                                    if(jsonObject.has("results"))
                                            results = jsonObject.getJSONArray("results");
                                    if (results != null && results.length() > 0) {
                                        JSONObject firstObject = results.getJSONObject(0);
                                        if (firstObject.has("recordings")) {
                                            JSONArray recordings = firstObject.getJSONArray("recordings");
                                            if (recordings.length() > 0) {
                                                mbid_map = recordings.getString(0);
                                                mbid = (mbid_map.substring(mbid_map.indexOf("id") + 5, mbid_map.indexOf("\"}")));
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            count++;
                            continue;
                        }
                    }

                    if (!mbid.isEmpty()) {
                        try {
                            mi = musicInfoGrabber.getMusicBrainzSongInfo(mbid);
                            mi.setFingerprint(fp);
                            mi.setLocal_id(s.getID());
                            mi.setPath(s.getPath());

                            String album_art = mi.getAlbum_art();
                            if (album_art == null || album_art.equals(""))
                                mi.setAlbum_art(null);
                            else
                                mi.setAlbum_art(storeAlbumArtInternal(album_art, mbid));
                            if(mi.getTrack_number() == null || mi.getTrack_number().equals(""))
                                mi.setTrack_number("0");
                            if(mi.getAlbum_track_count() == null || mi.getAlbum_track_count().equals(""))
                                mi.setAlbum_track_count("0");
                            if(mi.getSong_title() != null) {
                                if (dbHelper.getFingerprint(fp) == null) {
                                    dbHelper.addFingerprint(fp, s.getPath(), mbid);
                                    dbHelper.addRecording(mbid, mi.getSong_title(), mi.getArtist_name(), mi.getAlbum_title(), mi.getGenre(), mi.getRelease_date(), mi.getAlbum_art(), Integer.parseInt(mi.getTrack_number()), Integer.parseInt(mi.getAlbum_track_count()));
                                }
                            }
                            publishProgress(mi);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                /*try {
                    Thread.sleep(300);
                }catch(InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
            MusicInfo tempMi = null;
            publishProgress(tempMi);
        }
        return "Scan Complete";
    }

    public String storeAlbumArtInternal(String art, String mbid) {
        File internalStorage = context.getDir("JollyAlbumArt", Context.MODE_PRIVATE);
        File filePath = new File(internalStorage, mbid + ".png");
        String art_path = filePath.toString();
        Bitmap image = null;
        try {
            image = getBitmapFromURL(art, context);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        if(image == null)
            System.out.println("image is null... help!");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            art_path = null;
        }
        return art_path;
    }

    public static Bitmap getBitmapFromURL(String src, Context c) {
        try {
            /*URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            System.out.println("Src: " + src + "\nBitmap: ");
            return BitmapFactory.decodeStream(input);*/
            return Glide.with(c).asBitmap().load(src).submit().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasInternetConnection() {
        try {
            InetAddress address = InetAddress.getByName("www.google.com");
            return !address.equals("");
        } catch (UnknownHostException e) {
            // Log error
        }
        return false;
    }

}


