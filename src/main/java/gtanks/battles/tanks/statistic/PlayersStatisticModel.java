package gtanks.battles.tanks.statistic;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;

public class PlayersStatisticModel {
    private BattlefieldModel bfModel;

    public PlayersStatisticModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void changeStatistic(BattlefieldPlayerController player) {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "update_player_statistic", JsonUtils.parsePlayerStatistic(player));
    }
}
