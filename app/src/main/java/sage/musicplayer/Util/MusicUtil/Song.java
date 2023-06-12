package sage.musicplayer.Util.MusicUtil;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Song implements Serializable, Parcelable {

    private long id;
    private String title;
    private String artist;
    private String duration;
    private long album_ID;
    private String album_name;
    private String album_art;
    private String dateAdded;
    private String genre;
    private String release_date;

    private String mbid;
    private String album_mbid;
    private String artist_mbid;

    private String track_number;
    private String track_total;

    private Calendar calendar;
    private Date date;
    private Date createdTime;

    private String fingerprint;
    private String path;

    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public Song() {

    }

    private Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        duration = in.readString();
        album_ID = in.readLong();
        album_name = in.readString();
        album_art = in.readString();
        dateAdded = in.readString();
        genre = in.readString();
        release_date = in.readString();
        mbid = in.readString();
        album_mbid = in.readString();
        artist_mbid = in.readString();
        track_number = in.readString();
        track_total = in.readString();
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(in.readLong());
        date = (Date) in.readSerializable();
        createdTime = (Date) in.readSerializable();
    }

    public Song(long songID, String songTitle, String songArtist, String songDuration, long alID) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        duration = songDuration;
        album_ID = alID;
        calendar = Calendar.getInstance();
        createdTime = calendar.getTime();
        album_art = "";
    }

    public long getId() {
        return id;
    }

    public String getMbid() {
        return mbid;
    }

    public String getArtist_mbid() {
        return artist_mbid;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getAlbum_mbid() {
        return album_mbid;
    }

    public Date getDate() {
        return date;
    }

    public String getTrack_number() {
        return track_number;
    }



    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getDuration() {
        return duration;
    }

    public long getAlbumID() { return album_ID; }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getAlbum_art() {
        return album_art;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null)
            return false;
        Song sCompare = (Song) obj;
        if(sCompare.getCreatedTime() == getCreatedTime())
            return true;
        return false;
    }

    public boolean isNull() {
        if(getDuration() == null)
            return true;
        return getDuration().isEmpty();
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setQueuedTime() {
        if(calendar == null)
            calendar = Calendar.getInstance();
        date = calendar.getTime();
    }

    public long getQueuedTime() {
        return date.getTime();
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAlbum_ID(long album_ID) {
        this.album_ID = album_ID;
    }

    public void setArtist_mbid(String artist_mbid) {
        this.artist_mbid = artist_mbid;
    }

    public void setAlbum_mbid(String album_mbid) {
        this.album_mbid = album_mbid;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setTrack_number(String track_number) {
        this.track_number = track_number;
    }

    public void setAlbum_art(String album_art) {
        this.album_art = album_art;
    }

    public long getCreatedTime() {
        return createdTime.getTime();
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGenre() {
        return genre;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getTrack_total() {
        return track_total;
    }

    public void setTrack_total(String track_total) {
        this.track_total = track_total;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(duration);
        dest.writeLong(album_ID);
        dest.writeString(album_name);
        dest.writeString(album_art);
        dest.writeString(dateAdded);
        dest.writeString(genre);
        dest.writeString(release_date);
        dest.writeString(mbid);
        dest.writeString(album_mbid);
        dest.writeString(artist_mbid);
        dest.writeString(track_number);
        dest.writeString(track_total);
        dest.writeLong(calendar.getTimeInMillis());
        dest.writeSerializable(date);
        dest.writeSerializable(createdTime);
    }
}
