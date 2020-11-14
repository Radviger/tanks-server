package gtanks.battles.tanks.weapons.twins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FiringWeaponModel;
import gtanks.commands.Type;

public class TwinsModel extends FiringWeaponModel {
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final WeaponWeakeningData weakeningData;
    private final TwinsEntity entity;

    public TwinsModel(TwinsEntity twinsEntity, WeaponWeakeningData weakeningData, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(twinsEntity.getShotData().reloadMsec);
        this.bfModel = battle;
        this.player = tank;
        this.entity = twinsEntity;
        this.weakeningData = weakeningData;
    }

    @Override
    public void startFire(JsonObject data) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire_twins", this.player.tank.id, BattlefieldPlayerController.GSON.toJson(data));
    }

    @Override
    public void fire(JsonObject data) {
        this.bfModel.fire(this.player, data);

        if (!this.check(data.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this);
            return;
        }

        JsonElement victimId = data.get("victimId");
        if (victimId != null && !victimId.isJsonNull()) {
            BattlefieldPlayerController victim = this.bfModel.getPlayer(victimId.getAsString());
            if (victim != null) {
                onTarget(new BattlefieldPlayerController[]{victim}, data.get("distance").getAsInt());
            }
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        if (targets.length != 0) {
            if (targets[0] != null) {
                if ((double) distance >= this.weakeningData.minimumDamageRadius) {
                    damage = WeaponUtils.calculateDamageFromDistance(damage, (int) this.weakeningData.minimumDamagePercent);
                }

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
