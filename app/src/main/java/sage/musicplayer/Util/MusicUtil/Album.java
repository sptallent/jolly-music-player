package sage.musicplayer.Util.MusicUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class Album implements Serializable, Parcelable {

    private String name;
    private String artist;
    ArrayList<Song> albumSongs;

    private String mbid;
    private String artist_mbid;
    private String track_count;
    private String album_art;
    private String release_date;
    private long album_id;

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private Album(Parcel in) {
        name = in.readString();
        artist = in.readString();
        albumSongs = in.readArrayList(Song.class.getClassLoader());
        mbid = in.readString();
        artist_mbid = in.readString();
        track_count = in.readString();
        album_art = in.readString();
        release_date = in.readString();
        album_id = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(artist);
        dest.writeList(albumSongs);
        dest.writeString(mbid);
        dest.writeString(artist_mbid);
        dest.writeString(track_count);
        dest.writeString(album_art);
        dest.writeString(release_date);
        dest.writeLong(album_id);
    }

    public Album() {
        albumSongs = new ArrayList<>();
    }

    public Album(String albumName, String albumArtist) {
        name = albumName;
        artist = albumArtist;
        albumSongs = new ArrayList<>();
    }

    public String getAlbum_art() {
        return album_art;
    }

    public String getTrack_count() {
        return track_count;
    }

    public String getArtist_mbid() {
        return artist_mbid;
    }

    public String getMbid() {
        return mbid;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setAlbumSongs(ArrayList<Song> albumSongs) {
        this.albumSongs = albumSongs;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbum_art(String album_art) {
        this.album_art = album_art;
    }

    public void setArtist_mbid(String artist_mbid) {
        this.artist_mbid = artist_mbid;
    }

    public void setTrack_count(String track_count) {
        this.track_count = track_count;
    }


    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Song> getAlbumSongs() {
        return albumSongs;
    }

    public void addSong(Song s) {
        albumSongs.add(s);
    }

    public void removeSong(int index) {
        albumSongs.remove(index);
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
