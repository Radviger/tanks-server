package gtanks.battles;

import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.Tank;
import gtanks.battles.tanks.data.DamageTankData;
import gtanks.battles.tanks.statistic.prizes.BattlePrizeCalculate;
import gtanks.battles.tanks.weapons.WeaponUtils;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattleType;
import gtanks.services.TanksServices;
import gtanks.system.destroy.Destroyable;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TankKillModel implements Destroyable {
    private static final String QUARTZ_GROUP = TankKillModel.class.getName();
    private static final int EXPERIENCE_FOR_KILL = 10;
    private static final int TIME_TO_RESTART_BATTLE = 10000;
    private final BattlefieldModel bfModel;
    private final TanksServices tanksServices = TanksServices.INSTANCE;
    private final QuartzService quartzService = QuartzServiceImpl.INSTANCE;
    private final BattleInfo battleInfo;
    private double battleFund;
    private final String quartzName;

    public TankKillModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        this.battleInfo = bfModel.battleInfo;
        this.quartzName = "TankKillModel " + this.hashCode() + " battle=" + this.battleInfo.battleId;
    }

    public synchronized void damageTank(BattlefieldPlayerController controller, BattlefieldPlayerController damager, float damage, boolean considerDD) {
        if (controller != null && damager != null) {
            Tank tank = controller.tank;
            if (!tank.state.equals("newcome") && !tank.state.equals("suicide")) {
                if (!this.bfModel.battleInfo.team || controller == damager || !controller.playerTeamType.equals(damager.playerTeamType) || this.bfModel.battleInfo.friendlyFire) {
                    Integer resistance = controller.tank.getColormap().getResistance(damager.tank.getWeapon().getEntity().getType());
                    damage = WeaponUtils.calculateDamageWithResistance(damage, resistance == null ? 0 : resistance);
                    if (tank.isUsedEffect(EffectType.ARMOR)) {
                        damage /= 2.0F;
                    }

                    if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
                        damage *= 2.0F;
                    }

                    DamageTankData lastDamage = tank.lastDamagers.get(damager);
                    DamageTankData damageData = new DamageTankData();
                    damageData.damage = damage;
                    damageData.timeDamage = System.currentTimeMillis();
                    damageData.damager = damager;
                    if (lastDamage != null) {
                        damageData.damage += lastDamage.damage;
                    }

                    if (damager.tank.isUsedEffect(EffectType.DAMAGE) && considerDD) {
                        damageData.damage /= 2.0F;
                    }

                    if (controller != damager) {
                        tank.lastDamagers.put(damager, damageData);
                    }

                    tank.health -= WeaponUtils.calculateHealth(tank, damage);
                    this.changeHealth(tank, tank.health);
                    if (tank.health <= 0) {
                        tank.health = 0;
                        this.killTank(controller, damager);
                    }

                }
            }
        }
    }

    public boolean healPlayer(BattlefieldPlayerController healer, BattlefieldPlayerController target, float addHeal) {
        Tank targetTank = target.tank;
        if (!targetTank.state.equals("newcome") && !targetTank.state.equals("suicide")) {
            if (targetTank.health >= TIME_TO_RESTART_BATTLE) {
                return false;
            } else {
                targetTank.health += WeaponUtils.calculateHealth(targetTank, addHeal);
                if (targetTank.health >= TIME_TO_RESTART_BATTLE) {
                    targetTank.health = TIME_TO_RESTART_BATTLE;
                }

                this.changeHealth(targetTank, targetTank.health);
                return true;
            }
        } else {
            return false;
        }
    }

    public void changeHealth(Tank tank, int value) {
        if (tank != null) {
            tank.health = value;
            this.bfModel.sendToAllPlayers(Type.BATTLE, "change_health", tank.id, String.valueOf(tank.health));
        }

    }

    public synchronized void killTank(BattlefieldPlayerController controller, BattlefieldPlayerController killer) {
        Tank tank = controller.tank;
        tank.state = "suicide";
        tank.getWeapon().stopFire();
        controller.clearEffects();
        controller.send(Type.BATTLE, "local_user_killed");
        if (killer == null) {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "suicide");
        } else {
            this.bfModel.sendToAllPlayers(Type.BATTLE, "kill_tank", tank.id, "killed", killer.tank.id);
            if (controller == killer) {
                controller.statistic.addDeaths();
                this.bfModel.statistics.changeStatistic(controller);
            } else {
                if (this.bfModel.battleInfo.team) {
                    if (controller.playerTeamType.equals(killer.playerTeamType)) {
                        if (this.bfModel.battleInfo.friendlyFire) {
                            controller.statistic.addDeaths();
                            this.bfModel.statistics.changeStatistic(controller);
                        }
                    } else {
                        killer.statistic.addKills(false);
                        controller.statistic.addDeaths();
                        switch (killer.playerTeamType) {
                            case BLUE:
                                if (this.battleInfo.battleType != BattleType.CTF) {
                                    ++this.bfModel.battleInfo.scoreBlue;
                                }

                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", killer.playerTeamType.name(), String.valueOf(this.bfModel.battleInfo.scoreBlue));
                                break;
                            case RED:
                                if (this.battleInfo.battleType != BattleType.CTF) {
                                    ++this.bfModel.battleInfo.scoreRed;
                                }

                                this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", killer.playerTeamType.name(), String.valueOf(this.bfModel.battleInfo.scoreRed));
                                break;
                        }

                        this.bfModel.statistics.changeStatistic(killer);
                        this.bfModel.statistics.changeStatistic(controller);
                    }
                } else {
                    killer.statistic.addKills(true);
                    controller.statistic.addDeaths();
                    this.bfModel.statistics.changeStatistic(killer);
                    this.bfModel.statistics.changeStatistic(controller);
                }

                if (this.bfModel.battleInfo.team) {
                    Map<BattlefieldPlayerController, DamageTankData> lastDamagers = controller.tank.lastDamagers;
                    if (lastDamagers.size() <= 1) {
                        this.tanksServices.addScore(killer.parentLobby, EXPERIENCE_FOR_KILL);
                        if (this.bfModel.battleInfo.team) {
                            killer.statistic.addScore(EXPERIENCE_FOR_KILL);
                            this.bfModel.statistics.changeStatistic(killer);
                        }
                    } else {
                        DamageTankData damager1 = lastDamagers.get(lastDamagers.keySet().toArray()[lastDamagers.size() - 1]);
                        DamageTankData damager2 = lastDamagers.get(lastDamagers.keySet().toArray()[lastDamagers.size() - 2]);
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - damager1.timeDamage <= TIME_TO_RESTART_BATTLE && currentTime - damager2.timeDamage <= TIME_TO_RESTART_BATTLE) {
                            int score1;
                            int score2;
                            if (damager1.damage > damager2.damage) {
                                score1 = (int) (0.15D * (double) (100.0F / (controller.tank.getHull().hp / damager1.damage)));
                                score2 = 15 - score1;
                            } else if (damager2.damage > damager1.damage) {
                                score2 = (int) (0.15D * (double) (100.0F / (controller.tank.getHull().hp / damager2.damage)));
                                score1 = 15 - score2;
                            } else {
                                score1 = 7;
                                score2 = 7;
                            }

                            score1 = Math.abs(score1);
                            score2 = Math.abs(score2);
                            if (this.bfModel.battleInfo.team) {
                                damager1.damager.statistic.addScore(score1);
                                damager2.damager.statistic.addScore(score2);
                                this.bfModel.statistics.changeStatistic(damager1.damager);
                                this.bfModel.statistics.changeStatistic(damager2.damager);
                            }

                            this.tanksServices.addScore(damager1.damager.parentLobby, score1);
                            this.tanksServices.addScore(damager2.damager.parentLobby, score2);
                        } else {
                            killer.statistic.addScore(EXPERIENCE_FOR_KILL);
                            this.bfModel.statistics.changeStatistic(killer);
                            this.tanksServices.addScore(killer.parentLobby, EXPERIENCE_FOR_KILL);
                        }
                    }
                } else {
                    int killScore = EXPERIENCE_FOR_KILL;
                    if (controller.flag != null) {
                        killScore *= 2;
                    }

                    this.tanksServices.addScore(killer.parentLobby, killScore);
                }

                this.addFund(0.037D * (double) (killer.getUser().getRang() + 1) + 0.01D);
            }

            if (this.battleInfo.numKills > 0 && killer.statistic.getKills() >= (long) this.battleInfo.numKills) {
                this.restartBattle(false);
            }

            if (controller.flag != null) {
                this.bfModel.ctfModel.dropFlag(controller, controller.tank.position);
            }
        }

        this.bfModel.statistics.changeStatistic(controller);
        this.bfModel.respawnPlayer(controller, false);
        controller.tank.lastDamagers.clear();
    }

    public void restartBattle(boolean timeLimitFinish) {
        if (!timeLimitFinish && this.battleInfo.time > 0) {
            this.quartzService.deleteJob(this.bfModel.QUARTZ_NAME, BattlefieldModel.QUARTZ_GROUP);
        }

        this.calculatePrizes();
        this.bfModel.battleFinish();
        this.bfModel.sendToAllPlayers(Type.BATTLE, "battle_finish", JsonUtils.parseFishishBattle(this.bfModel.players, TIME_TO_RESTART_BATTLE));
        this.quartzService.addJob(this.quartzName, QUARTZ_GROUP, (e) -> {
            this.bfModel.battleRestart();
        }, TimeType.MS, TIME_TO_RESTART_BATTLE);
    }

    private void calculatePrizes() {
        if (this.bfModel != null && this.bfModel.players != null && this.bfModel.players.size() > 0) {
            List<BattlefieldPlayerController> users = new ArrayList<>(this.bfModel.players.values());
            if (!this.bfModel.battleInfo.team) {
                Collections.sort(users);
                BattlePrizeCalculate.calc(users, (int) this.battleFund);
            } else {
                List<BattlefieldPlayerController> redTeam = new ArrayList<>();
                List<BattlefieldPlayerController> blueTeam = new ArrayList<>();

                for (BattlefieldPlayerController user : users) {
                    switch (user.playerTeamType) {
                        case RED:
                            redTeam.add(user);
                            break;
                        case BLUE:
                            blueTeam.add(user);
                            break;
                    }
                }

                BattlePrizeCalculate.calculateForTeam(redTeam, blueTeam, this.bfModel.battleInfo.scoreRed, this.bfModel.battleInfo.scoreBlue, 0.25D, (int) this.battleFund);
            }

        }
    }

    public void addFund(double value) {
        this.battleFund += value;
        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_fund", String.valueOf((int) this.battleFund));
        this.bfModel.bonusesSpawnService.updatedFund();
    }

    public double getBattleFund() {
        return this.battleFund;
    }

    public void setBattleFund(int value) {
        this.battleFund = value;
    }

    @Override
    public void destroy() {
        this.quartzService.deleteJob(this.quartzName, QUARTZ_GROUP);
    }
}
