package gtanks.main.params;

import java.util.ArrayList;
import java.util.Collections;

public class OnlineStats {
    private static int online;
    private static ArrayList stat = new ArrayList();

    public static int getOnline() {
        return online;
    }

    public static void addOnline() {
        ++online;
        stat.add(online);
    }

    public static void removeOnline() {
        --online;
    }

    public static int getMaxOnline() {
        return stat.size() == 0 ? 0 : (Integer) Collections.max(stat);
    }
}
