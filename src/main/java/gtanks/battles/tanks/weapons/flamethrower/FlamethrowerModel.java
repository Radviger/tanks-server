package gtanks.battles.tanks.weapons.flamethrower;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;

public class FlamethrowerModel extends TickableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    public BattlefieldModel bfModel;
    public BattlefieldPlayerController player;
    private FlamethrowerEntity entity;

    public FlamethrowerModel(FlamethrowerEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.targetDetectionInterval);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
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
    public void fire(String json) {
        JsonObject obj = GSON.fromJson(json, JsonObject.class);
        JsonArray tanks = obj.getAsJsonArray("targetsIds");
        if (!this.check(obj.get("tickPeriod").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
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
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        for (BattlefieldPlayerController victim : targetsTanks) {
            this.bfModel.tanksKillModel.damageTank(victim, this.player, this.entity.damage_max, true);
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}
