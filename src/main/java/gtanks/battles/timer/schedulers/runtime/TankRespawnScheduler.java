package gtanks.battles.timer.schedulers.runtime;

import gtanks.StringUtils;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.managers.SpawnManager;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.logger.remote.RemoteDatabaseLogger;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class TankRespawnScheduler {
    private static final Timer TIMER = new Timer("TankRespawnScheduler timer");
    private static final long TIME_TO_PREPARE_SPAWN = 3000L;
    private static final long TIME_TO_SPAWN = 5000L;
    private static HashMap tasks = new HashMap();
    private static boolean disposed;

    public static void startRespawn(BattlefieldPlayerController player, boolean onlySpawn) {
        if (!disposed) {
            try {
                if (player == null) {
                    return;
                }

                if (player.battle == null) {
                    return;
                }

                TankRespawnScheduler.PrepareToSpawnTask task = new TankRespawnScheduler.PrepareToSpawnTask();
                task.player = player;
                task.onlySpawn = onlySpawn;
                tasks.put(player, task);
                TIMER.schedule(task, onlySpawn ? 1L : 3000L);
            } catch (Exception var3) {
                var3.printStackTrace();
                RemoteDatabaseLogger.error(var3);
            }

        }
    }

    public static void dispose() {
        disposed = true;
    }

    public static void cancelRespawn(BattlefieldPlayerController player) {
        try {
            TankRespawnScheduler.PrepareToSpawnTask task = (TankRespawnScheduler.PrepareToSpawnTask) tasks.get(player);
            if (task == null) {
                return;
            }

            if (task.spawnTask == null) {
                task.cancel();
            } else {
                task.spawnTask.cancel();
            }

            tasks.remove(player);
        } catch (Exception var2) {
            var2.printStackTrace();
            RemoteDatabaseLogger.error(var2);
        }

    }

    static class PrepareToSpawnTask extends TimerTask {
        public TankRespawnScheduler.SpawnTask spawnTask;
        public BattlefieldPlayerController player;
        public Vector3 preparedPosition;
        public boolean onlySpawn;

        @Override
        public void run() {
            try {
                if (this.player == null) {
                    return;
                }

                if (this.player.tank == null) {
                    return;
                }

                if (this.player.battle == null) {
                    return;
                }

                this.preparedPosition = SpawnManager.getSpawnState(this.player.battle.battleInfo.map, this.player.playerTeamType);
                if (this.onlySpawn) {
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                } else {
                    if (this.player.battle == null) {
                        return;
                    }

                    this.player.tank.position = this.preparedPosition;
                    this.player.send(Type.BATTLE, "prepare_to_spawn", StringUtils.concatStrings(this.player.tank.id, ";", String.valueOf(this.preparedPosition.x), "@", String.valueOf(this.preparedPosition.y), "@", String.valueOf(this.preparedPosition.z), "@", String.valueOf(this.preparedPosition.rot)));
                }

                this.spawnTask = new TankRespawnScheduler.SpawnTask();
                this.spawnTask.preparedSpawnTask = this;
                TankRespawnScheduler.TIMER.schedule(this.spawnTask, 5000L);
            } catch (Exception var2) {
                var2.printStackTrace();
                RemoteDatabaseLogger.error(var2);
            }

        }
    }

    static class SpawnTask extends TimerTask {
        TankRespawnScheduler.PrepareToSpawnTask preparedSpawnTask;

        @Override
        public void run() {
            try {
                BattlefieldPlayerController player = this.preparedSpawnTask.player;
                if (player == null) {
                    return;
                }

                if (player.tank == null) {
                    return;
                }

                if (player.battle == null) {
                    return;
                }

                player.battle.tanksKillModel.changeHealth(player.tank, 10000);
                player.battle.sendToAllPlayers(Type.BATTLE, "spawn", JsonUtils.parseSpawnCommand(player, this.preparedSpawnTask.preparedPosition));
                player.tank.state = "newcome";
                TankRespawnScheduler.tasks.remove(player);
            } catch (Exception var2) {
                var2.printStackTrace();
                RemoteDatabaseLogger.error(var2);
            }

        }
    }
}
