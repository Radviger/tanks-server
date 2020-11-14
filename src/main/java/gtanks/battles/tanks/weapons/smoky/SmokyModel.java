package gtanks.battles.tanks.weapons.smoky;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;
import gtanks.logger.Logger;

public class SmokyModel extends FiringWeaponModel {
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final SmokyEntity entity;
    private final WeaponWeakeningData weakeningData;

    public SmokyModel(SmokyEntity entity, WeaponWeakeningData weakeningData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeningData = weakeningData;
    }

    @Override
    public void fire(JsonObject data) {
        if (!check(data.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
        } else {
            this.bfModel.fire(this.player, data);
            JsonElement victimId = data.get("victimId");
            if (victimId != null) {
                BattlefieldPlayerController victim = this.bfModel.players.get(victimId.getAsString());
                if (victim != null) {
                    this.onTarget(new BattlefieldPlayerController[]{victim}, data.get("distance").getAsNumber().intValue());
                }
            }
        }
    }

    @Override
    public void startFire(JsonObject data) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        if (targets.length != 0) {
            if (targets.length > 1) {
                Logger.log("SmokyModel::onTarget() Warning! targetsTanks length = " + targets.length);
            }

            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
            if ((double) distance >= this.weakeningData.minimumDamageRadius) {
                damage = WeaponUtils.calculateDamageFromDistance(damage, (int) this.weakeningData.minimumDamagePercent);
            }

            this.bfModel.tanksKillModel.damageTank(targets[0], this.player, damage, true);
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
