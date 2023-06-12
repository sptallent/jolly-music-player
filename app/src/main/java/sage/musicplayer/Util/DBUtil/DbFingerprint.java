package sage.musicplayer.Util.DBUtil;

public class DbFingerprint {

    private String fingerprint;
    private String mbid;

    public DbFingerprint() {

    }

    public DbFingerprint(String fp, String mb) {
        fingerprint = fp;
        mbid = mb;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getMbid() {
        return mbid;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public void setMbid(String mbid) {
        this.mbid = mbid;
    }
}
