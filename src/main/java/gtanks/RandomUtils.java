package gtanks;

public class RandomUtils {
    public static float getRandom(float min, float max) {
        return min == max ? min : (float) ((double) min + Math.random() * (double) (max - min + 1.0F));
    }
}
