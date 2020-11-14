package gtanks.battles.ctf.anticheats;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.ctf.flags.FlagServer;
import gtanks.battles.ctf.flags.FlagState;

import java.util.HashMap;
import java.util.Map;

public class CaptureTheFlagAnticheatModel {
    private static final long MIN_TIME_DELIVERED = 4000L;
    private final Map<BattlefieldPlayerController, Data> data = new HashMap<>();
    private final BattlefieldModel bfModel;

    public CaptureTheFlagAnticheatModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public boolean onTakeFlag(BattlefieldPlayerController taker, FlagServer flag) {
        Data data = this.data.get(taker);
        if (data == null) {
            this.data.put(taker, data = new Data());
        }

        data.lastTimeTakeFlag = System.currentTimeMillis();
        data.prevState = flag.state;
        return false;
    }

    public boolean onDeliveredFlag(BattlefieldPlayerController taker, FlagServer flag) {
        Data data = this.data.get(taker);
        long time = System.currentTimeMillis() - data.lastTimeTakeFlag;
        if (time <= MIN_TIME_DELIVERED && data.prevState == FlagState.BASE) {
            this.bfModel.cheatDetected(taker, this);
            System.out.println(time);
            return true;
        } else {
            return false;
        }
    }

    static class Data {
        long lastTimeTakeFlag;
        long lastTimeDeliveredFlag;
        FlagState prevState;
    }
}
