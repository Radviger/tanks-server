package gtanks.lobby.chat;

import gtanks.StringUtils;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.main.netty.NettyUsersHandler;
import gtanks.main.params.OnlineStats;
import gtanks.services.AutoEntryServices;
import gtanks.services.BanServices;
import gtanks.services.LobbyServices;
import gtanks.services.TanksServices;
import gtanks.services.ban.BanChatCommads;
import gtanks.services.ban.BanTimeType;
import gtanks.services.ban.BanType;
import gtanks.services.ban.DateFormater;
import gtanks.services.ban.block.BlockGameReason;
import gtanks.system.timers.SystemTimerScheduler;
import gtanks.users.User;
import gtanks.users.karma.Karma;
import gtanks.users.locations.UserLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public enum ChatLobby {
    INSTANCE;

    private static final int MAX_SIZE_MESSAGES = 50;
    private static final int MAX_WARNING = 4;
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private boolean stopped = false;
    private final TanksServices tanksServices = TanksServices.INSTANCE;
    private final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private final BanServices banServices = BanServices.INSTANCE;

    public void addMessage(ChatMessage msg) {
        this.checkSyntax(msg);
    }

    public void checkSyntax(ChatMessage msg) {
        msg.message = msg.message.trim();
        Karma karma = this.database.getKarmaByUser(msg.user);
        if (karma.isChatBanned()) {
            long currDate = System.currentTimeMillis();
            Date banTo = karma.getChatBannedBefore();
            long delta = currDate - banTo.getTime();
            if (delta <= 0L) {
                msg.localLobby.send(Type.LOBBY_CHAT, "system", "Вы отключены от чата. Вы вернётесь в ЭФИР через " + DateFormater.formatTimeToUnban(delta) + ". Причина: " + karma.getReasonChatBan());
                return;
            }
        }

        if (msg.message.startsWith("/")) {
            String temp = msg.message.replace('/', ' ').trim();
            String[] arguments = temp.split(" ");
            if (!msg.user.getUserGroup().isCommandAvailable(arguments[0])) {
                return;
            }

            switch (arguments[0]) {
                case "addcry": {
                    if (arguments.length >= 2) {
                        this.tanksServices.addCrystal(msg.localLobby, this.getInt(arguments[1]));
                    }
                    break;
                }
                case "cleant": {
                    if (arguments.length >= 2) {
                        this.cleanMessagesByText(StringUtils.concatMassive(arguments, 1));
                    }
                    break;
                }
                case "addscore": {
                    if (arguments.length >= 2) {
                        int score = this.getInt(arguments[1]);
                        if (msg.localLobby.getLocalUser().getScore() + score < 0) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Ваше количество очков опыта не должно быть отрицательное!");
                        } else {
                            this.tanksServices.addScore(msg.localLobby, score);
                        }
                    }
                    break;
                }
                case "online": {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "Current online: " + OnlineStats.getOnline() + "\nMax online: " + OnlineStats.getMaxOnline());
                    break;
                }
                case "system": {
                    if (arguments.length >= 2) {
                        this.sendSystemMessageToAll(arguments, false);
                    }
                    break;
                }
                case "unbanip": {
                    if (arguments.length >= 2) {
                        User _victim = this.database.getUserById(arguments[1]);
                        if (_victim == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден");
                            return;
                        }

                        LobbyManager _l = this.lobbyServices.getLobbyByUser(_victim);
                        NettyUsersHandler.unblock(_l.getLocalUser().getLastIP());
                    }
                    break;
                }
                case "w": {
                    if (arguments.length < 3) {
                        return;
                    }

                    User giver = this.database.getUserById(arguments[1]);
                    if (giver == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден!");
                    } else {
                        String reason = StringUtils.concatMassive(arguments, 2);
                        this.sendSystemMessageToAll("Танкист " + giver.getNickname() + " предупрежден. Причина: " + reason, false);
                    }
                    break;
                }
                case "kick": {
                    if (arguments.length >= 2) {
                        User _userForKick = this.database.getUserById(arguments[1]);
                        if (_userForKick == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден");
                        } else {
                            LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
                            if (_lobby != null) {
                                _lobby.kick();
                                this.sendSystemMessageToAll(_userForKick.getNickname() + " кикнут", false);
                            }
                        }
                    }
                    break;
                }
                case "stop": {
                    this.stopped = true;
                    this.sendSystemMessageToAll("Чат остановлен", false);
                    break;
                }
                case "warn": {
                    this.sendSystemMessageToAll(arguments, true);
                    break;
                }
                case "banip": {
                    if (arguments.length >= 2) {
                        User victim = this.database.getUserById(arguments[1]);
                        if (victim == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден");
                            return;
                        }

                        LobbyManager l = this.lobbyServices.getLobbyByUser(victim);
                        NettyUsersHandler.block(l.getLocalUser().getLastIP());
                        l.kick();
                    }
                    break;
                }
                case "clean": {
                    if (arguments.length >= 2) {
                        this.cleanMessagesByUser(arguments[1]);
                    }
                    break;
                }
                case "clear": {
                    this.clear();
                    break;
                }
                case "getip": {
                    if (arguments.length >= 2) {
                        User shower = this.database.getUserById(arguments[1]);
                        if (shower == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден");
                            return;
                        }

                        String ip = shower.getAntiCheatData().ip;
                        if (ip == null) {
                            ip = shower.getLastIP();
                        }

                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "IP user " + shower.getNickname() + " : " + ip);
                    }
                    break;
                }
                case "start": {
                    this.stopped = false;
                    this.sendSystemMessageToAll("Чат запущен", false);
                    break;
                }
                case "unban": {
                    if (arguments.length >= 2) {
                        User cu = this.database.getUserById(arguments[1]);
                        if (cu == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден!");
                        } else {
                            this.banServices.unbanChat(cu);
                            this.sendSystemMessageToAll("Танкисту " + cu.getNickname() + " был разрешён выход в эфир", false);
                        }
                    }
                    break;
                }
                case "blockgame": {
                    if (arguments.length >= 3) {
                        User victim_ = this.database.getUserById(arguments[1]);

                        int reasonId;
                        try {
                            reasonId = Integer.parseInt(arguments[2]);
                        } catch (Exception var24) {
                            reasonId = 0;
                        }

                        if (victim_ == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден!");
                        } else {
                            this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, victim_, msg.user, BlockGameReason.getReasonById(reasonId).getReason());
                            LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                            if (lobby != null) {
                                lobby.kick();
                            }

                            this.sendSystemMessageToAll("Танкист " + victim_.getNickname() + " был заблокирован и кикнут", false);
                        }
                    }
                    break;
                }
                case "unblockgame": {
                    if (arguments.length < 2) {
                        return;
                    }

                    User av = this.database.getUserById(arguments[1]);
                    if (av == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден!");
                    } else {
                        this.banServices.unblock(av);
                        this.sendSystemMessageToAll("Танкист " + av.getNickname() + " был разблокирован", false);
                    }
                    break;
                }
                case "rbattle": {
                    if (arguments.length >= 2) {
                        StringBuilder id = new StringBuilder();

                        for (int i = 1; i < arguments.length; ++i) {
                            id.append(arguments[i]).append(" ");
                        }

                        BattleInfo battle = BattlesList.getBattleInfoById(id.toString().trim().replace("#battle", ""));
                        if (battle == null) {
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Битва не найдена");
                        } else {
                            if (battle.model != null) {
                                battle.model.sendTableMessageToPlayers("Битва была досрочна завершена, скоро вы будете кикнуты");
                            }

                            SystemTimerScheduler.scheduleTask(() -> {
                                this.sendSystemMessageToAll("Битва " + battle.name + " была принудительно завершена", false);
                                BattlesList.removeBattle(battle);
                                autoEntryServices.battleDisposed(battle.model);
                            }, 4000L);
                            msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Битва будет удалена через 4 секунды");
                        }
                    }
                    break;
                }
                case "ban": {
                    BanTimeType time = BanChatCommads.getTimeType(arguments[0]);
                    if (arguments.length < 3) {
                        return;
                    }

                    String reason = StringUtils.concatMassive(arguments, 2);
                    if (time == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Команда бана не найдена!");
                        return;
                    }

                    User _victim = this.database.getUserById(arguments[1]);
                    if (_victim == null) {
                        msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Игрок не найден!");
                        return;
                    }

                    this.banServices.ban(BanType.CHAT, time, _victim, msg.user, reason);
                    this.sendSystemMessageToAll("Танкист " + _victim.getNickname() + " лишен права выхода в эфир " + time.getNameType() + " Причина: " + reason, false);
                    break;
                }
                default: {
                    msg.localLobby.send(Type.LOBBY_CHAT, "system", "[SERVER]: Неизвестная команда!");
                    break;
                }
            }
        } else if (!msg.message.isEmpty()) {
            if (msg.message.length() >= 399) {
                return;
            }

            if (!this.stopped) {
                if (!msg.localLobby.getChatFloodController().detected(msg.message)) {
                    msg.message = this.getNormalMessage(msg.message.trim());
                    msg.localLobby.timer = System.currentTimeMillis();
                    if (this.chatMessages.size() >= 50) {
                        this.chatMessages.remove(0);
                    }

                    this.chatMessages.add(msg);
                    this.sendMessageToAll(msg);
                } else {
                    if (msg.user.getWarnings() >= 4) {
                        BanTimeType time = BanTimeType.FIVE_MINUTES;
                        String reason = "Флуд.";
                        this.banServices.ban(BanType.CHAT, time, msg.user, msg.user, reason);
                        this.sendSystemMessageToAll("Танкист " + msg.user.getNickname() + " лишен права выхода в эфир " + time.getNameType() + " Причина: " + reason, false);
                        return;
                    }

                    this.sendSystemMessageToAll("Танкист " + msg.user.getNickname() + "  предупрежден. Причина: Флуд.", false);
                    msg.user.addWarning();
                }
            }
        }
    }

    public void cleanMessagesByText(String text) {
        this.chatMessages.removeIf(p -> p.message.equals(text));
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by_text", text);
    }

    public void cleanMessagesByUser(String nickname) {
        this.chatMessages.removeIf(p -> !p.system && p.user != null && p.user.getNickname().equals(nickname));
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clean_by", nickname);
    }

    public void clear() {
        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "clear_all");
        this.chatMessages.clear();
        this.sendSystemMessageToAll("Чат очищен", false);
    }

    public void sendSystemMessageToAll(String[] ar, boolean yellow) {
        StringBuilder total = new StringBuilder();

        for (int i = 1; i < ar.length; ++i) {
            total.append(ar[i]).append(" ");
        }

        ChatMessage msg = new ChatMessage(null, total.toString(), false, null, yellow, null);
        msg.system = true;
        this.chatMessages.add(msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }

        this.lobbyServices.sendCommandToAllUsers(Type.LOBBY_CHAT, UserLocation.ALL, "system", total.toString().trim(), yellow ? "yellow" : "green");
    }

    public void sendSystemMessageToAll(String msg, boolean yellow) {
        ChatMessage sys_msg = new ChatMessage(null, msg, false, null, yellow, null);
        sys_msg.system = true;
        this.chatMessages.add(sys_msg);
        if (this.chatMessages.size() >= 50) {
            this.chatMessages.remove(0);
        }

        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE, "system", msg.trim());
    }

    public void sendMessageToAll(ChatMessage msg) {
        this.lobbyServices.sendCommandToAllUsersBesides(Type.LOBBY_CHAT, UserLocation.BATTLE, JsonUtils.parseChatLobbyMessage(msg));
    }

    public String getNormalMessage(String src) {
        StringBuilder str = new StringBuilder();
        char[] mass = src.toCharArray();

        for (int i = 0; i < mass.length; ++i) {
            if (mass[i] == ' ') {
                if (mass[i] != mass[i + 1]) {
                    str.append(" ");
                }
            } else {
                str.append(mass[i]);
            }
        }
        return str.toString();
    }

    public void deleteMessagesByText(String text, boolean accuracy) {
    }

    public int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception var3) {
            return Integer.MAX_VALUE;
        }
    }

    public Collection<ChatMessage> getMessages() {
        return this.chatMessages;
    }
}
