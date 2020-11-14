package gtanks.battles.tanks.weapons.ricochet;

import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;
import gtanks.commands.Type;

public class RicochetModel extends FiringWeaponModel {
    private final RicochetEntity entity;
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;

    public RicochetModel(RicochetEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        super(entity.getShotData().reloadMsec);
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
    }

    @Override
    public void fire(JsonObject data) {
        if (!check(data.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
        } else {
            boolean selfHit = data.get("self_hit").getAsBoolean();
            if (!selfHit) {
                this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "fire_ricochet", this.player.tank.id, BattlefieldPlayerController.GSON.toJson(data));
            }

            int distance = data.get("distance").getAsInt();
            BattlefieldPlayerController victim = selfHit ? this.player : this.bfModel.getPlayer(data.get("victimId").getAsString());
            if (victim != null) {
                this.onTarget(new BattlefieldPlayerController[]{victim}, distance);
            }
        }
    }

    @Override
    public void startFire(JsonObject data) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.tank.id, BattlefieldPlayerController.GSON.toJson(data));
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        BattlefieldPlayerController victim = targets[0];
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min);
        this.bfModel.tanksKillModel.damageTank(victim, this.player, damage, true);
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }

    @Override
    public void stopFire() {
    }
}
