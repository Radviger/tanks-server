package gtanks.gui.console;

import gtanks.StringUtils;
import gtanks.services.LobbyServices;

import java.awt.*;

public class ConsoleCommandHandler implements CommandHandler {
    private static final LobbyServices lobbyMessages = LobbyServices.INSTANCE;
    private final ConsoleWindow context;

    public ConsoleCommandHandler(ConsoleWindow context) {
        this.context = context;
    }

    @Override
    public void onEnterCommand(String cmd) {
        String[] args = cmd.split(" ");
        switch (args[0]) {
            case "trace": {
                this.context.append(Color.YELLOW, StringUtils.concatMassive(args, 1));
                break;
            }
        }
    }
}
