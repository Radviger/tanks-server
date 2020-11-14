package gtanks.battles.tanks.weapons.railgun;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;

@AnticheatModel(
    name = "RailgunModel",
    actionInfo = "Child FireableWeaponAnticheatModel"
)
public class RailgunModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private RailgunEntity entity;
    private BattlefieldModel battle;
    private BattlefieldPlayerController tank;

    public RailgunModel(RailgunEntity entity, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.battle = battle;
        this.tank = tank;
    }

    @Override
    public void startFire(String json) {
        this.battle.startFire(this.tank);
    }

    @Override
    public void fire(String json) {
        this.battle.fire(this.tank, json);

        try {
            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            JsonArray tanks = obj.getAsJsonArray("targets");
            if (!this.check(obj.get("reloadTime").getAsInt())) {
                this.battle.cheatDetected(this.tank, this.getClass());
                return;
            }

            if (tanks == null) {
                return;
            }

            BattlefieldPlayerController[] tanks_array = new BattlefieldPlayerController[tanks.size()];

            for (int i = 0; i < tanks.size(); ++i) {
                tanks_array[i] = this.battle.players.get(tanks.get(i).getAsString());
            }

            this.onTarget(tanks_array, 0);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length != 0) {
            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);

            for (BattlefieldPlayerController targetsTank : targetsTanks) {
                this.battle.tanksKillModel.damageTank(targetsTank, this.tank, damage, true);
                damage /= 2.0F;
            }
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }
}
