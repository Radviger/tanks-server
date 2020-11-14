package gtanks.bugs;

import gtanks.StringUtils;
import gtanks.bugs.screenshots.BufferScreenshotTransfer;
import gtanks.users.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class BugReport {
    private static String URL_BUGS_FILE = "bugs/bugs.data";
    private static File bugsFile;

    public static void bugReport(User sender, BufferScreenshotTransfer screenshot) {
    }

    public static void bugReport(User sender, BugInfo bug) {
        if (bugsFile == null) {
            bugsFile = new File(URL_BUGS_FILE);

            try {
                bugsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            try (FileWriter writer = new FileWriter(bugsFile, true)) {
                writer.append(getFormatedData(sender, bug));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFormatedData(User sender, BugInfo bug) {
        return StringUtils.concatStrings("----", (new Date()).toString(), "----\n", "  User: ", sender.getNickname(), "\n", bug.toString(), "---------------------------------------\n");
    }
}
