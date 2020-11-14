package gtanks.battles.tanks.weapons.frezee;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponModel;
import gtanks.battles.tanks.weapons.frezee.effects.FreezeEffectModel;

public class FreezeModel extends TickableWeaponModel {
    private final FrezeeEntity entity;
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;

    public FreezeModel(FrezeeEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.weaponTickMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(JsonObject data) {
        JsonArray victims = data.getAsJsonArray("victims");
        JsonArray distances = data.getAsJsonArray("targetDistances");

        if (!this.check(data.get("tickPeriod").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
            return;
        }

        for (int i = 0; i < victims.size(); ++i) {
            String victimId = victims.get(i).getAsString();
            int distance = distances.get(i).getAsNumber().intValue();
            BattlefieldPlayerController victim = this.bfModel.getPlayer(victimId);
            if (victim != null && !((float) ((int) (victim.tank.position.distanceTo(this.player.tank.position) / 100.0D)) > this.entity.damageAreaRange)) {
                this.onTarget(new BattlefieldPlayerController[]{this.bfModel.getPlayer(victimId)}, distance);
            }
        }
    }

    @Override
    public void startFire(JsonObject data) {
        this.bfModel.startFire(this.player);
    }

    @Override
    public void stopFire() {
        this.bfModel.stopFire(this.player);
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0F;
        if (!((float) distance > this.entity.damageAreaRange)) {
            this.bfModel.tanksKillModel.damageTank(targets[0], this.player, damage, true);
            BattlefieldPlayerController victim = targets[0];
            if (victim != null && victim.tank != null) {
                boolean canFreeze = true;
                if (this.bfModel.battleInfo.team) {
                    canFreeze = !this.player.playerTeamType.equals(victim.playerTeamType);
                }

                if (canFreeze) {
                    if (victim.tank.freezeEffect == null) {
                        victim.tank.freezeEffect = new FreezeEffectModel(this.entity.coolingSpeed, victim.tank, this.bfModel);
                        victim.tank.freezeEffect.setStartSpecFromTank();
                    }

                    victim.tank.freezeEffect.update();
                }
            }
        }
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }
}
