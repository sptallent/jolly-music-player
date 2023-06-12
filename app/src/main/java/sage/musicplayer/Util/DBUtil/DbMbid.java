package sage.musicplayer.Util.DBUtil;

public class DbMbid {

    private String mb;
    private String tit;
    private String art_name;
    private String alb_name;
    private String gen;
    private String rel_date;
    private String alb_art;
    private int tr_num;
    private int tr_tot;

    public DbMbid() {

    }

    public DbMbid(String mbid, String title, String artist_name, String album_name, String genre, String release_date, String album_art, int track_num, int track_total) {
        mb = mbid;
        tit = title;
        art_name = artist_name;
        alb_name = album_name;
        gen = genre;
        rel_date = release_date;
        alb_art = album_art;
        tr_num = track_num;
        tr_tot = track_total;
    }

    public String getMbid() {
        return mb;
    }

    public void setMbid(String mb) {
        this.mb = mb;
    }

    public String getTitle() {
        return tit;
    }

    public void setTitle(String tit) {
        this.tit = tit;
    }

    public String getArtist_name() {
        return art_name;
    }

    public void setArtist_name(String art_name) {
        this.art_name = art_name;
    }

    public String getAlbum_name() {
        return alb_name;
    }

    public void setAlbum_name(String alb_name) {
        this.alb_name = alb_name;
    }

    public String getGenre() {
        return gen;
    }

    public void setGenre(String gen) {
        this.gen = gen;
    }

    public String getRelease_date() {
        return rel_date;
    }

    public void setRelease_date(String rel_date) {
        this.rel_date = rel_date;
    }

    public String getAlbum_art() {
        return alb_art;
    }

    public void setAlbum_art(String alb_art) {
        this.alb_art = alb_art;
    }

    public int getTrack_num() {
        return tr_num;
    }

    public void setTrack_num(int tr_num) {
        this.tr_num = tr_num;
    }

    public int getTrack_total() {
        return tr_tot;
    }

    public void setTrack_total(int tr_tot) {
        this.tr_tot = tr_tot;
    }
}
