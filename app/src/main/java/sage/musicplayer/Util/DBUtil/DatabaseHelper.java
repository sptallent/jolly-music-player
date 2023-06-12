package sage.musicplayer.Util.DBUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.File;
import java.util.ArrayList;

import sage.musicplayer.Util.MusicUtil.MusicUtils;
import sage.musicplayer.Util.MusicUtil.Playlist;
import sage.musicplayer.Util.MusicUtil.Song;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "JollyMusic.db";
    private static final String FP_TABLE = "fp_table";
    private static final String MBID_TABLE = "mbid_table";
    private static final String PLAYLIST_TABLE = "pl_table";
    private static final String PLAYLIST_SONGS_TABLE = "pl_songs_table";
    private static final String MUSIC_STORE_TABLE = "music_store_table";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FP_TABLE + " (fp TEXT NOT NULL UNIQUE, path text NOT NULL, mbid TEXT NOT NULL);");
        db.execSQL("CREATE TABLE " + MBID_TABLE + " (mbid TEXT NOT NULL UNIQUE, title TEXT NOT NULL, album_name TEXT, artist_name TEXT, genre TEXT, release_date TEXT, album_art TEXT, track_num INTEGER, track_total INTEGER);");
        db.execSQL("CREATE TABLE " + PLAYLIST_TABLE + " (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE);");
        db.execSQL("CREATE TABLE " + PLAYLIST_SONGS_TABLE + " (id INTEGER NOT NULL, s_id INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE " + MUSIC_STORE_TABLE + " (s_id INTEGER NOT NULL, title TEXT NOT NULL, album_name TEXT, artist_name TEXT, genre TEXT, album_art TEXT, path TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MBID_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PLAYLIST_SONGS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + MUSIC_STORE_TABLE);
        onCreate(db);
    }

    public void delAllFingerprintAndMBID() {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql_fp = "DELETE FROM " + FP_TABLE;
        db.execSQL(sql_fp);
        String sql_mbid = "DELETE FROM " + MBID_TABLE;
        db.execSQL(sql_mbid);
        db.close();
    }

    public long addPlaylist(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        return db.insert(PLAYLIST_TABLE, null, values);
    }

    public long addSongToPlaylist(int p_id, int s_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", p_id);
        values.put("s_id", s_id);
        return db.insert(PLAYLIST_SONGS_TABLE, null, values);
    }

    public void delPlaylist(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PLAYLIST_TABLE, "id = ?", new String[] {String.valueOf(id)});
        db.delete(PLAYLIST_SONGS_TABLE, "id = ?", new String[] {String.valueOf(id)});
    }

    public void delSongFromPlaylist(int p_id, int s_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(PLAYLIST_SONGS_TABLE, "id = ? AND s_id = ?", new String[] {String.valueOf(p_id), String.valueOf(s_id)});
    }

    public Playlist getPlaylist(int p_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Playlist playlist = new Playlist();
        String query = "SELECT name FROM " + PLAYLIST_TABLE + " WHERE id = ? LIMIT 1;";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(p_id)});
        if(cursor.moveToFirst()) {
            playlist.setPlaylistName(cursor.getString(0));
        }
        cursor.close();

        query = "SELECT s_id FROM " + PLAYLIST_SONGS_TABLE + " WHERE id = ?;";
        cursor = db.rawQuery(query, new String[] {String.valueOf(p_id)});
        cursor.moveToFirst();
        while(cursor.getCount() > 0 && !cursor.isAfterLast()) {
            playlist.addSong(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();
        playlist.setId(p_id);
        return playlist;
    }

    public ArrayList<Playlist> getAllPlaylists() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Playlist> temp = new ArrayList<>();
        String query = "SELECT id FROM " + PLAYLIST_TABLE + ";";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        while(cursor.getCount() > 0 && !cursor.isAfterLast()) {
            temp.add(getPlaylist(cursor.getInt(0)));
            cursor.moveToNext();
        }
        cursor.close();
        return temp;
    }

    public Playlist getPlaylistByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Playlist temp = new Playlist();
        String query = "SELECT id FROM " + PLAYLIST_TABLE + " WHERE name = ? LIMIT 1;";
        Cursor cursor = db.rawQuery(query, new String[] {name});
        if(cursor.moveToFirst()) {
            temp.setId(cursor.getInt(0));
        }
        cursor.close();

        return temp;
    }

    public long addFingerprint(String fp, String path, String mbid) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("fp", fp);
        values.put("path", path);
        values.put("mbid", mbid);
        return db.insert(FP_TABLE, null, values);
    }

    public long addRecording(String mbid, String title, String artist_name, String album_name, String genre, String release_date, String album_art, int track_num, int track_total) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("mbid", mbid);
        values.put("title", title);
        values.put("artist_name", artist_name);
        values.put("album_name", album_name);
        values.put("genre", genre);
        values.put("release_date", release_date);
        values.put("album_art", album_art);
        values.put("track_num", track_num);
        values.put("track_total", track_total);
        return db.insert(MBID_TABLE, null, values);
    }

    public long addMusicStore(int sid, String title, String artist_name, String album_name, String genre, String album_art, String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("s_id", sid);
        values.put("title", title);
        values.put("artist_name", artist_name);
        values.put("album_name", album_name);
        values.put("genre", genre);
        values.put("album_art", album_art);
        values.put("path", path);
        return db.insert(MUSIC_STORE_TABLE, null, values);
    }

    public Song getMusicStore(long s_id) {
        if (s_id != -1) {
            SQLiteDatabase db = this.getReadableDatabase();
            try {
                String query = "SELECT title, artist_name, album_name, genre, album_art, path FROM " + MUSIC_STORE_TABLE + " WHERE s_id = ?;";
                Cursor cursor = db.rawQuery(query, new String[]{Long.toString(s_id)});
                Song song = new Song();
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    song.setId(s_id);
                    song.setTitle(cursor.getString(0));
                    song.setArtist(cursor.getString(1));
                    song.setAlbum_name(cursor.getString(2));
                    song.setGenre(cursor.getString(3));
                    song.setAlbum_art(cursor.getString(4));
                    song.setPath(cursor.getString(5));
                } else {
                    return null;
                }
                cursor.close();
                return song;
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    public void delMusicStore(int s_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(MUSIC_STORE_TABLE, "s_id = ?", new String[] {String.valueOf(s_id)});
    }

    public DbFingerprint getFingerprint(String fp) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT fp, mbid FROM " + FP_TABLE + " WHERE fp = ?;";
        Cursor cursor = db.rawQuery(query, new String[] {fp});
        DbFingerprint dbFingerprint = null;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            dbFingerprint =  new DbFingerprint(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        return dbFingerprint;
    }

    public DbFingerprint getFingerprintByPath(String path) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "Select fp, mbid FROM " + FP_TABLE + " WHERE path = ?;";
        Cursor cursor = db.rawQuery(query, new String[] {path});
        DbFingerprint dbFingerprint = null;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            dbFingerprint = new DbFingerprint(cursor.getString(0), cursor.getString(1));
        }
        cursor.close();
        return dbFingerprint;
    }

    public DbMbid getMbid(String mbid) {
        if(mbid != null) {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT mbid, title, artist_name, album_name, genre, release_date, album_art, track_num, track_total FROM " + MBID_TABLE + " WHERE mbid = ?;";
            Cursor cursor = db.rawQuery(query, new String[]{mbid});
            DbMbid dbMbid = null;
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                dbMbid = new DbMbid(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7), cursor.getInt(8));
            }
            cursor.close();
            return dbMbid;
        }
        return null;
    }

    public void delFingerprint(String fp) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(FP_TABLE, "fp = ?", new String[] {fp});
    }

    public void delMbid(String mbid) {
        SQLiteDatabase db = this.getReadableDatabase();

        DbMbid dbMbid = getMbid(mbid);
        if(dbMbid != null) {
            String album_art = dbMbid.getAlbum_art();
            db.delete(MBID_TABLE, "mbid = ?", new String[]{mbid});
            if (album_art != null && album_art.length() != 0) {
                File image = new File(album_art);
                image.delete();
            }
        }
    }

    public void delAlbumArtFP(String mbid) {
        SQLiteDatabase db = this.getReadableDatabase();
        DbMbid dbMbid = getMbid(mbid);
        String album_art = dbMbid.getAlbum_art();

        ContentValues values = new ContentValues();
        values.put("album_art", "");
        db.update(MBID_TABLE, values,"mbid = ?", new String[]{mbid});

        if (album_art != null && album_art.length() != 0) {
            File image = new File(album_art);
            image.delete();
        }
    }

    public void delAlbumArt(long s_id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Song song = getMusicStore(s_id);
        String album_art = song.getAlbum_art();

        ContentValues values = new ContentValues();
        values.put("album_art", "");
        db.update(MUSIC_STORE_TABLE, values,"s_id = ?", new String[]{String.valueOf(s_id)});

        if (album_art != null && album_art.length() != 0) {
            File image = new File(album_art);
            image.delete();
        }
    }
}

