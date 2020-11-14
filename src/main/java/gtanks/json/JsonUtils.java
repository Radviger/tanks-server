package gtanks.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.chat.BattleChatMessage;
import gtanks.battles.ctf.CTFModel;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.maps.Map;
import gtanks.battles.maps.MapsLoader;
import gtanks.battles.mines.ServerMine;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.frezee.FrezeeEntity;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.battles.tanks.weapons.twins.TwinsEntity;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattleType;
import gtanks.lobby.battles.BattlesList;
import gtanks.lobby.chat.ChatMessage;
import gtanks.lobby.top.HallOfFame;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.services.AutoEntryServices;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;
import gtanks.users.garage.items.PropertyItem;
import gtanks.users.garage.items.modification.ModificationInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class JsonUtils {
    private static final Gson GSON = new Gson();

    private static final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;
    private static final DatabaseManager databaseManager = DatabaseManagerHibernate.INSTANCE;

    public static String parseConfiguratorEntity(Object entity, Class<?> clazz) {
        JsonObject obj = new JsonObject();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value instanceof Number) {
                    obj.addProperty(field.getName(), (Number) value);
                } else if (value instanceof String) {
                    obj.addProperty(field.getName(), (String) value);
                } else {
                    throw new IllegalArgumentException("Invalid json primitive type: " + value.getClass() + " for field " + field.getName());
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException var7) {
            var7.printStackTrace();
        }

        return GSON.toJson(obj);
    }

    public static String parseInitMinesComand(java.util.Map<BattlefieldPlayerController, List<ServerMine>> mines) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();

        for (List<ServerMine> userMines : mines.values()) {
            for (ServerMine mine : userMines) {
                JsonObject _mine = new JsonObject();
                _mine.addProperty("ownerId", mine.getOwner().tank.id);
                _mine.addProperty("mineId", mine.getId());
                _mine.addProperty("x", mine.getPosition().x);
                _mine.addProperty("y", mine.getPosition().y);
                _mine.addProperty("z", mine.getPosition().z);
                array.add(_mine);
            }
        }

        obj.add("mines", array);
        return GSON.toJson(obj);
    }

    public static String parsePutMineComand(ServerMine mine) {
        JsonObject obj = new JsonObject();
        obj.addProperty("mineId", mine.getId());
        obj.addProperty("userId", mine.getOwner().tank.id);
        obj.addProperty("x", mine.getPosition().x);
        obj.addProperty("y", mine.getPosition().y);
        obj.addProperty("z", mine.getPosition().z);
        return GSON.toJson(obj);
    }

    public static String parseInitInventoryComand(Garage garage) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();

        for (Item item : garage.getInventoryItems()) {
            JsonObject io = new JsonObject();
            io.addProperty("id", item.id);
            io.addProperty("count", item.count);
            io.addProperty("slotId", item.index);
            io.addProperty("itemEffectTime", item.id.equals("mine") ? 20 : (item.id.equals("health") ? 20 : 55));
            io.addProperty("itemRestSec", 10);
            array.add(io);
        }

        obj.add("items", array);
        return GSON.toJson(obj);
    }

    public static String parseRemovePlayerComand(BattlefieldPlayerController player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", player.battle.battleInfo.battleId);
        obj.addProperty("id", player.getUser().getNickname());
        return GSON.toJson(obj);
    }

    public static String parseRemovePlayerComand(String userId, String battleid) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", battleid);
        obj.addProperty("id", userId);
        return GSON.toJson(obj);
    }

    public static String parseAddPlayerComand(BattlefieldPlayerController player, BattleInfo battleInfo) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", battleInfo.battleId);
        obj.addProperty("id", player.getUser().getNickname());
        obj.addProperty("kills", player.statistic.getScore());
        obj.addProperty("name", player.getUser().getNickname());
        obj.addProperty("rank", player.getUser().getRang() + 1);
        obj.addProperty("type", player.playerTeamType.name());
        return GSON.toJson(obj);
    }

    public static String parseDropFlagCommand(FlagServer flag) {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", flag.position.x);
        obj.addProperty("y", flag.position.y);
        obj.addProperty("z", flag.position.z);
        obj.addProperty("flagTeam", flag.flagTeamType.name());
        return GSON.toJson(obj);
    }

    public static String parseCTFModelData(BattlefieldModel model) {
        Map map = model.battleInfo.map;
        JsonObject obj = new JsonObject();
        CTFModel ctfModel = model.ctfModel;
        Vector3 blueFlagPos = ctfModel.getBlueFlag().position;
        Vector3 redFlagPos = ctfModel.getRedFlag().position;
        JsonObject basePosBlue = new JsonObject();
        basePosBlue.addProperty("x", map.flagBluePosition.x);
        basePosBlue.addProperty("y", map.flagBluePosition.y);
        basePosBlue.addProperty("z", map.flagBluePosition.z);
        JsonObject basePosRed = new JsonObject();
        basePosRed.addProperty("x", map.flagRedPosition.x);
        basePosRed.addProperty("y", map.flagRedPosition.y);
        basePosRed.addProperty("z", map.flagRedPosition.z);
        JsonObject posBlue = new JsonObject();
        posBlue.addProperty("x", blueFlagPos.x);
        posBlue.addProperty("y", blueFlagPos.y);
        posBlue.addProperty("z", blueFlagPos.z);
        JsonObject posRed = new JsonObject();
        posRed.addProperty("x", redFlagPos.x);
        posRed.addProperty("y", redFlagPos.y);
        posRed.addProperty("z", redFlagPos.z);
        obj.add("basePosBlueFlag", basePosBlue);
        obj.add("basePosRedFlag", basePosRed);
        obj.add("posBlueFlag", posBlue);
        obj.add("posRedFlag", posRed);
        obj.addProperty("blueFlagCarrierId", ctfModel.getBlueFlag().owner == null ? null : ctfModel.getBlueFlag().owner.tank.id);
        obj.addProperty("redFlagCarrierId", ctfModel.getRedFlag().owner == null ? null : ctfModel.getRedFlag().owner.tank.id);
        return GSON.toJson(obj);
    }

    public static String parseUpdateCoundPeoplesCommand(BattleInfo battle) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", battle.battleId);
        obj.addProperty("redPeople", battle.redPeople);
        obj.addProperty("bluePeople", battle.bluePeople);
        return GSON.toJson(obj);
    }

    public static String parseFishishBattle(java.util.Map<String, BattlefieldPlayerController> players, int timeToRestart) {
        JsonObject obj = new JsonObject();
        JsonArray users = new JsonArray();
        obj.addProperty("time_to_restart", timeToRestart);
        if (players == null) {
            return GSON.toJson(obj);
        } else {
            for (BattlefieldPlayerController bpc : players.values()) {
                JsonObject stat = new JsonObject();
                stat.addProperty("kills", bpc.statistic.getKills());
                stat.addProperty("deaths", bpc.statistic.getDeaths());
                stat.addProperty("id", bpc.getUser().getNickname());
                stat.addProperty("rank", bpc.getUser().getRang() + 1);
                stat.addProperty("prize", bpc.statistic.getPrize());
                stat.addProperty("team_type", bpc.playerTeamType.name());
                stat.addProperty("score", bpc.statistic.getScore());
                users.add(stat);
            }

            obj.add("users", users);
            return GSON.toJson(obj);
        }
    }

    public static String parsePlayerStatistic(BattlefieldPlayerController player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("kills", player.statistic.getKills());
        obj.addProperty("deaths", player.statistic.getDeaths());
        obj.addProperty("id", player.getUser().getNickname());
        obj.addProperty("rank", player.getUser().getRang() + 1);
        obj.addProperty("team_type", player.playerTeamType.name());
        obj.addProperty("score", player.statistic.getScore());
        return GSON.toJson(obj);
    }

    public static String parseSpawnCommand(BattlefieldPlayerController bpc, Vector3 pos) {
        JsonObject obj = new JsonObject();
        if (bpc != null && bpc.tank != null) {
            obj.addProperty("tank_id", bpc.tank.id);
            obj.addProperty("health", bpc.tank.health);
            obj.addProperty("speed", bpc.tank.speed);
            obj.addProperty("turn_speed", bpc.tank.turnSpeed);
            obj.addProperty("turret_rotation_speed", bpc.tank.turretRotationSpeed);
            obj.addProperty("incration_id", bpc.battle.incration);
            obj.addProperty("team_type", bpc.playerTeamType.name());
            obj.addProperty("x", pos.x);
            obj.addProperty("y", pos.y);
            obj.addProperty("z", pos.z);
            obj.addProperty("rot", pos.rot);
            return GSON.toJson(obj);
        } else {
            return null;
        }
    }

    public static String parseBattleData(BattlefieldModel model) {
        JsonObject obj = new JsonObject();
        JsonArray users = new JsonArray();
        obj.addProperty("name", model.battleInfo.name);
        obj.addProperty("fund", model.tanksKillModel.getBattleFund());
        obj.addProperty("scoreLimit", model.battleInfo.battleType == BattleType.CTF ? model.battleInfo.numFlags : model.battleInfo.numKills);
        obj.addProperty("timeLimit", model.battleInfo.time);
        obj.addProperty("currTime", model.getTimeLeft());
        obj.addProperty("score_red", model.battleInfo.scoreRed);
        obj.addProperty("score_blue", model.battleInfo.scoreBlue);
        obj.addProperty("team", model.battleInfo.team);

        for (BattlefieldPlayerController bpc : model.players.values()) {
            JsonObject usr = new JsonObject();
            usr.addProperty("nickname", bpc.parentLobby.getLocalUser().getNickname());
            usr.addProperty("rank", bpc.parentLobby.getLocalUser().getRang() + 1);
            usr.addProperty("teamType", bpc.playerTeamType.name());
            users.add(usr);
        }

        obj.add("users", users);
        return GSON.toJson(obj);
    }

    public static String parseUserToJSON(User user) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", user.getNickname());
        obj.addProperty("crystall", user.getCrystall());
        obj.addProperty("email", user.getEmail());
        obj.addProperty("tester", user.getType() != TypeUser.DEFAULT);
        obj.addProperty("next_score", user.getNextScore());
        obj.addProperty("place", user.getPlace());
        obj.addProperty("rang", user.getRang() + 1);
        obj.addProperty("rating", user.getRating());
        obj.addProperty("score", user.getScore());
        return GSON.toJson(obj);
    }

    public static JsonObject parseUserToJsonObject(User user) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", user.getNickname());
        obj.addProperty("crystall", user.getCrystall());
        obj.addProperty("email", user.getEmail());
        obj.addProperty("tester", user.getType() != TypeUser.DEFAULT);
        obj.addProperty("next_score", user.getNextScore());
        obj.addProperty("place", user.getPlace());
        obj.addProperty("rang", user.getRang() + 1);
        obj.addProperty("rating", user.getRating());
        obj.addProperty("score", user.getScore());
        return obj;
    }

    public static String parseHallOfFame(HallOfFame top) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();

        for (User user : top.getData()) {
            array.add(parseUserToJsonObject(user));
        }

        obj.add("users_data", array);
        return GSON.toJson(obj);
    }

    public static String parseChatLobbyMessage(ChatMessage msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", msg.user.getNickname());
        obj.addProperty("rang", msg.user.getRang() + 1);
        obj.addProperty("message", msg.message);
        obj.addProperty("addressed", msg.addressed);
        obj.addProperty("nameTo", msg.userTo == null ? "NULL" : msg.userTo.getNickname());
        obj.addProperty("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.addProperty("system", msg.system);
        obj.addProperty("yellow", msg.yellowMessage);
        return GSON.toJson(obj);
    }

    public static JsonObject parseChatLobbyMessageObject(ChatMessage msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", msg.user == null ? "" : msg.user.getNickname());
        obj.addProperty("rang", msg.user == null ? 0 : msg.user.getRang() + 1);
        obj.addProperty("message", msg.message);
        obj.addProperty("addressed", msg.addressed);
        obj.addProperty("nameTo", msg.userTo == null ? "" : msg.userTo.getNickname());
        obj.addProperty("rangTo", msg.userTo == null ? 0 : msg.userTo.getRang() + 1);
        obj.addProperty("system", msg.system);
        obj.addProperty("yellow", msg.yellowMessage);
        return obj;
    }

    public static String parseChatLobbyMessages(Collection<ChatMessage> messages) {
        JsonObject obj = new JsonObject();
        JsonArray msg = new JsonArray();

        for (ChatMessage m : messages) {
            msg.add(parseChatLobbyMessageObject(m));
        }

        obj.add("messages", msg);
        return GSON.toJson(obj);
    }

    public static String parseGarageUser(User user) {
        try {
            Garage garage = user.getGarage();
            JsonObject obj = new JsonObject();
            JsonArray array = new JsonArray();

            for (Item item : garage.items) {
                JsonObject i = new JsonObject();
                JsonArray properties = new JsonArray();
                JsonArray modification = new JsonArray();
                i.addProperty("id", item.id);
                i.addProperty("name", item.name.localize(user.getLocalization()));
                i.addProperty("description", item.description.localize(user.getLocalization()));
                i.addProperty("isInventory", boolToString(item.isInventory));
                i.addProperty("index", item.index);
                int value = Integer.parseInt(item.itemType.toString());
                i.addProperty("type", value);
                i.addProperty("modificationID", item.modificationIndex);
                i.addProperty("next_price", item.nextPrice);
                i.addProperty("next_rank", item.nextRankId);
                i.addProperty("price", item.price);
                i.addProperty("rank", item.rankId);
                i.addProperty("count", item.count);
                if (item.properties != null) {
                    for (PropertyItem prop : item.properties) {
                        if (prop != null && prop.property != null) {
                            properties.add(parseProperty(prop));
                        }
                    }
                }

                if (item.modifications != null) {
                    for (ModificationInfo mod : item.modifications) {
                        JsonObject m = new JsonObject();
                        JsonArray prop = new JsonArray();
                        m.addProperty("previewId", mod.previewId);
                        m.addProperty("price", mod.price);
                        m.addProperty("rank", mod.rank);
                        if (mod.propertys != null) {
                            PropertyItem[] var19;
                            int var18 = (var19 = mod.propertys).length;

                            for (int var17 = 0; var17 < var18; ++var17) {
                                PropertyItem a = var19[var17];
                                if (a != null && a.property != null) {
                                    prop.add(parseProperty(a));
                                }
                            }
                        }

                        m.add("properts", prop);
                        modification.add(m);
                    }
                }

                i.add("properts", properties);
                i.add("modification", modification);
                array.add(i);
            }

            obj.add("items", array);
            return GSON.toJson(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String parseMarketItems(User user) {
        Garage garage = user.getGarage();
        JsonObject json = new JsonObject();
        JsonArray jarray = new JsonArray();

        for (Item item : GarageItemsLoader.items.values()) {
            if (!garage.containsItem(item.id) && !item.specialItem) {
                JsonObject i = new JsonObject();
                JsonArray properts = new JsonArray();
                JsonArray modification = new JsonArray();
                i.addProperty("id", item.id);
                i.addProperty("name", item.name.localize(user.getLocalization()));
                i.addProperty("description", item.description.localize(user.getLocalization()));
                i.addProperty("isInventory", item.isInventory);
                i.addProperty("index", item.index);
                int value = Integer.parseInt(item.itemType.toString());
                i.addProperty("type", value);
                i.addProperty("modificationID", 0);
                i.addProperty("next_price", item.nextPrice);
                i.addProperty("next_rank", item.nextRankId);
                i.addProperty("price", item.price);
                i.addProperty("rank", item.rankId);

                if (item.properties != null) {
                    for (PropertyItem prop : item.properties) {
                        properts.add(parseProperty(prop));
                    }
                }

                if (item.modifications != null) {
                    for (ModificationInfo mod : item.modifications) {
                        JsonObject m = new JsonObject();
                        JsonArray prop = new JsonArray();
                        m.addProperty("previewId", mod.previewId);
                        m.addProperty("price", mod.price);
                        m.addProperty("rank", mod.rank);
                        if (mod.propertys != null) {
                            PropertyItem[] var19;
                            int var18 = (var19 = mod.propertys).length;

                            for (int var17 = 0; var17 < var18; ++var17) {
                                PropertyItem a = var19[var17];
                                prop.add(parseProperty(a));
                            }
                        }

                        m.add("properts", prop);
                        modification.add(m);
                    }
                }

                i.add("properts", properts);
                i.add("modification", modification);
                jarray.add(i);
            }
            json.add("items", jarray);
        }

        return GSON.toJson(json);
    }

    public static String parseItemInfo(Item item) {
        JsonObject obj = new JsonObject();
        obj.addProperty("itemId", item.id);
        obj.addProperty("count", item.count);
        return GSON.toJson(obj);
    }

    private static JsonObject parseProperty(PropertyItem item) {
        JsonObject h = new JsonObject();
        h.addProperty("property", item.property.toString());
        h.addProperty("value", item.value);
        return h;
    }

    public static String parseBattleMapList() {
        JsonObject obj = new JsonObject();
        JsonArray items = new JsonArray();
        JsonArray battles = new JsonArray();

        for (Map map : MapsLoader.maps.values()) {
            JsonObject m = new JsonObject();
            m.addProperty("id", map.id.replace(".xml", ""));
            m.addProperty("name", map.name);
            m.addProperty("gameName", "тип gameName");
            m.addProperty("maxPeople", map.maxPlayers);
            m.addProperty("maxRank", map.maxRank);
            m.addProperty("minRank", map.minRank);
            m.addProperty("themeName", map.themeId);
            m.addProperty("skyboxId", map.skyboxId);
            m.addProperty("ctf", map.ctf);
            m.addProperty("tdm", map.tdm);
            items.add(m);
        }

        obj.add("items", items);

        for (BattleInfo battle : BattlesList.getList()) {
            battles.add(parseBattleInfo(battle, 1));
        }

        obj.add("battles", battles);
        return GSON.toJson(obj);
    }

    public static String parseBattleInfo(BattleInfo battle) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", battle.battleId);
        obj.addProperty("mapId", battle.map.id);
        obj.addProperty("name", battle.name);
        obj.addProperty("previewId", battle.map.id + "_preview");
        obj.addProperty("team", battle.team);
        obj.addProperty("redPeople", battle.redPeople);
        obj.addProperty("bluePeople", battle.bluePeople);
        obj.addProperty("countPeople", battle.countPeople);
        obj.addProperty("maxPeople", battle.maxPeople);
        obj.addProperty("minRank", battle.minRank);
        obj.addProperty("maxRank", battle.maxRank);
        obj.addProperty("isPaid", battle.isPaid);
        return GSON.toJson(obj);
    }

    public static JsonObject parseBattleInfo(BattleInfo battle, int i) {
        JsonObject json = new JsonObject();
        json.addProperty("battleId", battle.battleId);
        json.addProperty("mapId", battle.map.id);
        json.addProperty("name", battle.name);
        json.addProperty("previewId", battle.map.id + "_preview");
        json.addProperty("team", battle.team);
        json.addProperty("redPeople", battle.redPeople);
        json.addProperty("bluePeople", battle.bluePeople);
        json.addProperty("countPeople", battle.countPeople);
        json.addProperty("maxPeople", battle.maxPeople);
        json.addProperty("minRank", battle.minRank);
        json.addProperty("maxRank", battle.maxRank);
        json.addProperty("isPaid", battle.isPaid);
        return json;
    }

    public static String parseBattleInfoShow(BattleInfo battle, boolean spectator) {
        JsonObject obj = new JsonObject();
        if (battle == null) {
            obj.addProperty("null_battle", true);
            return GSON.toJson(obj);
        } else {
            try {
                JsonArray users = new JsonArray();
                if (battle.model != null && battle.model.players != null) {
                    for (Object o : battle.model.players.values()) {
                        BattlefieldPlayerController player = (BattlefieldPlayerController) o;
                        JsonObject u = new JsonObject();
                        u.addProperty("nickname", player.parentLobby.getLocalUser().getNickname());
                        u.addProperty("rank", player.parentLobby.getLocalUser().getRang() + 1);
                        u.addProperty("kills", player.statistic.getKills());
                        u.addProperty("team_type", player.playerTeamType.name());
                        users.add(u);
                    }
                    for (Object o : autoEntryServices.getPlayersByBattle(battle.model)) {
                        AutoEntryServices.Data player = (AutoEntryServices.Data) o;
                        JsonObject u = new JsonObject();
                        User user = databaseManager.getUserById(player.userId);
                        u.addProperty("nickname", user.getNickname());
                        u.addProperty("rank", user.getRang() + 1);
                        u.addProperty("kills", player.statistic.getKills());
                        u.addProperty("team_type", player.teamType.name());
                        users.add(u);
                    }
                }

                obj.add("users_in_battle", users);
                obj.addProperty("name", battle.name);
                obj.addProperty("maxPeople", battle.maxPeople);
                obj.addProperty("type", battle.battleType.name());
                obj.addProperty("battleId", battle.battleId);
                obj.addProperty("minRank", battle.minRank);
                obj.addProperty("maxRank", battle.maxRank);
                obj.addProperty("timeLimit", battle.time);
                obj.addProperty("timeCurrent", battle.model.getTimeLeft());
                obj.addProperty("killsLimt", battle.numKills);
                obj.addProperty("scoreRed", battle.scoreRed);
                obj.addProperty("scoreBlue", battle.scoreBlue);
                obj.addProperty("autobalance", battle.autobalance);
                obj.addProperty("friendlyFire", battle.friendlyFire);
                obj.addProperty("paidBattle", battle.isPaid);
                obj.addProperty("withoutBonuses", true);
                obj.addProperty("userAlreadyPaid", true);
                obj.addProperty("fullCash", true);
                obj.addProperty("spectator", spectator);
                obj.addProperty("previewId", battle.map.id + "_preview");
            } catch (Exception e) {
                e.printStackTrace();
                return GSON.toJson(obj);
            }

            return GSON.toJson(obj);
        }
    }

    public static String parseBattleModelInfo(BattleInfo battle, boolean spectatorMode) {
        JsonObject obj = new JsonObject();
        obj.addProperty("kick_period_ms", 125000);
        obj.addProperty("map_id", battle.map.id.replace(".xml", ""));
        obj.addProperty("invisible_time", 3500);
        obj.addProperty("skybox_id", battle.map.skyboxId);
        obj.addProperty("spectator", spectatorMode);
        obj.addProperty("sound_id", battle.map.mapTheme.getAmbientSoundId());
        obj.addProperty("game_mode", battle.map.mapTheme.getGameModeId());
        return GSON.toJson(obj);
    }

    public static String parseTankData(BattlefieldModel player, BattlefieldPlayerController controller, Garage garageUser, Vector3 pos, boolean stateNull, int icration, String idTank, String nickname, int rank) {
        JsonObject obj = new JsonObject();
        obj.addProperty("battleId", player.battleInfo.battleId);
        obj.addProperty("colormap_id", garageUser.mountColormap.id + "_m0");
        obj.addProperty("hull_id", garageUser.mountHull.id + "_m" + garageUser.mountHull.modificationIndex);
        obj.addProperty("turret_id", garageUser.mountTurret.id + "_m" + garageUser.mountTurret.modificationIndex);
        obj.addProperty("team_type", controller.playerTeamType.name());
        if (pos == null) {
            pos = new Vector3(0.0F, 0.0F, 0.0F);
        }

        obj.addProperty("position", pos.x + "@" + pos.y + "@" + pos.z + "@" + pos.rot);
        obj.addProperty("incration", icration);
        obj.addProperty("tank_id", idTank);
        obj.addProperty("nickname", nickname);
        obj.addProperty("state", controller.tank.state);
        obj.addProperty("turn_speed", controller.tank.getHull().turnSpeed);
        obj.addProperty("speed", controller.tank.getHull().speed);
        obj.addProperty("turret_turn_speed", controller.tank.turretRotationSpeed);
        obj.addProperty("health", controller.tank.health);
        obj.addProperty("rank", rank + 1);
        obj.addProperty("mass", controller.tank.getHull().mass);
        obj.addProperty("power", controller.tank.getHull().power);
        obj.addProperty("kickback", controller.tank.getWeapon().getEntity().getShotData().kickback);
        obj.addProperty("turret_rotation_accel", controller.tank.getWeapon().getEntity().getShotData().turretRotationAccel);
        obj.addProperty("impact_force", controller.tank.getWeapon().getEntity().getShotData().impactCoeff);
        obj.addProperty("state_null", stateNull);
        return GSON.toJson(obj);
    }

    public static String parseMoveCommand(BattlefieldPlayerController player) {
        Tank tank = player.tank;
        JsonObject obj = new JsonObject();
        JsonObject pos = new JsonObject();
        JsonObject orient = new JsonObject();
        JsonObject line = new JsonObject();
        JsonObject angle = new JsonObject();
        pos.addProperty("x", tank.position.x);
        pos.addProperty("y", tank.position.y);
        pos.addProperty("z", tank.position.z);
        orient.addProperty("x", tank.orientation.x);
        orient.addProperty("y", tank.orientation.y);
        orient.addProperty("z", tank.orientation.z);
        line.addProperty("x", tank.linVel.x);
        line.addProperty("y", tank.linVel.y);
        line.addProperty("z", tank.linVel.z);
        angle.addProperty("x", tank.angVel.x);
        angle.addProperty("y", tank.angVel.y);
        angle.addProperty("z", tank.angVel.z);
        obj.add("position", pos);
        obj.add("orient", orient);
        obj.add("line", line);
        obj.add("angle", angle);
        obj.addProperty("turretDir", tank.turretDir);
        obj.addProperty("ctrlBits", tank.controllBits);
        obj.addProperty("tank_id", tank.id);
        return GSON.toJson(obj);
    }

    public static String parseBattleChatMessage(BattleChatMessage msg) {
        JsonObject obj = new JsonObject();
        obj.addProperty("nickname", msg.nickname);
        obj.addProperty("rank", msg.rank + 1);
        obj.addProperty("message", msg.message);
        obj.addProperty("team_type", msg.teamType);
        obj.addProperty("system", msg.system);
        obj.addProperty("team", msg.team);
        return GSON.toJson(obj);
    }

    public static String parseBonusInfo(Bonus bonus, int inc, int disappearingTime) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", bonus.type.toString() + "_" + inc);
        obj.addProperty("x", bonus.position.x);
        obj.addProperty("y", bonus.position.y);
        obj.addProperty("z", bonus.position.z);
        obj.addProperty("disappearing_time", disappearingTime);
        return GSON.toJson(obj);
    }

    public static JsonObject parseSpecialEntity(IEntity entity) {
        JsonObject j = new JsonObject();
        switch (entity.getType()) {
            case FLAMETHROWER:
                FlamethrowerEntity fm = (FlamethrowerEntity) entity;
                j.addProperty("cooling_speed", fm.coolingSpeed);
                j.addProperty("cone_angle", fm.coneAngle);
                j.addProperty("heating_speed", fm.heatingSpeed);
                j.addProperty("heat_limit", fm.heatLimit);
                j.addProperty("range", fm.range);
                j.addProperty("target_detection_interval", fm.targetDetectionInterval);
                break;
            case TWINS:
                TwinsEntity te = (TwinsEntity) entity;
                j.addProperty("shot_radius", te.shotRadius);
                j.addProperty("shot_range", te.shotRange);
                j.addProperty("shot_speed", te.shotSpeed);
            case RAILGUN:
            case SHAFT:
            default:
                break;
            case ISIDA:
                IsidaEntity ie = (IsidaEntity) entity;
                j.addProperty("angle", ie.maxAngle);
                j.addProperty("capacity", ie.capacity);
                j.addProperty("chargeRate", ie.chargeRate);
                j.addProperty("tickPeriod", ie.tickPeriod);
                j.addProperty("coneAngle", ie.lockAngle);
                j.addProperty("dischargeRate", ie.dischargeRate);
                j.addProperty("radius", ie.maxRadius);
                break;
            case THUNDER:
                ThunderEntity the = (ThunderEntity) entity;
                j.addProperty("impactForce", the.impactForce);
                j.addProperty("maxSplashDamageRadius", the.maxSplashDamageRadius);
                j.addProperty("minSplashDamagePercent", the.minSplashDamagePercent);
                j.addProperty("minSplashDamageRadius", the.minSplashDamageRadius);
                break;
            case FREZZE:
                FrezeeEntity frezeeEntity = (FrezeeEntity) entity;
                j.addProperty("damageAreaConeAngle", frezeeEntity.damageAreaConeAngle);
                j.addProperty("damageAreaRange", frezeeEntity.damageAreaRange);
                j.addProperty("energyCapacity", frezeeEntity.energyCapacity);
                j.addProperty("energyRechargeSpeed", frezeeEntity.energyRechargeSpeed);
                j.addProperty("energyDischargeSpeed", frezeeEntity.energyDischargeSpeed);
                j.addProperty("weaponTickMsec", frezeeEntity.weaponTickMsec);
                break;
            case RICOCHET:
                RicochetEntity ricochetEntity = (RicochetEntity) entity;
                j.addProperty("energyCapacity", ricochetEntity.energyCapacity);
                j.addProperty("energyPerShot", ricochetEntity.energyPerShot);
                j.addProperty("energyRechargeSpeed", ricochetEntity.energyRechargeSpeed);
                j.addProperty("shotDistance", ricochetEntity.shotDistance);
                j.addProperty("shotRadius", ricochetEntity.shotRadius);
                j.addProperty("shotSpeed", ricochetEntity.shotSpeed);
                break;
            case SNOWMAN:
                SnowmanEntity se = (SnowmanEntity) entity;
                j.addProperty("shot_radius", se.shotRadius);
                j.addProperty("shot_range", se.shotRange);
                j.addProperty("shot_speed", se.shotSpeed);
        }

        return j;
    }

    public static String parseWeapons(Collection<IEntity> weapons, java.util.Map<String, WeaponWeakeningData> wwds) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();

        for (IEntity entity : weapons) {
            JsonObject weapon = new JsonObject();
            WeaponWeakeningData wwd = wwds.get(entity.getShotData().id);
            weapon.addProperty("auto_aiming_down", entity.getShotData().autoAimingAngleDown);
            weapon.addProperty("auto_aiming_up", entity.getShotData().autoAimingAngleUp);
            weapon.addProperty("num_rays_down", entity.getShotData().numRaysDown);
            weapon.addProperty("num_rays_up", entity.getShotData().numRaysUp);
            weapon.addProperty("reload", entity.getShotData().reloadMsec);
            weapon.addProperty("id", entity.getShotData().id);
            if (wwd != null) {
                weapon.addProperty("max_damage_radius", wwd.maximumDamageRadius);
                weapon.addProperty("min_damage_radius", wwd.minimumDamageRadius);
                weapon.addProperty("min_damage_percent", wwd.minimumDamagePercent);
                weapon.addProperty("has_wwd", true);
            } else {
                weapon.addProperty("has_wwd", false);
            }

            weapon.add("special_entity", parseSpecialEntity(entity));
            array.add(weapon);
        }

        obj.add("weapons", array);
        return GSON.toJson(obj);
    }

    public static String parseTankSpec(Tank tank, boolean notSmooth) {
        JsonObject obj = new JsonObject();
        obj.addProperty("speed", tank.speed);
        obj.addProperty("turnSpeed", tank.turnSpeed);
        obj.addProperty("turretRotationSpeed", tank.turretRotationSpeed);
        obj.addProperty("immediate", notSmooth);
        return GSON.toJson(obj);
    }

    public static String boolToString(boolean src) {
        return src ? "true" : "false";
    }
}
