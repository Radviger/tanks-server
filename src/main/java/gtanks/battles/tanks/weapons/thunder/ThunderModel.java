package gtanks.battles.tanks.weapons.thunder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;
import gtanks.commands.Type;

public class ThunderModel extends FiringWeaponModel {
    private final ThunderEntity entity;
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;

    public ThunderModel(ThunderEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(JsonObject data) {
        if (!this.check(data.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
        } else {
            this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "fire", this.player.tank.id, BattlefieldPlayerController.GSON.toJson(data));
            String mainTargetId = data.get("mainTargetId").getAsString();
            if (mainTargetId != null) {
                this.onTarget(new BattlefieldPlayerController[]{this.bfModel.getPlayer(mainTargetId)}, data.get("distance").getAsInt());
            }

            JsonArray splashVictims = data.getAsJsonArray("splashTargetIds");
            JsonArray splashVictimsDistances = data.getAsJsonArray("splashTargetDistances");
            if (splashVictims != null && splashVictims.size() > 0) {
                for (int i = 0; i < splashVictims.size(); ++i) {
                    String victimId = splashVictims.get(i).getAsString();
                    float distance = splashVictimsDistances.get(i).getAsFloat();
                    float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min);
                    if (distance >= this.entity.minSplashDamageRadius && damage <= this.entity.maxSplashDamageRadius) {
                        damage -= damage / 100.0F * 25.0F;
                    }

                    if (distance <= this.entity.maxSplashDamageRadius) {
                        this.bfModel.tanksKillModel.damageTank(this.bfModel.getPlayer(victimId), this.player, damage, true);
                    }
                }

            }
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        this.bfModel.tanksKillModel.damageTank(targets[0], this.player, damage, true);
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }

    @Override
    public void startFire(JsonObject data) {
    }

    @Override
    public void stopFire() {
    }
}
