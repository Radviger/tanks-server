package gtanks.battles.tanks.weapons.thunder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AntiCheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.commands.Type;

@AntiCheatModel(
    name = "ThunderModel",
    actionInfo = "Child FireableWeaponAnticheatModel"
)
public class ThunderModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
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
    public void fire(String json) {
        JsonObject parser = GSON.fromJson(json, JsonObject.class);

        if (!this.check(parser.get("reloadTime").getAsInt())) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        } else {
            this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "fire", this.player.tank.id, json);
            String mainTargetId = parser.get("mainTargetId").getAsString();
            if (mainTargetId != null) {
                this.onTarget(new BattlefieldPlayerController[]{this.bfModel.getPlayer(mainTargetId)}, parser.get("distance").getAsInt());
            }

            JsonArray splashVictims = parser.getAsJsonArray("splashTargetIds");
            JsonArray splashVictimsDistances = parser.getAsJsonArray("splashTargetDistances");
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
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void stopFire() {
    }
}
