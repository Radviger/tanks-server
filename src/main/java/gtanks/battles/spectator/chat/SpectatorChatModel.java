package gtanks.battles.spectator.chat;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.bonuses.BonusType;
import gtanks.battles.chat.BattlefieldChatModel;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.spectator.SpectatorModel;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.services.BanServices;
import gtanks.services.LobbyServices;
import gtanks.services.ban.BanChatCommads;
import gtanks.services.ban.BanTimeType;
import gtanks.services.ban.BanType;
import gtanks.services.ban.block.BlockGameReason;
import gtanks.users.User;

public class SpectatorChatModel {
    private static final String CHAT_SPECTATOR_COMMAND = "spectator_message";
    private static final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private static final BanServices banServices = BanServices.INSTANCE;
    private final SpectatorModel spModel;
    private final BattlefieldChatModel chatModel;

    public SpectatorChatModel(SpectatorModel spModel) {
        this.spModel = spModel;
        BattlefieldModel bfModel = spModel.getBattleModel();
        this.chatModel = bfModel.chatModel;
    }

    public void onMessage(String message, SpectatorController spectator) {
        if (message.startsWith("/")) {
            String[] arguments = message.replace('/', ' ').trim().split(" ");
            if (!spectator.getUser().getUserGroup().isCommandAvailable(arguments[0])) {
                return;
            }

            switch (arguments[0]) {
                case "spawngold": {
                    int count = Integer.parseInt(arguments[1]);
                    int i = 0;
                    while (i < count) {
                        this.spModel.getBattleModel().bonusesSpawnService.spawnBonus(BonusType.GOLD);
                        i++;
                    }
                    break;
                }
                case "w": {
                    if (arguments.length < 3) {
                        return;
                    }

                    User giver = this.database.getUserById(arguments[1]);
                    if (giver != null) {
                        String reason = StringUtils.concatMassive(arguments, 2);
                        this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", giver.getNickname(), " предупрежден. Причина: ", reason));
                    }
                    break;
                }
                case "kick": {
                    User _userForKick = this.database.getUserById(arguments[1]);
                    if (_userForKick != null) {
                        LobbyManager _lobby = this.lobbyServices.getLobbyByUser(_userForKick);
                        if (_lobby != null) {
                            _lobby.kick();
                            this.chatModel.sendSystemMessage(_userForKick.getNickname() + " кикнут");
                        }
                    }
                    break;
                }
                case "unban": {
                    if (arguments.length >= 2) {
                        User cu = this.database.getUserById(arguments[1]);
                        if (cu != null) {
                            this.banServices.unbanChat(cu);
                            this.chatModel.sendSystemMessage("Танкисту " + cu.getNickname() + " был разрешён выход в эфир");
                        }
                    }
                    break;
                }
                case "blockgame": {
                    if (arguments.length >= 3) {
                        User victim_ = this.database.getUserById(arguments[1]);
                        boolean var6 = false;

                        int reasonId;
                        try {
                            reasonId = Integer.parseInt(arguments[2]);
                        } catch (Exception var14) {
                            reasonId = 0;
                        }

                        if (victim_ != null) {
                            this.banServices.ban(BanType.GAME, BanTimeType.FOREVER, victim_, spectator.getUser(), BlockGameReason.getReasonById(reasonId).getReason());
                            LobbyManager lobby = this.lobbyServices.getLobbyByNick(victim_.getNickname());
                            if (lobby != null) {
                                lobby.kick();
                            }

                            this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", victim_.getNickname(), " был заблокирован и кикнут"));
                        }
                    }
                    break;
                }
                case "unblockgame": {
                    if (arguments.length >= 2) {
                        User av = this.database.getUserById(arguments[1]);
                        if (av != null) {
                            this.banServices.unblock(av);
                            this.chatModel.sendSystemMessage(av.getNickname() + " разблокирован");
                        }
                    }
                    break;
                }
                case "spawncry": {
                    int count = Integer.parseInt(arguments[1]);
                    int i = 0;
                    while (i < count) {
                        this.spModel.getBattleModel().bonusesSpawnService.spawnBonus(BonusType.CRYSTAL);
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
                        return;
                    }

                    User _victim = this.database.getUserById(arguments[1]);
                    if (_victim == null) {
                        return;
                    }

                    this.banServices.ban(BanType.CHAT, time, _victim, spectator.getUser(), reason);
                    this.chatModel.sendSystemMessage(StringUtils.concatStrings("Танкист ", _victim.getNickname(), " лишен права выхода в эфир ", time.getNameType(), " Причина: ", reason));
                    break;
                }
            }
        } else {
            this.spModel.getBattleModel().sendToAllPlayers(Type.BATTLE, "spectator_message", message);
        }
    }
}
