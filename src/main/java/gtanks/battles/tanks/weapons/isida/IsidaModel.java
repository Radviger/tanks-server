package gtanks.battles.tanks.weapons.isida;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.anticheats.TickableWeaponModel;
import gtanks.commands.Type;
import gtanks.services.TanksServices;

public class IsidaModel extends TickableWeaponModel {
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
    public void startFire(JsonObject data) {
        JsonObject obj = new JsonObject();

        String shotType = "";
        String victim = null;

        JsonElement victimId = data.get("victimId");
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
        this.bfModel.sendToAllPlayers(this.player, Type.BATTLE, "start_fire", this.player.getUser().getNickname(), BattlefieldPlayerController.GSON.toJson(obj));
    }

    @Override
    public void fire(JsonObject data) {
        this.check(data.get("tickPeriod").getAsInt());
        JsonElement victimId = data.get("victimId");
        if (victimId != null) {
            String victim = victimId.getAsString();
            if (!victim.isEmpty()) {
                BattlefieldPlayerController target = this.bfModel.getPlayer(victim);
                if (target != null) {
                    if (!((float) ((int) (target.tank.position.distanceTo(this.player.tank.position) / 100.0D)) > this.entity.maxRadius)) {
                        this.onTarget(new BattlefieldPlayerController[]{target}, data.get("distance").getAsInt());
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
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
        if (distance != 1500) {
            this.bfModel.cheatDetected(this.player, this);
        }

        float damage = RandomUtils.getRandom(this.entity.damage_min, this.entity.damage_min) / 2.0F;
        if (this.bfModel.battleInfo.team && this.player.playerTeamType.equals(targets[0].playerTeamType)) {
            if (this.bfModel.tanksKillModel.healPlayer(this.player, targets[0], damage)) {
                this.addScoreForHealing(damage, targets[0]);
            }
        } else {
            this.bfModel.tanksKillModel.damageTank(targets[0], this.player, damage, true);
            this.bfModel.tanksKillModel.healPlayer(this.player, this.player, damage / 2.0F);
        }
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }
}
