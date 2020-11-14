package gtanks.battles.timer;

import gtanks.battles.BattlefieldModel;
import gtanks.logger.remote.RemoteDatabaseLogger;

import java.util.concurrent.TimeUnit;

/**
 * @deprecated
 */
@Deprecated
public class BattlefieldTImer extends Thread {
    public long currSecond;
    private long seconds;
    private boolean interputed = false;
    private BattlefieldModel model;

    public BattlefieldTImer(BattlefieldModel model) {
        if (model != null) {
            this.model = model;
            this.seconds = TimeUnit.SECONDS.toMillis((long) model.battleInfo.time);
            this.setName("[BattlefieldTImer]" + model.battleInfo);
            if (model.battleInfo.time >= 0) {
                this.start();
            }

        }
    }

    public void interpud() {
        this.interputed = true;
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (this.currSecond != this.seconds && !this.interputed) {
                    sleep(1000L);
                    this.currSecond += 1000L;
                    continue;
                }

                if (this.model != null && !this.interputed) {
                    this.model.tanksKillModel.restartBattle(true);
                }
            } catch (Exception var2) {
                RemoteDatabaseLogger.error(var2);
            }

            return;
        }
    }
}
