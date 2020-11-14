package gtanks.battles.tanks.weapons.railgun;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;

public class RailgunModel extends FiringWeaponModel {
    private final RailgunEntity entity;
    private final BattlefieldModel battle;
    private final BattlefieldPlayerController tank;

    public RailgunModel(RailgunEntity entity, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.battle = battle;
        this.tank = tank;
    }

    @Override
    public void startFire(JsonObject data) {
        this.battle.startFire(this.tank);
    }

    @Override
    public void fire(JsonObject data) {
        this.battle.fire(this.tank, data);

        JsonArray tanks = data.getAsJsonArray("targets");
        if (!check(data.get("reloadTime").getAsInt())) {
            this.battle.cheatDetected(this.tank, this);
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
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        if (targets.length != 0) {
            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);

            for (BattlefieldPlayerController targetsTank : targets) {
                this.battle.tanksKillModel.damageTank(targetsTank, this.tank, damage, true);
                damage /= 2.0F;
            }
        }
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }
}
