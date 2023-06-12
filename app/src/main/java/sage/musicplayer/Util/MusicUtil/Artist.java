package sage.musicplayer.Util.MusicUtil;

public class Artist {

    private String mbid;
    private String name;

    public Artist() {

    }

    public Artist(String mb, String n) {
        mbid = mb;
        name = n;
    }

    public String getMbid() {
        return mbid;
    }

    public String getName() {
        return name;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }

    public void setName(String name) {
        this.name = name;
    }

}
