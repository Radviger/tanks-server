package gtanks.network.requests;

import gtanks.StringUtils;
import gtanks.auth.Auth;
import gtanks.commands.Command;
import gtanks.commands.Commands;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.network.Networker;

import java.net.Socket;

/**
 * @deprecated
 */
@Deprecated
public class RequestsTransfer extends Networker implements Runnable {
    public LobbyManager lobby;
    private boolean work = true;
    private StringBuffer inputRequest;
    private StringBuffer badRequest = new StringBuffer();
    private Auth auth;

    public RequestsTransfer(Socket client) {
        super(client);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.work && super.bytes != -1) {
                    if ((this.inputRequest = new StringBuffer(this.onCommand().trim())).length() <= 0) {
                        continue;
                    }

                    if (this.inputRequest.toString().endsWith("~")) {
                        this.inputRequest = new StringBuffer(StringUtils.concatStrings(this.badRequest.toString(), this.inputRequest.toString()));
                        this.parseInputRequest();
                        this.badRequest = new StringBuffer();
                        continue;
                    }

                    this.badRequest = new StringBuffer(StringUtils.concatStrings(this.badRequest.toString(), this.inputRequest.toString()));
                    continue;
                }

                Logger.log("User " + super.socketToString() + " has been disconnected.");
                this.lobby.onDisconnect();
            } catch (Exception var2) {
                this.work = false;
                if (this.lobby != null) {
                    this.lobby.onDisconnect();
                }
            }

            return;
        }
    }

    private void parseInputRequest() {
        String[] commands = this.inputRequest.toString().split("~");

        for (String request : commands) {
            this.sendCommandToManagers(Commands.decrypt(request));
        }
    }

    private void sendCommandToManagers(Command cmd) {
        if (this.auth == null) {
            this.auth = new Auth(null, null);
        }

        switch (cmd.type) {
            case AUTH:
            case REGISTRATION:
                this.auth.executeCommand(cmd);
                break;
            case GARAGE:
            case PING:
            case BATTLE:
            case LOBBY_CHAT:
            case LOBBY:
            case CHAT:
                this.lobby.executeCommand(cmd);
                break;
            case UNKNOWN:
                Logger.log("User " + this.socketToString() + " send unknown request: " + cmd.toString());
            case HTTP:
        }
    }
}
