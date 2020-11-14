package gtanks.battles.chat;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.BonusType;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.main.params.OnlineStats;
import gtanks.services.BanServices;
import gtanks.services.LobbyServices;
import gtanks.services.TanksServices;
import gtanks.services.ban.BanChatCommads;
import gtanks.services.ban.BanTimeType;
import gtanks.services.ban.BanType;
import gtanks.services.ban.DateFormater;
import gtanks.services.ban.block.BlockGameReason;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.karma.Karma;

import java.util.Date;

public class BattlefieldChatModel {
    private static final int MAX_WARNING = 5;
    private final BattlefieldModel bfModel;
    private final TanksServices tanksServices = TanksServices.INSTANCE;
    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private final BanServices banServices = BanServices.INSTANCE;

    public BattlefieldChatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void onMessage(BattlefieldPlayerController player, String message, boolean team) {
        if (!(message = message.trim()).isEmpty()) {
            Karma karma = this.database.getKarmaByUser(player.getUser());
            if (karma.isChatBanned()) {
                long currDate = System.currentTimeMillis();
                Date banTo = karma.getChatBannedBefore();
                long delta = currDate - banTo.getTime();
                if (delta <= 0L) {
                    player.parentLobby.send(Type.LOBBY_CHAT, "system", StringUtils.concatStrings("Вы отключены от чата. Вы вернётесь в ЭФИР через ", DateFormater.formatTimeToUnban(delta), ". Причина: " + karma.getReasonChatBan()));
                    return;
                }

                this.banServices.unbanChat(player.getUser());
            }

            if (!this.bfModel.battleInfo.team) {
                team = false;
            }

            if (message.startsWith("/")) {
                if (player.getUser().getType() == TypeUser.DEFAULT) {
                    return;
                }

                String[] arguments = message.replace('/', ' ').trim().split(" ");
                if (!player.getUser().getUserGroup().isCommandAvailable(arguments[0])) {
                    return;
                }

                switch (arguments[0]) {
                    case "addcry": {
                        this.tanksServices.addCrystal(player.parentLobby, this.getInt(arguments[1]));
                        break;
                    }
                    case "addscore": {
                        int i = this.getInt(arguments[1]);
                        if (player.parentLobby.getLocalUser().getScore() + i < 0) {
                            this.sendSystemMessage("[SERVER]: Ваше количество очков опыта не должно быть отрицательное!", player);
                        } else {
                            this.tanksServices.addScore(player.parentLobby, i);
                        }
                        break;
                    }
                    case "online": {
                        this.sendSystemMessage("Current online: " + OnlineStats.getOnline() + "\nMax online: " + OnlineStats.getMaxOnline(), player);
                        break;
                    }
                    case "system": {
                        StringBuilder total = new StringBuilder();

                        for (String arg : arguments) {
                            total.append(arg).append(" ");
                        }

                        this.sendSystemMessage(total.toString());
                        break;
                    }
                    case "spawngold": {
                        int count = Integer.parseInt(arguments[1]);
                        int i = 0;
                        while (i < count) {
                            this.bfModel.bonusesSpawnService.spawnBonus(BonusType.GOLD);
                            i++;
                        }
                        break;
                    }
                    case "w": {
                        if (arguments.length < 3) {
                            return;
                        }

                        User giver = this.database.getUserById(arguments[1]);
                        if (giver == null) {
                            this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        } else {
                            String reason = StringUtils.concatMassive(arguments, 2);
                            this.sendSystemMessage(StringUtils.concatStrings("Танкист ", giver.getNickname(), " предупрежден. Причина: ", reason));
                        }
                        break;
                    }
                    case "kick": {
                        User target = this.database.getUserById(arguments[1]);
                        if (target == null) {
                            this.sendSystemMessage("[SERVER]: Игрок не найден", player);
                        } else {
                            LobbyManager _lobby = this.lobbyServices.getLobbyByUser(target);
                            if (_lobby != null) {
                                _lobby.kick();
                                this.sendSystemMessage(target.getNickname() + " кикнут");
                            }
                        }
                        break;
                    }
                    case "getip": {
                        if (arguments.length >= 2) {
                            User shower = this.database.getUserById(arguments[1]);
                            if (shower == null) {
                                return;
                            }

                            String ip = shower.getAntiCheatData().ip;
                            if (ip == null) {
                                ip = shower.getLastIP();
                            }

                            this.sendSystemMessage("IP user " + shower.getNickname() + " : " + ip, player);
                        }
                        break;
                    }
                    case "unban": {
                        if (arguments.length >= 2) {
                            User cu = this.database.getUserById(arguments[1]);
                            if (cu == null) {
                                this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                            } else {
                                this.banServices.unbanChat(cu);
                                this.sendSystemMessage("Танкисту " + cu.getNickname() + " был разрешён выход в эфир");
                            }
                        }
                        break;
                    }
                    case "blockgame": {
                        if (arguments.length < 3) {
                            return;
                        }

                        User target = this.database.getUserById(arguments[1]);

                        int reasonId;
                        try {
                            reasonId = Integer.parseInt(arguments[2]);
                        } catch (Exception e) {
                            reasonId = 0;
                        }

                        if (target == null) {
                            this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        } else {
                            this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, target, player.getUser(), BlockGameReason.getReasonById(reasonId).getReason());
                            LobbyManager lobby = this.lobbyServices.getLobbyByNick(target.getNickname());
                            if (lobby != null) {
                                lobby.kick();
                            }

                            this.sendSystemMessage(StringUtils.concatStrings("Танкист ", target.getNickname(), " был заблокирован и кикнут"));
                        }
                        break;
                    }
                    case "unblockgame": {
                        if (arguments.length < 2) {
                            return;
                        }

                        User av = this.database.getUserById(arguments[1]);
                        if (av == null) {
                            this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                        } else {
                            this.banServices.unblock(av);
                            this.sendSystemMessage(av.getNickname() + " разблокирован");
                        }
                        break;
                    }
                    case "spawncry": {
                        int count = Integer.parseInt(arguments[1]);
                        int i = 0;
                        while (i < count) {
                            this.bfModel.bonusesSpawnService.spawnBonus(BonusType.CRYSTAL);
                            i++;
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
                            this.sendSystemMessage("[SERVER]: Команда бана не найдена!", player);
                            return;
                        }

                        User target = this.database.getUserById(arguments[1]);
                        if (target == null) {
                            this.sendSystemMessage("[SERVER]: Игрок не найден!", player);
                            return;
                        }

                        this.banServices.ban(BanType.CHAT, time, target, player.getUser(), reason);
                        this.sendSystemMessage(StringUtils.concatStrings("Танкист ", target.getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
                        break;
                    }
                    default: {
                        this.sendSystemMessage("[SERVER]: Неизвестная команда!", player);
                        break;
                    }
                }
            } else {
                if (message.length() >= 399) {
                    return;
                }

                if (!player.parentLobby.getChatFloodController().detected(message)) {
                    player.parentLobby.timer = System.currentTimeMillis();
                    this.sendMessage(new BattleChatMessage(player.getUser().getNickname(), player.getUser().getRang(), message, player.playerTeamType.name(), team, false));
                } else {
                    if (player.getUser().getWarnings() >= MAX_WARNING) {
                        BanTimeType time = BanTimeType.FIVE_MINUTES;
                        String reason = "Флуд.";
                        this.banServices.ban(BanType.CHAT, time, player.getUser(), player.getUser(), reason);
                        this.sendSystemMessage(StringUtils.concatStrings("Танкист ", player.getUser().getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
                        return;
                    }

                    this.sendSystemMessage("Танкист " + player.getUser().getNickname() + "  предупрежден. Причина: Флуд.");
                    player.getUser().addWarning();
                }
            }
        }
    }

    public void sendSystemMessage(String message) {
        if (message == null) {
            message = " ";
        }

        this.sendMessage(new BattleChatMessage(null, 0, message, "NONE", false, true));
    }

    public void sendSystemMessage(String message, BattlefieldPlayerController player) {
        if (message == null) {
            message = " ";
        }

        this.sendMessage(new BattleChatMessage(null, 0, message, "NONE", false, true), player);
    }

    private void sendMessage(BattleChatMessage msg) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "chat", JsonUtils.parseBattleChatMessage(msg));
    }

    private void sendMessage(BattleChatMessage msg, BattlefieldPlayerController controller) {
        controller.send(Type.BATTLE, "chat", JsonUtils.parseBattleChatMessage(msg));
    }

    public int getInt(String src) {
        try {
            return Integer.parseInt(src);
        } catch (Exception var3) {
            return Integer.MAX_VALUE;
        }
    }
}
