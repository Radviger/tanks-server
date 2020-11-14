package gtanks.battles.tanks.weapons.isida;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponAnticheatModel;
import gtanks.commands.Type;
import gtanks.services.TanksServices;

public class IsidaModel extends TickableWeaponAnticheatModel implements IWeapon {
    private static final Gson GSON = new Gson();
    private static final double HEALER_POINTS_COEFFICIENT = 90.0D;
    private static final TanksServices tanksServices = TanksServices.INSTANCE;

    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final IsidaEntity entity;
    private double accumulatedPointsForHealing = 0.0D;

    public IsidaModel(IsidaEntity entity, BattlefieldPlayerController player, BattlefieldModel bfModel) {
        super(entity.tickPeriod);
        this.bfModel = bfModel;
        this.player = player;
        this.entity = entity;
    }

    @Override
    public void startFire(String json) {
        JsonObject obj = new JsonObject();
        JsonObject parser = GSON.fromJson(json, JsonObject.class);

        String shotType = "";
        String victim = null;

        JsonElement victimId = parser.get("victimId");
        if (victimId != null) {
            victim = victimId.getAsString();
            if (!victim.isEmpty()) {
                BattlefieldPlayerController target = this.bfModel.getPlayer(victim);
                if (target != null) {
                    if (this.bfModel.battleInfo.team && this.player.playerTeamType.equals(target.playerTeamType)) {
                        shotType = "heal";
                    } else {
                        shotType = "damage";
                    }
                }
            }
        } else {
            shotType = "idle";
        }

        obj.addProperty("type", shotType);
        obj.addProperty("shooterId", this.player.getUser().getNickname());
        obj.addProperty("targetId", victim);
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.getUser().getNickname(), GSON.toJson(obj));
    }

    @Override
    public void fire(String json) {
        JsonObject parser = GSON.fromJson(json, JsonObject.class);

        this.check(parser.get("tickPeriod").getAsInt());
        JsonElement victimId = parser.get("victimId");
        if (victimId != null) {
            String victim = victimId.getAsString();
            if (!victim.isEmpty()) {
                BattlefieldPlayerController target = this.bfModel.getPlayer(victim);
                if (target != null) {
                    if (!((float) ((int) (target.tank.position.distanceTo(this.player.tank.position) / 100.0D)) > this.entity.maxRadius)) {
                        this.onTarget(new BattlefieldPlayerController[]{target}, parser.get("distance").getAsInt());
                    }
                }
            }
        }
    }

    @Override
    public void stopFire() {
        this.bfModel.stopFire(this.player);
        this.calculateHealedScore();
    }

    private void calculateHealedScore() {
        if (this.accumulatedPointsForHealing > 0.0D) {
            tanksServices.addScore(this.player.parentLobby, (int) this.accumulatedPointsForHealing);
        }

        this.player.statistic.addScore((int) this.accumulatedPointsForHealing);
        this.bfModel.statistics.changeStatistic(this.player);
        this.accumulatedPointsForHealing = 0.0D;
    }

    private void addScoreForHealing(float healedPoint, BattlefieldPlayerController patient) {
        double patientRating = patient.getUser().getRang();
        double healerRating = this.player.getUser().getRang();
        double scorePoints = Math.atan((patientRating - healerRating) / (healerRating + 1) + 1) / Math.PI * HEALER_POINTS_COEFFICIENT * (double) healedPoint / 100.0D;
        this.accumulatedPointsForHealing += scorePoints;
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
        if (distance != 1500) {
            this.bfModel.cheatDetected(this.player, this.getClass());
        }

        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0F;
        if (this.bfModel.battleInfo.team && this.player.playerTeamType.equals(targetsTanks[0].playerTeamType)) {
            if (this.bfModel.tanksKillModel.healPlayer(this.player, targetsTanks[0], damage)) {
                this.addScoreForHealing(damage, targetsTanks[0]);
            }
        } else {
            this.bfModel.tanksKillModel.damageTank(targetsTanks[0], this.player, damage, true);
            this.bfModel.tanksKillModel.healPlayer(this.player, this.player, damage / 2.0F);
        }
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}
