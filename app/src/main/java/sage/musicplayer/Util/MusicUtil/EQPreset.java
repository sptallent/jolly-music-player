package sage.musicplayer.Util.MusicUtil;

import java.util.ArrayList;

public class EQPreset {

    private String name;
    private int bassStrength;
    private int loudness;
    ArrayList<String> bandDb;

    public EQPreset() {
        name = "";
        bassStrength = 0;
        loudness = 0;
        bandDb = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumBands() {
        return bandDb.size();
    }

    public int getBassStrength() {
        return bassStrength;
    }

    public void setBassStrength(int bassStrength) {
        this.bassStrength = bassStrength;
    }

    public int getLoudness() {
        return loudness;
    }

    public void setLoudness(int loudness) {
        this.loudness = loudness;
    }

    public int getBandDb(short pos) {
        return Integer.valueOf(bandDb.get(pos));
    }

    public void setBandDb(int pos, String db) {
        bandDb.add(pos, db);
    }
}
