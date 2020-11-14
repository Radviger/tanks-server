package gtanks.main.netty;

import gtanks.auth.Auth;
import gtanks.commands.Command;
import gtanks.commands.Commands;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;

public class ProtocolTransfer {
    private static final String COMMAND_SEPARATOR = "end~";
    private final int[] _keys = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
    private final Channel channel;
    private final ChannelHandlerContext context;
    public LobbyManager lobby;
    public Auth auth;
    private StringBuffer inputRequest;
    private StringBuffer badRequest = new StringBuffer();
    private int _lastKey = 1;

    public ProtocolTransfer(Channel channel, ChannelHandlerContext context) {
        this.channel = channel;
        this.context = context;
    }

    public void decryptProtocol(String protocol) {
        //System.out.println("Raw input data: " + protocol);
        if ((this.inputRequest = new StringBuffer(protocol)).length() > 0) {
            if (this.inputRequest.toString().endsWith(COMMAND_SEPARATOR)) {
                this.inputRequest = new StringBuffer(this.badRequest.toString() + this.inputRequest.toString());

                for (String request : parseCryptRequests()) {
                    int key;
                    try {
                        key = Integer.parseInt(String.valueOf(request.charAt(0)));
                    } catch (Exception e) {
                        Logger.log("[EXCEPTION] Detected cheater(replace protocol): " + this.channel.toString());
                        NettyUsersHandler.block(this.channel.getRemoteAddress().toString().split(":")[0]);
                        this.closeConnection();
                        return;
                    }

                    if (key == this._lastKey) {
                        Logger.log("Detected cheater(replace protocol): " + this.channel.toString());
                        NettyUsersHandler.block(this.channel.getRemoteAddress().toString().split(":")[0]);
                        this.closeConnection();
                        return;
                    }

                    int nextKey = (this._lastKey + 1) % this._keys.length;
                    if (key != (nextKey == 0 ? 1 : nextKey)) {
                        Logger.log("[NOT QUEQUE KEY " + nextKey + " " + this._lastKey + "] Detected cheater(replace protocol): " + this.channel.toString());
                        NettyUsersHandler.block(this.channel.getRemoteAddress().toString().split(":")[0]);
                        this.closeConnection();
                        return;
                    }

                    String decrypt = this.decrypt(request.substring(1), key);
                    //System.out.println("Decrypted: " + decrypt);
                    this.inputRequest = new StringBuffer(decrypt);
                    this.sendRequestToManagers(this.inputRequest.toString());
                }

                this.badRequest = new StringBuffer();
            } else {
                this.badRequest = new StringBuffer(this.badRequest.toString() + this.inputRequest.toString());
            }
        }

    }

    private String[] parseCryptRequests() {
        return this.inputRequest.toString().split(COMMAND_SEPARATOR);
    }

    private String decrypt(String request, int key) {
        this._lastKey = key;
        char[] _chars = request.toCharArray();

        for (int i = 0; i < request.length(); ++i) {
            _chars[i] = (char) (_chars[i]  - _keys[key]);
        }

        return new String(_chars);
    }

    private void sendRequestToManagers(String request) {
        this.sendCommandToManagers(Commands.decrypt(request));
    }

    private void sendCommandToManagers(Command cmd) {
        if (this.auth == null) {
            this.auth = new Auth(this, this.context);
        }

        switch (cmd.type) {
            case AUTH:
            case PING:
            case REGISTRATION:
                this.auth.executeCommand(cmd);
                break;
            case GARAGE:
            case CHAT:
            case LOBBY:
            case LOBBY_CHAT:
            case BATTLE:
                this.lobby.executeCommand(cmd);
                break;
            case UNKNOWN:
                Logger.log("User " + this.channel.toString() + " sent unknown request: " + cmd.toString());
                break;
            case HTTP:
            default:
                break;
            case SYSTEM:
                this.auth.executeCommand(cmd);

                if (this.lobby != null) {
                    this.lobby.executeCommand(cmd);
                }
        }
    }

    public boolean send(Type type, String... args) {
        StringBuilder request = new StringBuilder();
        request.append(type.toString());
        request.append(";");

        for (int i = 0; i < args.length - 1; ++i) {
            request.append(args[i]).append(";");
        }

        request.append(args[args.length - 1]).append(COMMAND_SEPARATOR);
        if (this.channel.isWritable() && this.channel.isConnected() && this.channel.isOpen()) {
            this.channel.write(request.toString());
        }

        return true;
    }

    protected void onDisconnect() {
        if (this.lobby != null) {
            this.lobby.onDisconnect();
        }
    }

    public void closeConnection() {
        this.channel.close();
    }

    public String getIP() {
        return this.channel.getRemoteAddress().toString();
    }
}
