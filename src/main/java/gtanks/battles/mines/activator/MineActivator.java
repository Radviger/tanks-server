package gtanks.battles.mines.activator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.mines.ServerMine;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;

import java.util.TimerTask;

public class MineActivator extends TimerTask {
    private static final String PUT_MINE_COMMAND = "put_mine";
    private static final String ACTIVATE_MINE_COMMAND = "activate_mine";
    private BattlefieldModel bfModel;
    private ServerMine mine;

    public MineActivator(BattlefieldModel bfModel, ServerMine mine) {
        this.bfModel = bfModel;
        this.mine = mine;
    }

    public void putMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "put_mine", JsonUtils.parsePutMineComand(this.mine));
    }

    public void activateMine() {
        this.bfModel.sendToAllPlayers(Type.BATTLE, "activate_mine", this.mine.getId());
    }

    @Override
    public void run() {
        this.activateMine();
    }
}
