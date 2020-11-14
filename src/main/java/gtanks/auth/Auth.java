package gtanks.auth;

import gtanks.RankUtils;
import gtanks.commands.Command;
import gtanks.commands.Type;
import gtanks.groups.UserGroupsLoader;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.chat.ChatLobby;
import gtanks.logger.Logger;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.main.netty.ProtocolTransfer;
import gtanks.network.Session;
import gtanks.services.AutoEntryServices;
import gtanks.system.localization.Localization;
import gtanks.users.User;
import gtanks.users.karma.Karma;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.util.regex.Pattern;

public class Auth extends AuthCommandsConst {
    private static final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private static final ChatLobby chatLobby = ChatLobby.INSTANCE;
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;
    private final ProtocolTransfer transfer;
    private final ChannelHandlerContext context;
    private Localization localization;

    public Auth(ProtocolTransfer transfer, ChannelHandlerContext context) {
        this.transfer = transfer;
        this.context = context;
    }

    public void executeCommand(Command command) {
        try {
            switch (command.type) {
                case AUTH: {
                    String nickname = command.args[0];
                    String password = command.args[1];
                    if (nickname.length() > 50) {
                        return;
                    }

                    if (password.length() > 50) {
                        return;
                    }

                    User user = database.getUserById(nickname);
                    if (user == null) {
                        this.transfer.send(Type.AUTH, "not_exist");
                        return;
                    }

                    if (!user.getPassword().equals(password)) {
                        Logger.log("The user " + user.getNickname() + " has not been logged. Password deined.");
                        this.transfer.send(Type.AUTH, "denied");
                        return;
                    }

                    this.onPasswordAccept(user);
                    break;
                }
                case REGISTRATION:
                    if (command.args[0].equals("check_name")) {
                        String nickname = command.args[1];
                        if (nickname.length() > 50) {
                            return;
                        }

                        boolean callsignExist = database.contains(nickname);
                        boolean callsignNormal = this.callsignNormal(nickname);
                        this.transfer.send(Type.REGISTRATION, "check_name_result", !callsignExist && callsignNormal ? "not_exist" : "nickname_exist");
                    } else {
                        String nickname = command.args[0];
                        String password = command.args[1];
                        if (nickname.length() > 50) {
                            return;
                        }

                        if (password.length() > 50) {
                            return;
                        }

                        if (database.contains(nickname)) {
                            this.transfer.send(Type.REGISTRATION, "nickname_exist");
                            return;
                        }

                        if (this.callsignNormal(nickname)) {
                            User newUser = new User(nickname, password);
                            newUser.setLastIP(this.transfer.getIP());
                            database.register(newUser);
                            this.transfer.send(Type.REGISTRATION, "info_done");
                        } else {
                            this.transfer.closeConnection();
                        }
                    }
                    break;
                case SYSTEM: {
                    String nickname = command.args[0];

                    switch (nickname) {
                        case "init_location":
                            this.localization = Localization.valueOf(command.args[1]);
                            break;
                        case "c01":
                            this.transfer.closeConnection();
                            break;
                        case "get_aes_data":

                            break;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            RemoteDatabaseLogger.error(e);
        }
    }

    private boolean callsignNormal(String nick) {
        Pattern pattern = Pattern.compile("[a-zA-Z]\\w{3,14}");
        return pattern.matcher(nick).matches();
    }

    private void onPasswordAccept(User user) {
        try {
            Karma karma = database.getKarmaByUser(user);
            user.setKarma(karma);
            if (karma.isGameBlocked()) {
                this.transfer.send(Type.AUTH, "ban", karma.getReasonGameBan());
                return;
            }

            if (user.session != null) {
                this.transfer.closeConnection();
                return;
            }

            user.getAntiCheatData().ip = this.transfer.getIP();
            user.session = new Session(this.transfer, this.context);
            database.cache(user);
            user.setGarage(database.getGarageByUser(user));
            user.getGarage().unparseJSONData();
            user.setUserGroup(UserGroupsLoader.getUserGroup(user.getType()));
            Logger.log("The user " + user.getNickname() + " has been logged. Password accept.");
            this.transfer.lobby = new LobbyManager(this.transfer, user);
            if (this.localization == null) {
                this.localization = Localization.EN;
            }

            user.setLocalization(this.localization);
            this.transfer.send(Type.AUTH, "accept");
            this.transfer.send(Type.LOBBY, "init_panel", JsonUtils.parseUserToJSON(user));
            this.transfer.send(Type.LOBBY, "update_rang_progress", String.valueOf(RankUtils.getUpdateNumber(user.getScore())));
            if (!autoEntryServices.needEnterToBattle(user)) {
                this.transfer.send(Type.GARAGE, "init_garage_items", JsonUtils.parseGarageUser(user).trim());
                this.transfer.send(Type.GARAGE, "init_market", JsonUtils.parseMarketItems(user));
                this.transfer.send(Type.LOBBY_CHAT, "init_chat");
                this.transfer.send(Type.LOBBY_CHAT, "init_messages", JsonUtils.parseChatLobbyMessages(chatLobby.getMessages()));
            } else {
                this.transfer.send(Type.LOBBY, "init_battlecontroller");
                autoEntryServices.prepareToEnter(this.transfer.lobby);
            }

            user.setLastIP(user.getAntiCheatData().ip);
            database.update(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
