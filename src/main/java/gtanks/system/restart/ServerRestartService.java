package gtanks.system.restart;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.timer.schedulers.runtime.TankRespawnScheduler;
import gtanks.commands.Type;
import gtanks.lobby.battles.BattleInfo;
import gtanks.lobby.battles.BattlesList;
import gtanks.logger.Logger;
import gtanks.main.netty.NettyService;
import gtanks.services.LobbyServices;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzServiceImpl;
import gtanks.users.locations.UserLocation;

public enum ServerRestartService {
    INSTANCE;

    private static final LobbyServices lobbyServices = LobbyServices.INSTANCE;
    private static final QuartzService quartzService = QuartzServiceImpl.INSTANCE;
    private static final NettyService nettyServices = NettyService.INSTANCE;

    public void restart() {
        lobbyServices.sendCommandToAllUsers(Type.LOBBY, UserLocation.ALL, "server_halt");
        quartzService.addJob("ServerRestartJob", "Systems Jobs", (e) -> {
            TankRespawnScheduler.dispose();

            for (BattleInfo battle : BattlesList.getList()) {
                BattlefieldModel model = battle.model;
                if (model != null) {
                    model.tanksKillModel.restartBattle(false);
                }
            }

            quartzService.addJob("ServerRestartJob: Destroy", "Systems Jobs", (e_) -> {
                nettyServices.destroy();
                Logger.log("Server can be shutdowning!");
                this.disableSystemOutput();
            }, TimeType.SEC, 10L);
        }, TimeType.SEC, 40L);
    }

    private void disableSystemOutput() {
        System.setOut(new OmittingPrintStream());
    }

}
