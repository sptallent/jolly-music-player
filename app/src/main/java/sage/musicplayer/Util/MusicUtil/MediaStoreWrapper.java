package sage.musicplayer.Util.MusicUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

public class MediaStoreWrapper {

    Context context;

    final String PLAYLIST_ID = android.provider.MediaStore.Audio.Playlists._ID;
    final String PLAYLIST_NAME = android.provider.MediaStore.Audio.Playlists.NAME;
    final String PLAYLIST_SONG_ID = android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID;

    public MediaStoreWrapper(Context c) {
        context = c;
    }

    /*public ArrayList<Playlist> getPlaylists() {
        ArrayList<Playlist> temp = new ArrayList();
        ArrayList<Playlist> completedTemp = new ArrayList<>();

        ContentResolver playlistResolver = context.getContentResolver();
        Uri playlistUri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);

        if(playlistCursor != null && playlistCursor.moveToFirst()) {
            int playlist_id = playlistCursor.getColumnIndex(PLAYLIST_ID);
            int playlist_name = playlistCursor.getColumnIndex(PLAYLIST_NAME);

            do {
                long this_id = playlistCursor.getLong(playlist_id);
                String this_name = playlistCursor.getString(playlist_name);

                temp.add(new Playlist(this_id, this_name));
            } while(playlistCursor.moveToNext());

            for(int i = 0; i < temp.size(); i++) {
                Playlist temp_playlist = temp.get(i);
                Uri currentPlaylistUri = MediaStore.Audio.Playlists.Members.getContentUri("MediaStore.Audio.Media.EXTERNAL_CONTENT_URI", temp_playlist.getId());
                Cursor currentPlaylistCursor = playlistResolver.query(currentPlaylistUri, null, null, null, null);

                if(currentPlaylistCursor != null &&currentPlaylistCursor.moveToFirst()) {
                    int playlist_audio_id = currentPlaylistCursor.getColumnIndex(PLAYLIST_SONG_ID);
                    do {
                        String currentAudioID = currentPlaylistCursor.getString(playlist_audio_id);
                        temp_playlist.addSong(Long.parseLong(currentAudioID));
                    } while (currentPlaylistCursor.moveToNext());
                }
                completedTemp.add(temp_playlist);
            }
        }
        return completedTemp;
    }*/

}
