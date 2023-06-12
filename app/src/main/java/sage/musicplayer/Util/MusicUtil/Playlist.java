package sage.musicplayer.Util.MusicUtil;

import java.util.ArrayList;

public class Playlist {

    int id;
    String playlist_name;
    ArrayList<Song> playlistSongs;

    public Playlist() {
        playlistSongs = new ArrayList<>();
    }

    public Playlist(int i, String n) {
        id = i;
        playlist_name = n;
        playlistSongs = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlaylistName() {
        return playlist_name;
    }

    public void setPlaylistName(String playlist_name) {
        this.playlist_name = playlist_name;
    }

    public ArrayList<Song> getPlaylistSongs() {
        return playlistSongs;
    }

    public void setPlaylistSongs(ArrayList<Song> playlistSongs) {
        this.playlistSongs = playlistSongs;
    }

    public void addSong(long id) {
        Song s = new Song();
        s.setId(id);
        playlistSongs.add(s);
    }
}
