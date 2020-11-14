package gtanks.battles.tanks.weapons.twins;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.anticheats.AnticheatModel;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;
import gtanks.battles.tanks.weapons.anticheats.FireableWeaponAnticheatModel;
import gtanks.commands.Type;

@AnticheatModel(
    name = "TwinsModel",
    actionInfo = "Child FireableWeaponAnticheatModel"
)
public class TwinsModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final WeaponWeakeningData weakeingData;
    private final TwinsEntity entity;

    public TwinsModel(TwinsEntity twinsEntity, WeaponWeakeningData weakeingData, BattlefieldPlayerController tank, BattlefieldModel battle) {
        super(twinsEntity.getShotData().reloadMsec);
        this.bfModel = battle;
        this.player = tank;
        this.entity = twinsEntity;
        this.weakeingData = weakeingData;
    }

    @Override
    public void startFire(String json) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire_twins", this.player.tank.id, json);
    }

    @Override
    public void fire(String json) {
        this.bfModel.fire(this.player, json);

        try {
            JsonObject parser = GSON.fromJson(json, JsonObject.class);
            if (!this.check(parser.get("reloadTime").getAsInt())) {
                this.bfModel.cheatDetected(this.player, this.getClass());
                return;
            }

            BattlefieldPlayerController victim = this.bfModel.getPlayer(parser.get("victimId").getAsString());
            this.onTarget(new BattlefieldPlayerController[]{victim}, parser.get("distance").getAsInt());
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_max);
        if (targetsTanks.length != 0) {
            if (targetsTanks[0] != null) {
                if ((double) distance >= this.weakeingData.minimumDamageRadius) {
                    damage = WeaponUtils.calculateDamageFromDistance(damage, (int) this.weakeingData.minimumDamagePercent);
                }

                this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
            }
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
