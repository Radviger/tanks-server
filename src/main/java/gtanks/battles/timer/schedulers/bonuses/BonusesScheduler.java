package gtanks.battles.timer.schedulers.bonuses;

import gtanks.battles.BattlefieldModel;
import gtanks.commands.Type;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class BonusesScheduler {
    private static final Timer TIMER = new Timer("BonusesScheduler timer");
    private static HashMap tasks = new HashMap();

    public static void runRemoveTask(BattlefieldModel bfModel, String bonusId, long disappearingTime) {
        BonusesScheduler.RemoveBonusTask rbt = new BonusesScheduler.RemoveBonusTask();
        rbt.bfModel = bfModel;
        rbt.bonusId = bonusId;
        tasks.put(bonusId, rbt);
        TIMER.schedule(rbt, disappearingTime * 1000L - 1250L);
    }

    static class RemoveBonusTask extends TimerTask {
        public String bonusId;
        public BattlefieldModel bfModel;

        @Override
        public void run() {
            if (this.bfModel != null) {
                if (this.bfModel.activeBonuses != null) {
                    this.bfModel.activeBonuses.remove(this.bonusId);
                    this.bfModel.sendToAllPlayers(Type.BATTLE, "remove_bonus", this.bonusId);
                    BonusesScheduler.tasks.remove(this.bonusId);
                }
            }
        }
    }
}
