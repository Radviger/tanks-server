package gtanks.battles.mines.activator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.mines.ServerMine;
import gtanks.test.osgi.OSGi;
import gtanks.test.server.configuration.entitys.MineConfiguratorEntity;

import java.util.Timer;

public class MinesActivatorService {
    private static final int ACTIVATION_TIME = ((MineConfiguratorEntity) OSGi.getModelByInterface(MineConfiguratorEntity.class)).getActivationTimeMsec();
    private static final MinesActivatorService instance = new MinesActivatorService();
    private static final Timer TIMER = new Timer("MinesActivatorService Timer");

    private MinesActivatorService() {
    }

    public static MinesActivatorService getInstance() {
        return instance;
    }

    public void activate(BattlefieldModel model, ServerMine mine) {
        MineActivator activator = new MineActivator(model, mine);
        TIMER.schedule(activator, (long) ACTIVATION_TIME);
        activator.putMine();
    }
}
