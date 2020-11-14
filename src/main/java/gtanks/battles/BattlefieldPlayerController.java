package gtanks.battles;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.StringUtils;
import gtanks.battles.commands.BattlefieldPlayerCommandsConst;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.inventory.InventoryController;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.colormaps.ColorMapsFactory;
import gtanks.battles.tanks.loaders.HullsFactory;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.commands.Command;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.network.listeners.DisconnectListener;
import gtanks.services.AutoEntryServices;
import gtanks.services.LobbyServices;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.locations.UserLocation;

import java.util.Objects;

public class BattlefieldPlayerController extends BattlefieldPlayerCommandsConst implements DisconnectListener, Comparable<BattlefieldPlayerController> {
    public static final Gson GSON = new Gson();

    public LobbyManager parentLobby;
    public BattlefieldModel battle;
    public Tank tank;
    public PlayerStatistic statistic;
    public PlayerTeamType playerTeamType;
    public FlagServer flag;
    public InventoryController inventory;
    public long timer;
    public long lastFireTime;
    public boolean userInited = false;
    private final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;

    public BattlefieldPlayerController(LobbyManager parent, BattlefieldModel battle, PlayerTeamType playerTeamType) {
        this.parentLobby = parent;
        this.battle = battle;
        this.playerTeamType = playerTeamType;
        this.tank = new Tank(null);
        this.tank.setHull(Objects.requireNonNull(HullsFactory.getHull(this.getGarage().mountHull.getId())));
        this.tank.setWeapon(WeaponsFactory.getWeapon(this.getGarage().mountTurret.getId(), this, battle));
        this.tank.setColormap(ColorMapsFactory.getColormap(this.getGarage().mountColormap.getId()));
        this.statistic = new PlayerStatistic(0, 0, 0);
        this.inventory = new InventoryController(this);
        battle.addPlayer(this);
        this.sendShotsData();
    }

    public User getUser() {
        return this.parentLobby.getLocalUser();
    }

    public Garage getGarage() {
        return this.parentLobby.getLocalUser().getGarage();
    }

