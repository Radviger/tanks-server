package gtanks.battles.tanks.weapons.flamethrower;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponModel;

public class FlamethrowerModel extends TickableWeaponModel {
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final FlamethrowerEntity entity;

    public FlamethrowerModel(FlamethrowerEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.targetDetectionInterval);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
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
    public void fire(JsonObject data) {
        JsonArray tanks = data.getAsJsonArray("targetsIds");
        if (!check(data.get("tickPeriod").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
            return;
        }

        if (tanks.size() == 0) {
            return;
        }

        BattlefieldPlayerController[] targetVictim = new BattlefieldPlayerController[tanks.size()];

        for (int i = 0; i < tanks.size(); ++i) {
            BattlefieldPlayerController target = this.bfModel.getPlayer(tanks.get(i).getAsString());
            if (target != null && !((float) ((int) (target.tank.position.distanceTo(this.player.tank.position) / 100.0D)) > this.entity.range)) {
                targetVictim[i] = target;
            }
        }

        this.onTarget(targetVictim, 0);
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        for (BattlefieldPlayerController victim : targets) {
            this.bfModel.tanksKillModel.damageTank(victim, this.player, this.entity.damage_max, true);
        }
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }
}
