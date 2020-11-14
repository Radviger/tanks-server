package gtanks.services;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyCommandsConst;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.chat.ChatLobby;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import gtanks.users.User;
import gtanks.users.locations.UserLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum AutoEntryServices {
    INSTANCE;

    private static final String QUARTZ_NAME = "AutoEntryServices GC";
    private static final String QUARTZ_GROUP = "runner";
    private static final ChatLobby chatLobby = ChatLobby.INSTANCE;
    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    public Map<String, Data> playersForAutoEntry = new FastHashMap<>();

    AutoEntryServices() {
        QuartzService quartzService = QuartzServiceImpl.INSTANCE;
        quartzService.addJobInterval(QUARTZ_NAME, QUARTZ_GROUP, (e) -> {
            long currentTime = System.currentTimeMillis();

            for (Data data : this.playersForAutoEntry.values()) {
                if (currentTime - data.createdTime >= 120000L) {
                    this.removePlayer(data.battle, data.userId, data.teamType, data.battle.battleInfo.team);
                }
            }

        }, TimeType.SEC, 30L);
    }

    public void removePlayer(String userId) {
        this.playersForAutoEntry.remove(userId);
    }

    public boolean removePlayer(BattlefieldModel data, String userId, PlayerTeamType teamType, boolean team) {
        if (this.playersForAutoEntry.get(userId) == null) {
            return false;
        } else {
            lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "remove_player_from_battle", JsonUtils.parseRemovePlayerComand(userId, data.battleInfo.battleId));
            if (!team) {
                --data.battleInfo.countPeople;
                lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", data.battleInfo.battleId, ";", String.valueOf(data.battleInfo.countPeople)));
            } else {
                if (teamType == PlayerTeamType.RED) {
                    --data.battleInfo.redPeople;
                } else {
                    --data.battleInfo.bluePeople;
                }

                lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JsonUtils.parseUpdateCoundPeoplesCommand(data.battleInfo));
            }

            this.playersForAutoEntry.remove(userId);
            return true;
        }
    }

    public void prepareToEnter(LobbyManager lobby) {
        AutoEntryServices.Data data = this.playersForAutoEntry.get(lobby.getLocalUser().getNickname());
        if (data == null) {
            this.transmitToLobby(lobby);
        } else {
            BattlefieldModel bModel = data.battle;
            if (bModel == null) {
                this.transmitToLobby(lobby);
            } else {
                this.removePlayer(lobby.getLocalUser().getNickname());
                PlayerStatistic statistic = data.statistic;
                BattleInfo battleInfo = bModel.battleInfo;
                lobby.getLocalUser().setUserLocation(UserLocation.BATTLE);
                lobby.battle = new BattlefieldPlayerController(lobby, bModel, data.teamType);
                lobby.battle.statistic = statistic;
                lobby.disconnectListeners.addListener(lobby.battle);
                lobby.send(Type.BATTLE, "init_battle_model", JsonUtils.parseBattleModelInfo(battleInfo, false));
            }
        }
    }

    private void transmitToLobby(LobbyManager lobby) {
        lobby.send(Type.GARAGE, LobbyCommandsConst.INIT_GARAGE_ITEMS, JsonUtils.parseGarageUser(lobby.getLocalUser()).trim());
        lobby.send(Type.GARAGE, LobbyCommandsConst.INIT_MARKET, JsonUtils.parseMarketItems(lobby.getLocalUser()));
        lobby.send(Type.LOBBY_CHAT, "init_chat");
        lobby.send(Type.LOBBY_CHAT, LobbyCommandsConst.INIT_MESSAGES, JsonUtils.parseChatLobbyMessages(chatLobby.getMessages()));
    }

    public boolean needEnterToBattle(User user) {
        return this.playersForAutoEntry.get(user.getNickname()) != null;
    }

    public void userExit(BattlefieldPlayerController player) {
        AutoEntryServices.Data data = new Data();
        data.battle = player.battle;
        data.statistic = player.statistic;
        data.createdTime = System.currentTimeMillis();
        data.teamType = player.playerTeamType;
        data.userId = player.getUser().getNickname();
        this.playersForAutoEntry.put(player.getUser().getNickname(), data);
    }

    public List<Data> getPlayersByBattle(BattlefieldModel battle) {
        List<Data> players = new ArrayList<>();

        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle != null && data.battle == battle) {
                players.add(data);
            }
        }

        return players;
    }

    public void battleRestarted(BattlefieldModel battle) {
        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle != null && data.battle == battle) {
                data.statistic.clear();
            }
        }
    }

    public void battleDisposed(BattlefieldModel battle) {
        for (Data data : this.playersForAutoEntry.values()) {
            if (data.battle != null && data.battle == battle) {
                this.playersForAutoEntry.remove(data.userId);
            }
        }
    }

    public static class Data {
        public BattlefieldModel battle;
        public PlayerStatistic statistic;
        public PlayerTeamType teamType;
        public long createdTime;
        public String userId;
    }
}
