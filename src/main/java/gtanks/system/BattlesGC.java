package gtanks.system;

import gtanks.battles.BattlefieldModel;
import gtanks.lobby.battles.BattlesList;
import gtanks.logger.Logger;
import gtanks.services.AutoEntryServices;
import gtanks.services.LobbyServices;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BattlesGC {
    private static final long TIME_FOR_REMOVING_EMPTY_BATTLE = 20000L;
    private static final Map<BattlefieldModel, Timer> battlesForRemoval = new HashMap<>();

    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private static final AutoEntryServices autoEntryServices = AutoEntryServices.INSTANCE;

    public static void addBattleForRemove(BattlefieldModel battle) {
        if (battle != null) {
            Timer timer = new Timer("BattlesGC::Timer for battle: " + battle.battleInfo.battleId);
            timer.schedule(new BattlesGC.RemoveBattleTask(battle), TIME_FOR_REMOVING_EMPTY_BATTLE);
            battlesForRemoval.put(battle, timer);
        }
    }

    public static void cancelRemoving(BattlefieldModel model) {
        Timer timer = battlesForRemoval.get(model);
        if (timer != null) {
            timer.cancel();
            battlesForRemoval.remove(model);
        }
    }

    private static void removeEmptyBattle(BattlefieldModel battle) {
        Logger.log("[BattlesGarbageCollector]: battle[" + battle.battleInfo + "] has been deleted by inactivity.");
        BattlesList.removeBattle(battle.battleInfo);
        autoEntryServices.battleDisposed(battle);
    }

    static class RemoveBattleTask extends TimerTask {
        private final BattlefieldModel battle;

        public RemoveBattleTask(BattlefieldModel battle) {
            this.battle = battle;
        }

        @Override
        public void run() {
            if (this.battle != null) {
                BattlesGC.removeEmptyBattle(this.battle);
            }
        }
    }
}
