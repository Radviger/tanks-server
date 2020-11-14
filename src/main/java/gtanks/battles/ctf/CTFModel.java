package gtanks.battles.ctf;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.anticheats.CaptureTheFlagAnticheatModel;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.ctf.flags.FlagState;
import gtanks.battles.tanks.PlayerTeamType;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.battles.BattleInfo;
import gtanks.services.TanksServices;

import java.util.ArrayList;
import java.util.List;

public class CTFModel extends CaptureTheFlagAnticheatModel {
    private final BattlefieldModel bfModel;
    private final FlagServer blueFlag = new FlagServer();
    private final FlagServer redFlag = new FlagServer();
    private final TanksServices tanksServices = TanksServices.INSTANCE;

    public CTFModel(BattlefieldModel bfModel) {
        super(bfModel);
        this.bfModel = bfModel;
        this.blueFlag.flagTeamType = PlayerTeamType.BLUE;
        this.redFlag.flagTeamType = PlayerTeamType.RED;
        this.blueFlag.state = FlagState.BASE;
        this.redFlag.state = FlagState.BASE;
        this.blueFlag.position = bfModel.battleInfo.map.flagBluePosition;
        this.blueFlag.basePosition = this.blueFlag.position;
        this.redFlag.position = bfModel.battleInfo.map.flagRedPosition;
        this.redFlag.basePosition = this.redFlag.position;
    }

    public void attemptToTakeFlag(BattlefieldPlayerController taker, PlayerTeamType flagTeamType) {
        FlagServer flag = this.getTeamFlag(flagTeamType);
        if (flag.owner == null) {
            if (taker.playerTeamType == flagTeamType) {
                FlagServer enemyFlag = this.getEnemyTeamFlag(flagTeamType);
                if (flag.state == FlagState.DROPED) {
                    this.returnFlag(taker, flag);
                    return;
                }

                if (enemyFlag.owner == taker) {
                    if (this.onDeliveredFlag(taker, enemyFlag)) {
                        return;
                    }

                    this.bfModel.sendToAllPlayers(Type.BATTLE, "deliver_flag", taker.playerTeamType.name(), taker.tank.id);
                    enemyFlag.state = FlagState.BASE;
                    enemyFlag.owner = null;
                    taker.flag = null;
                    if (enemyFlag.returnTimer != null) {
                        enemyFlag.returnTimer.stop = true;
                        enemyFlag.returnTimer = null;
                    }

                    BattleInfo info = this.bfModel.battleInfo;
                    int score = (taker.playerTeamType == PlayerTeamType.BLUE ? info.redPeople : info.bluePeople) * 10;
                    this.tanksServices.addScore(taker.parentLobby, score);
                    taker.statistic.addScore(score);
                    this.bfModel.statistics.changeStatistic(taker);
                    double fund = 0.0D;
                    List<BattlefieldPlayerController> otherTeam = new ArrayList<>();

                    for (BattlefieldPlayerController otherPlayer : this.bfModel.players) {
                        if (!otherPlayer.playerTeamType.equals(taker.playerTeamType) && otherPlayer.playerTeamType != PlayerTeamType.NONE) {
                            otherTeam.add(otherPlayer);
                        }
                    }

                    for (BattlefieldPlayerController otherPlayer : otherTeam) {
                        fund += Math.sqrt((double) otherPlayer.getUser().getRang() * 0.125D);
                    }

                    this.bfModel.tanksKillModel.addFund(fund);

                    if (taker.playerTeamType == PlayerTeamType.BLUE) {
                        ++info.scoreBlue;
                        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "BLUE", String.valueOf(info.scoreBlue));
                        if (info.numFlags == info.scoreBlue) {
                            this.bfModel.tanksKillModel.restartBattle(false);
                        }
                    } else {
                        ++info.scoreRed;
                        this.bfModel.sendToAllPlayers(Type.BATTLE, "change_team_scores", "RED", String.valueOf(info.scoreRed));
                        if (info.numFlags == info.scoreRed) {
                            this.bfModel.tanksKillModel.restartBattle(false);
                        }
                    }
                }
            } else {
                if (this.onTakeFlag(taker, flag)) {
                    return;
                }

                this.bfModel.sendToAllPlayers(Type.BATTLE, "flagTaken", taker.tank.id, flagTeamType.name());
                flag.state = FlagState.TAKEN_BY;
                flag.owner = taker;
                taker.flag = flag;
                if (flag.returnTimer != null) {
                    flag.returnTimer.stop = true;
                    flag.returnTimer = null;
                }
            }

        }
    }

    public void dropFlag(BattlefieldPlayerController following, Vector3 posDrop) {
        FlagServer flag = this.getEnemyTeamFlag(following.playerTeamType);
        flag.state = FlagState.DROPED;
        flag.position = posDrop;
        flag.owner = null;
        following.flag = null;
        flag.returnTimer = new FlagReturnTimer(this, flag);
        flag.returnTimer.start();
        this.bfModel.sendToAllPlayers(Type.BATTLE, "flag_drop", JsonUtils.parseDropFlagCommand(flag));
    }

    public void returnFlag(BattlefieldPlayerController following, FlagServer flag) {
        flag.state = FlagState.BASE;
        if (flag.owner != null) {
            flag.owner.flag = null;
            flag.owner = null;
        }

        flag.position = flag.basePosition;
        if (flag.returnTimer != null) {
            flag.returnTimer.stop = true;
            flag.returnTimer = null;
        }

        String id = following == null ? null : following.tank.id;
        this.bfModel.sendToAllPlayers(Type.BATTLE, "return_flag", flag.flagTeamType.name(), id);
        int score = 5;
        if (following != null) {
            this.tanksServices.addScore(following.parentLobby, score);
            following.statistic.addScore(score);
            this.bfModel.statistics.changeStatistic(following);
        }

    }

    private FlagServer getTeamFlag(PlayerTeamType teamType) {
        switch (teamType) {
            case RED:
                return this.redFlag;
            case BLUE:
                return this.blueFlag;
            default:
                return null;
        }
    }

    private FlagServer getEnemyTeamFlag(PlayerTeamType teamType) {
        switch (teamType) {
            case RED:
                return this.blueFlag;
            case BLUE:
                return this.redFlag;
            default:
                return null;
        }
    }

    public FlagServer getRedFlag() {
        return this.redFlag;
    }

    public FlagServer getBlueFlag() {
        return this.blueFlag;
    }
}
