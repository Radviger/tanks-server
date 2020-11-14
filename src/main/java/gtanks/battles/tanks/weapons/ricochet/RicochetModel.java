package gtanks.battles.tanks.weapons.ricochet;

import com.google.gson.Gson;
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
    name = "RicochetModel",
    actionInfo = "Child FireableWeaponAnticheatModel"
)
public class RicochetModel extends FireableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private RicochetEntity entity;
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;

    public RicochetModel(RicochetEntity entity, BattlefieldModel bfModel, BattlefieldPlayerController player) {
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
            boolean selfHit = parser.get("self_hit").getAsBoolean();
            if (!selfHit) {
                this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "fire_ricochet", this.player.tank.id, json);
            }

            int distance = this.getValueByObject(parser.get("distance"));
            BattlefieldPlayerController victim = selfHit ? this.player : this.bfModel.getPlayer(parser.get("victimId").getAsString());
            if (victim != null) {
                this.onTarget(new BattlefieldPlayerController[]{victim}, distance);
            }
        }
    }

    @Override
    public void startFire(String json) {
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.tank.id, json);
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        BattlefieldPlayerController victim = targetsTanks[0];
        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min);
        this.bfModel.tanksKillModel.damageTank(victim, this.player, damage, true);
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }

    private int getValueByObject(Object obj) {
        if (obj == null) {
            return 0;
        } else {
            try {
                return (int) Double.parseDouble(String.valueOf(obj));
            } catch (Exception var3) {
                return Integer.parseInt(String.valueOf(obj));
            }
        }
    }

    @Override
    public void stopFire() {
    }
}
