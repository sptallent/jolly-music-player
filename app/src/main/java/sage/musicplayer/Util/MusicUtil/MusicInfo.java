package sage.musicplayer.Util.MusicUtil;

import java.io.Serializable;

public class MusicInfo implements Serializable {

    private long local_id;
    private String song_mbid = null;
    private String artist_mbid = null;
    private String album_mbid = null;

    private String song_title = null;
    private String artist_name = null;
    private String album_title = null;

    private String track_number = null;
    private String genre = null;
    private String release_date = null;
    private String album_track_count = null;

    private String path = null;

    String album_art = null;
    String fingerprint = null;

    public MusicInfo() {

    }

    @Override
    public String toString() {
        return "Song Title: " + song_title + "\nSong MBID: " + song_mbid + "\nTrack Number: " + track_number + "\nArtist Name: " + artist_name + "\nArtist MBID: " + artist_mbid + "\nAlbum Title : " + album_title +
                "\nAlbum MBID: " + album_mbid + "\nAlbum Track Count: " + album_track_count + "\nAlbum Art: " + album_art + "\n";
    }

    public String getSong_mbid() {
        return song_mbid;
    }

    public String getArtist_mbid() {
        return artist_mbid;
    }

    public String getAlbum_mbid() {
        return album_mbid;
    }

    public String getSong_title() {
        return song_title;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public String getTrack_number() {
        return track_number;
    }

    public String getAlbum_track_count() {
        return album_track_count;
    }

    public String getAlbum_art() {
        return album_art;
    }

    public String getGenre() {
        return genre;
    }

    public String getRelease_date() {
        return release_date;
    }

    public long getLocal_id() {
        return local_id;
    }

    public void setSong_mbid(String song_mbid) {
        this.song_mbid = song_mbid;
    }

    public void setAlbum_art(String album_art) {
        this.album_art = album_art;
    }

    public void setArtist_mbid(String artist_mbid) {
        this.artist_mbid = artist_mbid;
    }

    public void setTrack_number(String track_num) {
        if(track_num == null)
            this.track_number = track_num;
        else
            this.track_number = track_num.replaceAll("\\D+", "");
    }

    public void setAlbum_mbid(String album_mbid) {
        this.album_mbid = album_mbid;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public void setAlbum_track_count(String track_count) {
        if(track_count == null)
            this.album_track_count = track_count;
        else
            this.album_track_count = track_count.replaceAll("\\D+", "");
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public void setSong_title(String song_title) {
        this.song_title = song_title;
    }

    public void setLocal_id(long local_id) {
        this.local_id = local_id;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public boolean foundData() {
        if(!getSong_mbid().isEmpty())
            return true;
        return false;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
