package sage.musicplayer.Util.MusicUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;

public class MusicInfoGrabber {

    private final String base_url = "https://musicbrainz.org/ws/2/";
    private final String mb_entity = "recording/";
    private final String mb_params = "?inc=releases+artists+media+genres&fmt=json";
    private final String ca_base_url = "https://coverartarchive.org/release/";

    public MusicInfoGrabber() {

    }

    public MusicInfo getMusicBrainzSongInfo(String song_mbid) {
        MusicInfo musicInfo = new MusicInfo();
        try {
            URL url = new URL(base_url + mb_entity + song_mbid + mb_params);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.addRequestProperty("User-Agent", "Jolly Music Player(Android)");

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
                JSONObject mbJSONObj = new JSONObject(sb.toString());

                String song_title = "";
                if(mbJSONObj.has("title"))
                        song_title = mbJSONObj.getString("title");
                String song_release = "";
                if(mbJSONObj.has("first-release-date"))
                    song_release = mbJSONObj.getString("first-release-date");

                JSONArray jsonArtistArray = null;
                if(mbJSONObj.has("artist-credit"))
                    jsonArtistArray = mbJSONObj.getJSONArray("artist-credit");
                JSONObject mbArtistObj = null;
                if(jsonArtistArray != null && jsonArtistArray.length() > 0)
                    mbArtistObj = (JSONObject) jsonArtistArray.get(0);

                String artist_name = "";
                if(mbArtistObj != null && mbArtistObj.has("name"))
                    artist_name = mbArtistObj.getString("name");
                String artist_mbid = "";
                if(mbArtistObj != null && mbArtistObj.has("artist"))
                    artist_mbid = mbArtistObj.getJSONObject("artist").getString("id");
                JSONArray jsonReleasesArray = null;
                if(mbJSONObj.has("releases"))
                    jsonReleasesArray = mbJSONObj.getJSONArray("releases");
                JSONObject mbReleaseObj = null;
                if(jsonReleasesArray != null && jsonReleasesArray.length() > 0)
                    mbReleaseObj = (JSONObject) jsonReleasesArray.get(0);

                String album_title = "";
                if(mbReleaseObj != null && mbReleaseObj.has("title"))
                        album_title = mbReleaseObj.getString("title");
                String album_mbid = "";
                if(mbReleaseObj != null && mbReleaseObj.has("id"))
                    album_mbid= mbReleaseObj.getString("id");


                StringBuilder genre_builder = new StringBuilder();
                JSONArray genre_arr = null;
                String genre = "";
                if(mbArtistObj != null && mbArtistObj.has("artist") && mbArtistObj.getJSONObject("artist").has("genres")) {
                    genre_arr = mbArtistObj.getJSONObject("artist").getJSONArray("genres");
                    for (int i = 0; i < genre_arr.length(); i++) {
                        String cur_genre = genre_arr.getJSONObject(i).getString("name") + ",";
                        genre_builder.append(cur_genre);
                    }
                    if (genre_builder.length() - 1 > 0 && genre_builder.lastIndexOf(",") == genre_builder.length() - 1)
                        genre_builder.deleteCharAt(genre_builder.length() - 1);
                    genre = genre_builder.toString();
                }
                String track_number = "0";
                if(mbReleaseObj != null && mbReleaseObj.has("media") && (mbReleaseObj.getJSONArray("media").get(0)) != null)
                    track_number = ((JSONObject) (((JSONObject) mbReleaseObj.getJSONArray("media").get(0)).getJSONArray("tracks").get(0))).getString("number");
                int album_track_count = 0;
                if(mbReleaseObj != null && mbReleaseObj.has("media") && (mbReleaseObj.getJSONArray("media").get(0)) != null)
                    album_track_count = (((JSONObject) mbReleaseObj.getJSONArray("media").get(0)).getInt("track-count"));

                if (!song_mbid.isEmpty())
                    musicInfo.setSong_mbid(song_mbid);
                if (!song_title.isEmpty())
                    musicInfo.setSong_title(song_title);
                if (!artist_name.isEmpty())
                    musicInfo.setArtist_name(artist_name);
                if (!artist_mbid.isEmpty())
                    musicInfo.setArtist_mbid(artist_mbid);
                if (!album_title.isEmpty())
                    musicInfo.setAlbum_title(album_title);
                if (!album_mbid.isEmpty())
                    musicInfo.setAlbum_mbid(album_mbid);
                if (!track_number.isEmpty())
                    musicInfo.setTrack_number(track_number);
                if (album_track_count > 0)
                    musicInfo.setAlbum_track_count(String.valueOf(album_track_count));
                if(!genre.isEmpty())
                    musicInfo.setGenre(genre);
                if(!song_release.isEmpty())
                    musicInfo.setRelease_date(song_release);
                String album_art;
                if (!album_mbid.isEmpty()) {
                    album_art = getAlbumCoverArt(album_mbid);
                    if (!album_art.isEmpty())
                        musicInfo.setAlbum_art(album_art);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return musicInfo;
    }

    public String getAlbumCoverArt(String album_mbid) {
        String cover_art = "";
        try {
            URL ca_url = new URL(ca_base_url + album_mbid);
            HttpURLConnection ca_conn = (HttpURLConnection) ca_url.openConnection();
            ca_conn.setConnectTimeout(5000);
            ca_conn.setRequestMethod("GET");
            ca_conn.setInstanceFollowRedirects(true);
            ca_conn.setRequestProperty("Content-Type", "application/json; utf-8");

            InputStreamReader is;
            int status_code = ca_conn.getResponseCode();
            if (status_code == HTTP_OK) {
                is = new InputStreamReader(ca_conn.getInputStream());

                BufferedReader ca_br = new BufferedReader(is);

                String ca_line;
                StringBuilder ca_sb = new StringBuilder();
                while ((ca_line = ca_br.readLine()) != null) {
                    ca_sb.append(ca_line);
                }
                JSONObject caJSONObj = new JSONObject(ca_sb.toString());
                JSONObject caImageObj = null;
                if(caJSONObj.has("images"))
                    caImageObj = (JSONObject) caJSONObj.getJSONArray("images").get(0);
                if(caImageObj != null && caImageObj.has("front") && caImageObj.getBoolean("front")) {
                    cover_art = caImageObj.getString("image");
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        return cover_art;
    }
}