    public void executeCommand(Command cmd) {
        try {
            switch (cmd.type) {
                case GARAGE:
                case PING:
                    break;
                case CHAT:
                case LOBBY:
                case LOBBY_CHAT:
                default:
                    Logger.log(Type.ERROR, "User " + this.parentLobby.getLocalUser().getNickname() + "[" + this.parentLobby.pipeline.toString() + "] send unknowed request!");
                    break;
                case BATTLE:
                    switch (cmd.args[0]) {
                        case GET_INIT_DATA_LOCAL_TANK:
                            this.battle.initLocalTank(this);
                            break;
                        case ACTIVATE_TANKS:
                            this.battle.activateTank(this);
                            break;
                        case SUICIDE:
                            this.battle.respawnPlayer(this, true);
                            break;
                        case MOVE:
                            this.parseAndMove(cmd.args);
                            break;
                        case CHAT:
                            this.battle.chatModel.onMessage(this, cmd.args[1], Boolean.parseBoolean(cmd.args[2]));
                            break;
                        case ATTEMPT_TO_TAKE_BONUS:
                            this.battle.onTakeBonus(this, cmd.args[1]);
                            break;
                        case START_FIRE:
                            if (this.tank.state.equals("active")) {
                                this.tank.getWeapon().startFire(cmd.args.length >= 2 ? GSON.fromJson(cmd.args[1], JsonObject.class) : new JsonObject());
                            }
                            break;
                        case FIRE:
                            if (this.tank.state.equals("active")) {
                                this.tank.getWeapon().fire(GSON.fromJson(cmd.args[1], JsonObject.class));
                            }
                            break;
                        case I_EXIT_FROM_BATTLE:
                            this.parentLobby.onExitFromBattle();
                            break;
                        case STOP_FIRE:
                            this.tank.getWeapon().stopFire();
                            break;
                        case EXIT_FROM_STATISTIC:
                            this.parentLobby.onExitFromStatistic();
                            break;
                        case ATTEMPT_TO_TAKE_FLAG:
                            this.battle.ctfModel.attemptToTakeFlag(this, PlayerTeamType.valueOf(cmd.args[1]));
                            break;
                        case FLAG_DROP:
                            this.parseAndDropFlag(cmd.args[1]);
                            break;
                        case SPEEDHACK_DETECTED:
                            this.battle.cheatDetected(this, this);
                            break;
                        case ACTIVATE_ITEM:
                            Vector3 _tankPos;
                            try {
                                _tankPos = new Vector3(Float.parseFloat(cmd.args[2]), Float.parseFloat(cmd.args[3]), Float.parseFloat(cmd.args[4]));
                            } catch (Exception var4) {
                                _tankPos = new Vector3(0.0F, 0.0F, 0.0F);
                            }

                            this.inventory.activateItem(cmd.args[1], _tankPos);
                            break;
                        case MINE_HIT:
                            this.battle.battleMinesModel.hitMine(this, cmd.args[1]);
                            break;
                        case CHECK_MD5_MAP:
                            this.battle.mapChecksumModel.check(this, cmd.args[1]);
                            break;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseAndDropFlag(String json) {
        try {
            JsonObject flag = GSON.fromJson(json, JsonObject.class);
            float x = flag.get("x").getAsFloat();
            float y = flag.get("y").getAsFloat();
            float z = flag.get("z").getAsFloat();
            this.battle.ctfModel.dropFlag(this, new Vector3(x, y, z));
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    public void sendShotsData() {
        this.send(gtanks.commands.Type.BATTLE, INIT_SHOTS_DATA, WeaponsFactory.getJSONList());
    }

    public void parseAndMove(String[] args) {
        try {
            Vector3 pos = new Vector3(0.0F, 0.0F, 0.0F);
            Vector3 orient = new Vector3(0.0F, 0.0F, 0.0F);
            Vector3 line = new Vector3(0.0F, 0.0F, 0.0F);
            Vector3 ange = new Vector3(0.0F, 0.0F, 0.0F);
            String[] temp = args[1].split("@");
            pos.x = Float.parseFloat(temp[0]);
            pos.y = Float.parseFloat(temp[1]);
            pos.z = Float.parseFloat(temp[2]);
            orient.x = Float.parseFloat(temp[3]);
            orient.y = Float.parseFloat(temp[4]);
            orient.z = Float.parseFloat(temp[5]);
            line.x = Float.parseFloat(temp[6]);
            line.y = Float.parseFloat(temp[7]);
            line.z = Float.parseFloat(temp[8]);
            ange.x = Float.parseFloat(temp[9]);
            ange.y = Float.parseFloat(temp[10]);
            ange.z = Float.parseFloat(temp[11]);
            float turretDir = Float.parseFloat(args[2]);
            int bits = Integer.parseInt(args[3]);
            if (this.tank.position == null) {
                this.tank.position = new Vector3(0.0F, 0.0F, 0.0F);
            }

            this.tank.position = pos;
            this.tank.orientation = orient;
            this.tank.linVel = line;
            this.tank.angVel = ange;
            this.tank.turretDir = turretDir;
            this.tank.controllBits = bits;
            this.battle.moveTank(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearEffects() {
        while (this.tank.activeEffects.size() > 0) {
            this.tank.activeEffects.get(0).deactivate();
        }
    }

    public void toggleTeamType() {
        switch (this.playerTeamType) {
            case NONE:
                return;
            case BLUE:
                this.playerTeamType = PlayerTeamType.RED;
                ++this.battle.battleInfo.redPeople;
                --this.battle.battleInfo.bluePeople;
                break;
            default:
                this.playerTeamType = PlayerTeamType.BLUE;
                --this.battle.battleInfo.redPeople;
                ++this.battle.battleInfo.bluePeople;
                break;
        }

        this.lobbyServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JsonUtils.parseUpdateCoundPeoplesCommand(this.battle.battleInfo));
        this.battle.sendToAllPlayers(gtanks.commands.Type.BATTLE, "change_user_team", this.tank.id, this.playerTeamType.name());
    }

    public void destroy(boolean cache) {
        this.battle.removeUser(this, cache);
        if (!cache) {
            this.lobbyServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, "remove_player_from_battle", JsonUtils.parseRemovePlayerComand(this));
            if (!this.battle.battleInfo.team) {
                this.lobbyServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, StringUtils.concatStrings("update_count_users_in_dm_battle", ";", this.battle.battleInfo.battleId, ";", String.valueOf(this.battle.players.size())));
            } else {
                this.lobbyServices.sendCommandToAllUsers(gtanks.commands.Type.LOBBY, UserLocation.BATTLESELECT, "update_count_users_in_team_battle", JsonUtils.parseUpdateCoundPeoplesCommand(this.battle.battleInfo));
            }
        }

        this.parentLobby = null;
        this.battle = null;
        this.tank = null;
    }

    public void send(gtanks.commands.Type type, String... args) {
        if (this.parentLobby != null) {
            this.parentLobby.send(type, args);
        }
    }

    @Override
    public void onDisconnect() {
        this.autoEntryServices.userExit(this);
        this.destroy(true);
    }

    @Override
    public int compareTo(BattlefieldPlayerController o) {
        return (int) (o.statistic.getScore() - this.statistic.getScore());
    }
}
