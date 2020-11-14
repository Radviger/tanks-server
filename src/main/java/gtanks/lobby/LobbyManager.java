package gtanks.lobby;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gtanks.StringUtils;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.maps.Map;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.commands.Command;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattleType;
import gtanks.lobby.battles.BattlesList;
import gtanks.lobby.chat.ChatLobby;
import gtanks.lobby.chat.ChatMessage;
import gtanks.lobby.chat.flood.FloodController;
import gtanks.lobby.chat.flood.LobbyFloodController;
import gtanks.lobby.top.HallOfFame;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.main.netty.ProtocolTransfer;
import gtanks.main.params.OnlineStats;
import gtanks.network.listeners.DisconnectListeners;
import gtanks.services.AutoEntryServices;
import gtanks.services.LobbyServices;
import gtanks.system.dailybonus.DailyBonusService;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;
import gtanks.users.locations.UserLocation;

public class LobbyManager extends LobbyCommandsConst {
    private static final Gson GSON = new Gson();

    private static final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private static final HallOfFame top = HallOfFame.INSTANCE;
    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private static final ChatLobby chatLobby = ChatLobby.INSTANCE;
    private static final DailyBonusService dailyBonusService = DailyBonusService.INSTANCE;
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;
    public ProtocolTransfer pipeline;
    public BattlefieldPlayerController battle;
    public SpectatorController spectatorController;
    public DisconnectListeners disconnectListeners;
    public long timer;
    private User localUser;
    private FloodController chatFloodController;

    public LobbyManager(ProtocolTransfer pipeline, User localUser) {
        this.pipeline = pipeline;
        this.localUser = localUser;
        this.disconnectListeners = new DisconnectListeners();
        this.setChatFloodController(new LobbyFloodController());
        this.timer = System.currentTimeMillis();
        this.localUser.setUserLocation(UserLocation.GARAGE);
        lobbyServices.addLobby(this);
        OnlineStats.addOnline();
        dailyBonusService.userInitialized(this);
    }

    public void send(Type type, String... args) {
        this.pipeline.send(type, args);
    }

