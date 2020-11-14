package gtanks.battles.tanks.weapons.snowman;

import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;
import gtanks.battles.tanks.weapons.frezee.effects.FreezeEffectModel;
import gtanks.commands.Type;

public class SnowmanModel extends FiringWeaponModel {
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final WeaponWeakeningData weakeningData;
    private final SnowmanEntity entity;

    public SnowmanModel(SnowmanEntity snowmanEntity, WeaponWeakeningData weakeningData, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(snowmanEntity.getShotData().reloadMsec);
        this.bfModel = battle;
        this.player = tank;
        this.entity = snowmanEntity;
        this.weakeningData = weakeningData;
    }

    @Override
    public void startFire(JsonObject data) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire_snowman", this.player.tank.id, BattlefieldPlayerController.GSON.toJson(data));
    }

    @Override
    public void fire(JsonObject data) {
        this.bfModel.fire(this.player, data);

        if (!check(data.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
            return;
        }

        BattlefieldPlayerController victim = this.bfModel.getPlayer(data.get("victimId").getAsString());
        this.onTarget(new BattlefieldPlayerController[]{victim}, data.get("distance").getAsInt());
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        if (targets.length != 0) {
            if (targets[0] != null) {
                if ((double) distance >= this.weakeningData.minimumDamageRadius) {
                    damage = WeaponUtils.calculateDamageFromDistance(damage, (int) this.weakeningData.minimumDamagePercent);
                }

                if (targets[0].tank.freezeEffect == null) {
                    targets[0].tank.freezeEffect = new FreezeEffectModel(this.entity.frezeeSpeed, targets[0].tank, this.bfModel);
                    targets[0].tank.freezeEffect.setStartSpecFromTank();
                }

                targets[0].tank.freezeEffect.update();
                this.bfModel.tanksKillModel.damageTank(targets[0], this.player, damage, true);
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
