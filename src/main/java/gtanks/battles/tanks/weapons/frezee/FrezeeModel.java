package gtanks.battles.tanks.weapons.frezee;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;
import gtanks.battles.tanks.weapons.frezee.effects.FrezeeEffectModel;

public class FrezeeModel extends TickableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private final FrezeeEntity entity;
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;

    public FrezeeModel(FrezeeEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.weaponTickMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(String json) {
        JsonObject obj = GSON.fromJson(json, JsonObject.class);
        JsonArray victims = obj.getAsJsonArray("victims");
        JsonArray distances = obj.getAsJsonArray("targetDistances");

        if (!this.check(obj.get("tickPeriod").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
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
    public void startFire(String json) {
        this.bfModel.startFire(this.player);
    }

    @Override
    public void stopFire() {
        this.bfModel.stopFire(this.player);
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0F;
        if (!((float) distance > this.entity.damageAreaRange)) {
            this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
            BattlefieldPlayerController victim = targetsTanks[0];
            if (victim != null && victim.tank != null) {
                boolean canFrezee = true;
                if (this.bfModel.battleInfo.team) {
                    canFrezee = !this.player.playerTeamType.equals(victim.playerTeamType);
                }

                if (canFrezee) {
                    if (victim.tank.frezeeEffect == null) {
                        victim.tank.frezeeEffect = new FrezeeEffectModel(this.entity.coolingSpeed, victim.tank, this.bfModel);
                        victim.tank.frezeeEffect.setStartSpecFromTank();
                    }

                    victim.tank.frezeeEffect.update();
                }

            }
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}