    public void executeCommand(Command cmd) {
        try {
            switch (cmd.type) {
                case GARAGE:
                    switch (cmd.args[0]) {
                        case "try_mount_item":
                            if (this.localUser.getGarage().mountItem(cmd.args[1])) {
                                this.send(Type.GARAGE, "mount_item", cmd.args[1]);
                                this.localUser.getGarage().parseJSONData();
                                database.update(this.localUser.getGarage());
                            } else {
                                this.send(Type.GARAGE, "try_mount_item_NO");
                            }
                            break;
                        case "try_update_item":
                            this.onTryUpdateItem(cmd.args[1]);
                            break;
                        case "get_garage_data":
                            if (this.localUser.getGarage().mountHull != null && this.localUser.getGarage().mountTurret != null && this.localUser.getGarage().mountColormap != null) {
                                this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountHull.id, "_m", String.valueOf(this.localUser.getGarage().mountHull.modificationIndex)));
                                this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountTurret.id, "_m", String.valueOf(this.localUser.getGarage().mountTurret.modificationIndex)));
                                this.send(Type.GARAGE, "init_mounted_item", StringUtils.concatStrings(this.localUser.getGarage().mountColormap.id, "_m", String.valueOf(this.localUser.getGarage().mountColormap.modificationIndex)));
                            }
                            break;
                        case "try_buy_item":
                            this.onTryBuyItem(cmd.args[1], Integer.parseInt(cmd.args[2]));
                            break;
                    }
                case CHAT:
                case PING:
                case UNKNOWN:
                case HTTP:
                default:
                    break;
                case LOBBY:
                    switch (cmd.args[0]) {
                        case "get_hall_of_fame_data":
                            this.localUser.setUserLocation(UserLocation.HALL_OF_FAME);
                            this.send(Type.LOBBY, "init_hall_of_fame", JsonUtils.parseHallOfFame(top));
                            break;
                        case "get_garage_data":
                            this.sendGarage();
                            break;
                        case "get_data_init_battle_select":
                            this.sendMapsInit();
                            break;
                        case "check_battleName_for_forbidden_words":
                            this.checkBattleName(cmd.args[1]);
                            break;
                        case "try_create_battle_dm":
                            this.tryCreateBattleDM(cmd.args[1], cmd.args[2], Integer.parseInt(cmd.args[3]), Integer.parseInt(cmd.args[4]), Integer.parseInt(cmd.args[5]), Integer.parseInt(cmd.args[6]), Integer.parseInt(cmd.args[7]), this.stringToBoolean(cmd.args[8]), this.stringToBoolean(cmd.args[9]), this.stringToBoolean(cmd.args[10]));
                            break;
                        case "try_create_battle_tdm":
                            this.tryCreateTDMBattle(cmd.args[1]);
                            break;
                        case "try_create_battle_ctf":
                            this.tryCreateCTFBattle(cmd.args[1]);
                            break;
                        case "get_show_battle_info":
                            this.sendBattleInfo(cmd.args[1]);
                            break;
                        case "enter_battle":
                            this.onEnterInBattle(cmd.args[1]);
                            break;
                        case "bug_report":

                            break;
                        case "screenshot":

                            break;
                        case "enter_battle_team":
                            this.onEnterInTeamBattle(cmd.args[1], Boolean.parseBoolean(cmd.args[2]));
                            break;
                        case "enter_battle_spectator":
                            if (this.getLocalUser().getType() == TypeUser.DEFAULT) {
                                return;
                            }

                            this.enterInBattleBySpectator(cmd.args[1]);
                            break;
                        case "user_inited":
                            dailyBonusService.userLoaded(this);
                            break;
                    }
                    break;
                case LOBBY_CHAT:
                    chatLobby.addMessage(new ChatMessage(this.localUser, cmd.args[0], this.stringToBoolean(cmd.args[1]), cmd.args[2].equals("NULL") ? null : database.getUserById(cmd.args[2]), this));
                    break;
                case BATTLE:
                    if (this.battle != null) {
                        this.battle.executeCommand(cmd);
                    }

                    if (this.spectatorController != null) {
                        this.spectatorController.executeCommand(cmd);
                    }
                    break;
                case SYSTEM:
                    if (cmd.args[0].equals("c01")) {
                        this.kick();
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void enterInBattleBySpectator(String battleId) {
        BattleInfo battle = BattlesList.getBattleInfoById(battleId);
        if (battle != null) {
            battle.model.spectatorModel.addSpectator(this.spectatorController = new SpectatorController(this, battle.model, battle.model.spectatorModel));
            this.localUser.setUserLocation(UserLocation.BATTLE);
            this.send(Type.BATTLE, "init_battle_model", JsonUtils.parseBattleModelInfo(battle, true));
            Logger.log("User " + this.localUser.getNickname() + " enter in battle by spectator.");
        }
    }

    private void sendTableMessage(String msg) {
        this.send(Type.LOBBY, "server_message", msg);
    }

    private void tryCreateCTFBattle(String json) {
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }

                this.sendTableMessage("Вы можете создавать не более трех битв в течении 5 минут.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }

        JsonObject parser = GSON.fromJson(json, JsonObject.class);

        BattleInfo battle = new BattleInfo();
        battle.battleType = BattleType.CTF;
        battle.isPaid = parser.get("pay").getAsBoolean();
        battle.isPrivate = parser.get("privateBattle").getAsBoolean();
        battle.friendlyFire = parser.get("frielndyFire").getAsBoolean();
        battle.name = parser.get("gameName").getAsString();
        battle.map = MapsLoader.maps.get(parser.get("mapId").getAsString());
        battle.maxPeople = parser.get("numPlayers").getAsInt();
        battle.numFlags = parser.get("numFlags").getAsInt();
        battle.minRank = parser.get("minRang").getAsInt();
        battle.maxRank = parser.get("maxRang").getAsInt();
        battle.team = true;
        battle.time = parser.get("time").getAsInt();
        battle.autobalance = parser.get("autoBalance").getAsBoolean();
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }

        if (battle.maxPeople < 2) {
            battle.maxPeople = 2;
        }

        if (battle.time <= 0 && battle.numFlags <= 0) {
            battle.time = 15;
            battle.numFlags = 0;
        }

        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }

        if (battle.numKills > 999) {
            battle.numKills = 999;
        }

        BattlesList.tryCreateBattle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void tryCreateTDMBattle(String json) {
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }

                this.sendTableMessage("Вы можете создавать не более трех битв в течении 5 минут.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }


        JsonObject parser = GSON.fromJson(json, JsonObject.class);
      
        BattleInfo battle = new BattleInfo();
        battle.battleType = BattleType.TDM;
        battle.isPaid = parser.get("pay").getAsBoolean();
        battle.isPrivate = parser.get("privateBattle").getAsBoolean();
        battle.friendlyFire = parser.get("frielndyFire").getAsBoolean();
        battle.name = parser.get("gameName").getAsString();
        battle.map = MapsLoader.maps.get(parser.get("mapId").getAsString());
        battle.maxPeople = parser.get("numPlayers").getAsInt();
        battle.numKills = parser.get("numKills").getAsInt();
        battle.minRank = parser.get("minRang").getAsInt();
        battle.maxRank = parser.get("maxRang").getAsInt();
        battle.team = true;
        battle.time = parser.get("time").getAsInt();
        battle.autobalance = parser.get("autoBalance").getAsBoolean();
        Map map = battle.map;
        if (battle.maxRank < battle.minRank) {
            battle.maxRank = battle.minRank;
        }

        if (battle.maxPeople < 2) {
            battle.maxPeople = 2;
        }

        if (battle.time <= 0 && battle.numKills <= 0) {
            battle.time = 900;
            battle.numKills = 0;
        }

        if (battle.maxPeople > map.maxPlayers) {
            battle.maxPeople = map.maxPlayers;
        }

        if (battle.numKills > 999) {
            battle.numKills = 999;
        }

        BattlesList.tryCreateBattle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    public void onExitFromBattle() {
        if (this.battle != null) {
            if (this.autoEntryServices.removePlayer(this.battle.battle, this.getLocalUser().getNickname(), this.battle.playerTeamType, this.battle.battle.battleInfo.team)) {
                this.battle.destroy(true);
            } else {
                this.battle.destroy(false);
            }

            this.battle = null;
            this.disconnectListeners.removeListener(this.battle);
        }

        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }

        this.send(Type.LOBBY_CHAT, INIT_MESSAGES, JsonUtils.parseChatLobbyMessages(chatLobby.getMessages()));
    }

    public void onExitFromStatistic() {
        this.onExitFromBattle();
        this.sendMapsInit();
    }

    private void onEnterInTeamBattle(String battleId, boolean red) {
        this.localUser.setUserLocation(UserLocation.BATTLE);
        if (this.battle == null) {
            BattleInfo battleInfo = BattlesList.getBattleInfoById(battleId);
            if (battleInfo != null) {
                if (battleInfo.model.players.size() < battleInfo.maxPeople * 2) {
                    if (red) {
                        ++battleInfo.redPeople;
                    } else {
                        ++battleInfo.bluePeople;
                    }

                    this.battle = new BattlefieldPlayerController(this, battleInfo.model, red ? PlayerTeamType.RED : PlayerTeamType.BLUE);
                    this.disconnectListeners.addListener(this.battle);
                    lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JsonUtils.parseUpdateCoundPeoplesCommand(battleInfo));
                    this.send(Type.BATTLE, "init_battle_model", JsonUtils.parseBattleModelInfo(battleInfo, false));
                    lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle", JsonUtils.parseAddPlayerComand(this.battle, battleInfo));
                }
            }
        }
    }

    private void onEnterInBattle(String battleId) {
        this.localUser.setUserLocation(UserLocation.BATTLE);
        this.autoEntryServices.removePlayer(this.getLocalUser().getNickname());
        if (this.battle == null) {
            BattleInfo battleInfo = BattlesList.getBattleInfoById(battleId);
            if (battleInfo != null) {
                if (battleInfo.model.players.size() < battleInfo.maxPeople) {
                    this.battle = new BattlefieldPlayerController(this, battleInfo.model, PlayerTeamType.NONE);
                    this.disconnectListeners.addListener(this.battle);
                    ++battleInfo.countPeople;
                    System.out.println("incration");
                    if (!battleInfo.team) {
                        lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", battleId, ";", String.valueOf(this.battle.battle.battleInfo.countPeople)));
                    } else {
                        lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JsonUtils.parseUpdateCoundPeoplesCommand(battleInfo));
                    }

                    this.send(Type.BATTLE, "init_battle_model", JsonUtils.parseBattleModelInfo(battleInfo, false));
                    lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "add_player_to_battle", JsonUtils.parseAddPlayerComand(this.battle, battleInfo));
                }
            }
        }
    }

    private void sendBattleInfo(String id) {
        this.send(Type.LOBBY, SHOW_BATTLE_INFO, JsonUtils.parseBattleInfoShow(BattlesList.getBattleInfoById(id), this.getLocalUser().getType() != TypeUser.DEFAULT && this.getLocalUser().getType() != TypeUser.TESTER));
    }

    private void tryCreateBattleDM(String gameName, String mapId, int time, int kills, int maxPlayers, int minRang, int maxRang, boolean isPrivate, boolean pay, boolean mm) {
        if (System.currentTimeMillis() - this.localUser.getAntiCheatData().lastTimeCreationBattle <= 300000L) {
            if (this.localUser.getAntiCheatData().countCreatedBattles >= 3) {
                if (this.localUser.getAntiCheatData().countWarningForFludCreateBattle >= 5) {
                    this.kick();
                }

                this.sendTableMessage("Вы можете создавать не более трех битв в течении 5 минут.");
                ++this.localUser.getAntiCheatData().countWarningForFludCreateBattle;
                return;
            }
        } else {
            this.localUser.getAntiCheatData().countCreatedBattles = 0;
            this.localUser.getAntiCheatData().countWarningForFludCreateBattle = 0;
        }

        BattleInfo battle = new BattleInfo();
        Map map = MapsLoader.maps.get(mapId);
        if (maxRang < minRang) {
            maxRang = minRang;
        }

        if (maxPlayers < 2) {
            maxPlayers = 2;
        }

        if (time <= 0 && kills <= 0) {
            time = 900;
            kills = 0;
        }

        if (maxPlayers > map.maxPlayers) {
            maxPlayers = map.maxPlayers;
        }

        if (kills > 999) {
            kills = 999;
        }

        battle.name = gameName;
        battle.map = MapsLoader.maps.get(mapId);
        battle.time = time;
        battle.numKills = kills;
        battle.maxPeople = maxPlayers;
        battle.minRank = minRang;
        battle.countPeople = 0;
        battle.maxRank = maxRang;
        battle.team = false;
        battle.isPrivate = isPrivate;
        battle.isPaid = pay;
        BattlesList.tryCreateBattle(battle);
        this.localUser.getAntiCheatData().lastTimeCreationBattle = System.currentTimeMillis();
        ++this.localUser.getAntiCheatData().countCreatedBattles;
    }

    private void checkBattleName(String name) {
        this.send(Type.LOBBY, "check_battle_name", name);
    }

    private void sendMapsInit() {
        this.localUser.setUserLocation(UserLocation.BATTLESELECT);
        this.send(Type.LOBBY, "init_battle_select", JsonUtils.parseBattleMapList());
    }

    private void sendGarage() {
        this.localUser.setUserLocation(UserLocation.GARAGE);
        this.send(Type.GARAGE, "init_garage_items", JsonUtils.parseGarageUser(this.localUser).trim());
        this.send(Type.GARAGE, "init_market", JsonUtils.parseMarketItems(this.localUser));
    }

    public synchronized void onTryUpdateItem(String id) {
        Item item = this.localUser.getGarage().getItemById(id.substring(0, id.length() - 3));
        int modificationID = Integer.parseInt(id.substring(id.length() - 1));
        if (this.checkMoney(item.modifications[modificationID + 1].price)) {
            if (this.getLocalUser().getRang() + 1 < item.modifications[modificationID + 1].rank) {
                return;
            }

            if (this.localUser.getGarage().updateItem(id)) {
                this.send(Type.GARAGE, "update_item", id);
                this.addCrystall(-item.modifications[modificationID + 1].price);
                this.localUser.getGarage().parseJSONData();
                database.update(this.localUser.getGarage());
            }
        } else {
            this.send(Type.GARAGE, "try_update_NO");
        }

    }

    public synchronized void onTryBuyItem(String itemId, int count) {
        if (count > 0 && count <= 9999) {
            Item item = GarageItemsLoader.items.get(itemId.substring(0, itemId.length() - 3));
            int price = item.price * count;
            int itemRang = item.modifications[0].rank;
            if (this.checkMoney(price)) {
                if (this.getLocalUser().getRang() + 1 < itemRang) {
                    return;
                }
                Item fromUser = this.localUser.getGarage().buyItem(itemId, count, 0);

                if (fromUser != null) {
                    this.send(Type.GARAGE, "buy_item", StringUtils.concatStrings(item.id, "_m", String.valueOf(item.modificationIndex)), JsonUtils.parseItemInfo(fromUser));
                    this.addCrystall(-price);
                    this.localUser.getGarage().parseJSONData();
                    database.update(this.localUser.getGarage());
                } else {
                    this.send(Type.GARAGE, "try_buy_item_NO");
                }
            }

        } else {
            this.crystallToZero();
        }
    }

    private boolean checkMoney(int buyValue) {
        return this.localUser.getCrystall() - buyValue >= 0;
    }

    public synchronized void addCrystall(int value) {
        this.localUser.addCrystall(value);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    public void crystallToZero() {
        this.localUser.setCrystall(0);
        this.send(Type.LOBBY, "add_crystall", String.valueOf(this.localUser.getCrystall()));
        database.update(this.localUser);
    }

    private boolean stringToBoolean(String src) {
        return src.toLowerCase().equals("true");
    }

    public void onDisconnect() {
        database.uncache(this.localUser.getNickname());
        lobbyServices.removeLobby(this);
        OnlineStats.removeOnline();
        if (this.spectatorController != null) {
            this.spectatorController.onDisconnect();
            this.spectatorController = null;
        }

        if (this.battle != null) {
            this.battle.onDisconnect();
            this.battle = null;
        }

        this.localUser.session = null;
    }

    public void kick() {
        this.pipeline.closeConnection();
    }

    public User getLocalUser() {
        return this.localUser;
    }

    public void setLocalUser(User localUser) {
        this.localUser = localUser;
    }

    public FloodController getChatFloodController() {
        return this.chatFloodController;
    }

    public void setChatFloodController(FloodController chatFloodController) {
        this.chatFloodController = chatFloodController;
    }
}
