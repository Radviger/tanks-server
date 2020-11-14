package gtanks.battles.tanks.weapons.smoky;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.logger.Logger;

@AnticheatModel(
    name = "SmokyModel",
    actionInfo = "Child FireableWeaponAnticheatModel"
)
public class SmokyModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private SmokyEntity entity;
    private WeaponWeakeningData weakeingData;

    public SmokyModel(SmokyEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeingData = weakeingData;
    }

    @Override
    public void fire(String json) {
        JsonObject obj = GSON.fromJson(json, JsonObject.class);

        if (!this.check(obj.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        } else {
            this.bfModel.fire(this.player, json);
            JsonElement victimId = obj.get("victimId");
            if (victimId != null) {
                BattlefieldPlayerController victim = this.bfModel.players.get(victimId.getAsString());
                if (victim != null) {
                    this.onTarget(new BattlefieldPlayerController[]{victim}, obj.get("distance").getAsNumber().intValue());
                }
            }
        }
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (targetsTanks.length != 0) {
            if (targetsTanks.length > 1) {
                Logger.log("SmokyModel::onTarget() Warning! targetsTanks length = " + targetsTanks.length);
            }

            float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
            if ((double) distance >= this.weakeingData.minimumDamageRadius) {
                damage = WeaponUtils.calculateDamageFromDistance(damage, (int) this.weakeingData.minimumDamagePercent);
            }

            this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
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
