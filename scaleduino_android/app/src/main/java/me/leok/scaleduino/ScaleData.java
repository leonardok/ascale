package me.leok.scaleduino;

/**
 * Created by korndorl on 7/20/2017.
 */

public class ScaleData {
    public float weight;
    public String version;

    public ScaleData(float weight, String version) {
        this.weight = weight;
        this.version = version;
    }

    public String toString() {
        return "ScaleData [weight=" + weight + "]";
    }
}
