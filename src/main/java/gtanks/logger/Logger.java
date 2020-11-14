package gtanks.logger;

import gtanks.main.Main;

import java.awt.*;

public class Logger {
    public static void log(Type type, String msg) {
        Log tempLog = new Log(type, msg);
        System.err.println(tempLog.toString());
    }

    public static void debug(String msg) {
        System.out.println("[DEBUG] " + msg);
    }

    public static void log(String msg) {
        Log temp = new Log(Type.INFO, msg);
        if (Main.console != null) {
            Main.console.append(Color.WHITE, temp.toString());
        }

        System.err.println(temp.toString());
    }
}
