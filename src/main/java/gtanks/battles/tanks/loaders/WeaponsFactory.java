package gtanks.battles.tanks.loaders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.StringUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.ShotData;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerEntity;
import gtanks.battles.tanks.weapons.flamethrower.FlamethrowerModel;
import gtanks.battles.tanks.weapons.frezee.FrezeeEntity;
import gtanks.battles.tanks.weapons.frezee.FrezeeModel;
import gtanks.battles.tanks.weapons.isida.IsidaEntity;
import gtanks.battles.tanks.weapons.isida.IsidaModel;
import gtanks.battles.tanks.weapons.railgun.RailgunEntity;
import gtanks.battles.tanks.weapons.railgun.RailgunModel;
import gtanks.battles.tanks.weapons.ricochet.RicochetEntity;
import gtanks.battles.tanks.weapons.ricochet.RicochetModel;
import gtanks.battles.tanks.weapons.shaft.ShaftEntity;
import gtanks.battles.tanks.weapons.shaft.ShaftModel;
import gtanks.battles.tanks.weapons.smoky.SmokyEntity;
import gtanks.battles.tanks.weapons.smoky.SmokyModel;
import gtanks.battles.tanks.weapons.snowman.SnowmanEntity;
import gtanks.battles.tanks.weapons.snowman.SnowmanModel;
import gtanks.battles.tanks.weapons.thunder.ThunderEntity;
import gtanks.battles.tanks.weapons.thunder.ThunderModel;
import gtanks.battles.tanks.weapons.twins.TwinsEntity;
import gtanks.battles.tanks.weapons.twins.TwinsModel;
import gtanks.json.JsonUtils;
import gtanks.logger.Logger;
import gtanks.logger.Type;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class WeaponsFactory {
    private static final Gson GSON = new Gson();
    private static final Map<String, IEntity> weapons = new HashMap<>();
    private static final Map<String, WeaponWeakeningData> wwd = new HashMap<>();
    private static String jsonListWeapons;

    public static IWeapon getWeapon(String turretId, BattlefieldPlayerController tank, BattlefieldModel battle) {
        String turret = turretId.split("_m")[0];
        switch (turret) {
            case "snowman":
                return new SnowmanModel((SnowmanEntity) getEntity(turretId), getWwd(turretId), tank, battle);
            case "ricochet":
                return new RicochetModel((RicochetEntity) getEntity(turretId), battle, tank);
            case "thunder":
                return new ThunderModel((ThunderEntity) getEntity(turretId), battle, tank);
            case "frezee":
                return new FrezeeModel((FrezeeEntity) getEntity(turretId), battle, tank);
            case "isida":
                return new IsidaModel((IsidaEntity) getEntity(turretId), tank, battle);
            case "shaft":
                return new ShaftModel((ShaftEntity) getEntity(turretId), getWwd(turretId), battle, tank);
            case "smoky":
                return new SmokyModel((SmokyEntity) getEntity(turretId), getWwd(turretId), battle, tank);
            case "twins":
                return new TwinsModel((TwinsEntity) getEntity(turretId), getWwd(turretId), tank, battle);
            case "railgun":
                return new RailgunModel((RailgunEntity) getEntity(turretId), tank, battle);
            case "flamethrower":
                return new FlamethrowerModel((FlamethrowerEntity) getEntity(turretId), battle, tank);
            default:
                return new RailgunModel((RailgunEntity) getEntity("railgun_m0"), tank, battle);
        }
    }

    public static void init(String root) {
        weapons.clear();
        Logger.log("Weapons Factory inited. Loading weapons...");

        try {
            File folder = new File(root);

            for (File config : folder.listFiles()) {
                if (!config.getName().endsWith(".json")) {
                    throw new IllegalArgumentException("In folder " + root + " find non-configuration file: " + config.getName());
                }

                Logger.log("Loading " + config.getName() + "...");
                parse(config);
            }

            jsonListWeapons = JsonUtils.parseWeapons(getEntities(), wwd);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.log(Type.ERROR, "Loading entitys weapons failed. " + e.getMessage());
        }

    }

    private static void parse(File json) throws IOException, JsonParseException {
        JsonObject obj = GSON.fromJson(new FileReader(json), JsonObject.class);
        String type = obj.get("type").getAsString();

        for (JsonElement e : obj.getAsJsonArray("params")) {
            JsonObject item = e.getAsJsonObject();
            String modification = item.get("modification").getAsString();
            String id = StringUtils.concatStrings(type, "_", modification);
            ShotData shotData = new ShotData(id, item.get("autoAimingAngleDown").getAsDouble(), item.get("autoAimingAngleUp").getAsDouble(), item.get("numRaysDown").getAsInt(), item.get("numRaysUp").getAsInt(), item.get("reloadMsec").getAsInt(), item.get("impactCoeff").getAsFloat(), item.get("kickback").getAsFloat(), item.get("turretRotationAccel").getAsFloat(), item.get("turretRotationSpeed").getAsFloat());
            IEntity entity = null;
            switch (type) {
                case "snowman": {
                    WeaponWeakeningData wwdSnowman = new WeaponWeakeningData(item.get("max_damage_radius").getAsDouble(), item.get("min_damage_percent").getAsDouble(), item.get("min_damage_radius").getAsDouble());
                    entity = new SnowmanEntity(item.get("shot_range").getAsFloat(), item.get("shot_speed").getAsFloat(), item.get("shot_radius").getAsFloat(), item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat(), item.get("frezee_speed").getAsFloat(), shotData);
                    WeaponsFactory.wwd.put(id, wwdSnowman);
                    break;
                }
                case "ricochet": {
                    WeaponWeakeningData wwdRicochet = new WeaponWeakeningData(item.get("max_damage_radius").getAsDouble(), item.get("min_damage_percent").getAsDouble(), item.get("min_damage_radius").getAsDouble());
                    entity = new RicochetEntity(item.get("shotRadius").getAsFloat(), item.get("shotSpeed").getAsFloat(), item.get("energyCapacity").getAsInt(), item.get("energyPerShot").getAsInt(), item.get("energyRechargeSpeed").getAsFloat(), item.get("shotDistance").getAsFloat(), item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat(), shotData);
                    WeaponsFactory.wwd.put(id, wwdRicochet);
                    break;
                }
                case "thunder": {
                    WeaponWeakeningData wwdThunder = new WeaponWeakeningData(item.get("maxSplashDamageRadius").getAsDouble(), item.get("minSplashDamageRadius").getAsDouble(), item.get("minSplashDamagePercent").getAsDouble());
                    entity = new ThunderEntity(item.get("maxSplashDamageRadius").getAsFloat(), item.get("minSplashDamageRadius").getAsFloat(), item.get("minSplashDamagePercent").getAsFloat(), item.get("impactForce").getAsFloat(), shotData, item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat(), wwdThunder);
                    WeaponsFactory.wwd.put(id, wwdThunder);
                    break;
                }
                case "frezee": {
                    entity = new FrezeeEntity(item.get("damageAreaConeAngle").getAsFloat(), item.get("damageAreaRange").getAsFloat(), item.get("energyCapacity").getAsInt(), item.get("energyDischargeSpeed").getAsInt(), item.get("energyRechargeSpeed").getAsInt(), item.get("weaponTickMsec").getAsInt(), item.get("coolingSpeed").getAsFloat(), item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat(), shotData);
                    break;
                }
                case "isida": {
                    entity = new IsidaEntity(item.get("capacity").getAsInt(), item.get("chargeRate").getAsInt(), item.get("dischargeRate").getAsInt(), item.get("tickPeriod").getAsInt(), item.get("lockAngle").getAsFloat(), item.get("lockAngleCos").getAsFloat(), item.get("maxAngle").getAsFloat(), item.get("maxAngleCos").getAsFloat(), item.get("maxRadius").getAsFloat(), shotData, item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat());
                    break;
                }
                case "shaft": {
                    WeaponWeakeningData shaftwwd = new WeaponWeakeningData(item.get("max_damage_radius").getAsDouble(), item.get("min_damage_percent").getAsDouble(), item.get("min_damage_radius").getAsDouble());
                    entity = new ShaftEntity(shotData, item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat());
                    WeaponsFactory.wwd.put(id, shaftwwd);
                    break;
                }
                case "smoky": {
                    WeaponWeakeningData wwd = new WeaponWeakeningData(item.get("max_damage_radius").getAsDouble(), item.get("min_damage_percent").getAsDouble(), item.get("min_damage_radius").getAsDouble());
                    entity = new SmokyEntity(shotData, item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat());
                    WeaponsFactory.wwd.put(id, wwd);
                    break;
                }
                case "twins": {
                    WeaponWeakeningData wwdTwins = new WeaponWeakeningData(item.get("max_damage_radius").getAsDouble(), item.get("min_damage_percent").getAsDouble(), item.get("min_damage_radius").getAsDouble());
                    entity = new TwinsEntity(item.get("shot_range").getAsFloat(), item.get("shot_speed").getAsFloat(), item.get("shot_radius").getAsFloat(), item.get("min_damage").getAsFloat(), item.get("max_damage").getAsFloat(), shotData);
                    WeaponsFactory.wwd.put(id, wwdTwins);
                    break;
                }
                case "railgun": {
                    entity = new RailgunEntity(shotData, item.get("chargingTime").getAsInt(), item.get("weakeningCoeff").getAsInt(), item.get("min_damage").getAsInt(), item.get("max_damage").getAsInt());
                    break;
                }
                case "flamethrower": {
                    entity = new FlamethrowerEntity(item.get("target_detection_interval").getAsInt(), item.get("range").getAsFloat(), item.get("cone_angle").getAsFloat(), item.get("heating_speed").getAsInt(), item.get("cooling_speed").getAsInt(), item.get("heat_limit").getAsInt(), shotData, item.get("max_damage").getAsFloat(), item.get("min_damage").getAsFloat());
                    break;
                }
            }
            weapons.put(id, entity);
        }
    }

    public static WeaponWeakeningData getWwd(String id) {
        return wwd.get(id);
    }

    public static IEntity getEntity(String id) {
        return weapons.get(id);
    }

    public static String getId(IEntity entity) {
        String id = null;

        for (Entry<String, IEntity> entry : weapons.entrySet()) {
            if (entry.getValue().equals(entity)) {
                id = entry.getKey();
            }
        }

        return id;
    }

    public static Collection<IEntity> getEntities() {
        return weapons.values();
    }

    public static String getJSONList() {
        return jsonListWeapons;
    }
}
