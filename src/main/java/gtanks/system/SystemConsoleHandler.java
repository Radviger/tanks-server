package gtanks.system;

import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.main.params.OnlineStats;
import gtanks.system.restart.ServerRestartService;

import java.util.Scanner;

public enum SystemConsoleHandler {
    INSTANCE;

    private static final ServerRestartService serverRestartService = ServerRestartService.INSTANCE;

    public void start() {
        new Thread(() -> {
            try (Scanner scn = new Scanner(System.in)) {
                while (true) {
                    String input = scn.nextLine();
                    onCommand(input);
                }
            }
        }, "Console Handler").start();
    }

    private void onCommand(String input) {
        String[] spaceSplit = input.replace("/", "").split(" ");
        switch (spaceSplit[0]) {
            case "online": {
                System.out.println(this.getOnlineInfoString());
                break;
            }
            case "rf": {
                Logger.log(Type.WARNING, "Attention! The factories of weapons and hulls will be reloaded!");
                WeaponsFactory.init("config/weapons/");
                HullsFactory.init("config/hulls/");
                break;
            }
            case "help": {
                System.out.println(this.getHelpString());
                break;
            }
            case "restart": {
                serverRestartService.restart();
                break;
            }
        }
    }

    private String getOnlineInfoString() {
        return "\n Total online: " + OnlineStats.getOnline() + "\n Max online: " + OnlineStats.getMaxOnline() + "\n";
    }

    private String getHelpString() {
        return "rf - reload item's factories.\nonline - print current online.";
    }
}
