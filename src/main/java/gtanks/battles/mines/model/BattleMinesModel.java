package gtanks.battles.mines.model;

import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.mines.ServerMine;
import gtanks.battles.mines.activator.MinesActivatorService;
import gtanks.battles.spectator.SpectatorController;
import gtanks.battles.tanks.math.Vector3;
import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.test.osgi.OSGi;
import gtanks.test.server.configuration.entitys.MineConfiguratorEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BattleMinesModel {
    private static final String REMOVE_MINES_COMMAND = "remove_mines";
    private static final String INIT_MINES_COMMAND = "init_mines";
    private static final String HIT_MINE_COMMAND = "hit_mine";
    private static final String INIT_MINE_MODEL_COMMAND = "init_mine_model";
    private static final int minDamage;
    private static final int maxDamage;
    private static String _initObjectData;

    static {
        MineConfiguratorEntity configurator = (MineConfiguratorEntity) OSGi.getModelByInterface(MineConfiguratorEntity.class);
        minDamage = configurator.getMinDamage();
        maxDamage = configurator.getMaxDamage();
    }

    private final BattlefieldModel bfModel;
    private final Map<BattlefieldPlayerController, List<ServerMine>> mines = new FastHashMap<>();
    private final MinesActivatorService minesActivatorService = MinesActivatorService.getInstance();
    private int _incrationId;

    public BattleMinesModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void sendMines(BattlefieldPlayerController player) {
        player.send(Type.BATTLE, INIT_MINES_COMMAND, JsonUtils.parseInitMinesComand(this.mines));
    }

    public void sendMines(SpectatorController spectator) {
        spectator.sendCommand(Type.BATTLE, INIT_MINES_COMMAND, JsonUtils.parseInitMinesComand(this.mines));
    }

    public void initModel(BattlefieldPlayerController player) {
        if (_initObjectData == null) {
            _initObjectData = JsonUtils.parseConfiguratorEntity(OSGi.getModelByInterface(MineConfiguratorEntity.class), MineConfiguratorEntity.class);
        }

        player.send(Type.BATTLE, INIT_MINE_MODEL_COMMAND, _initObjectData);
    }

    public void initModel(SpectatorController spectator) {
        if (_initObjectData == null) {
            _initObjectData = JsonUtils.parseConfiguratorEntity(OSGi.getModelByInterface(MineConfiguratorEntity.class), MineConfiguratorEntity.class);
        }

        spectator.sendCommand(Type.BATTLE, INIT_MINE_MODEL_COMMAND, _initObjectData);
    }

    public void tryPutMine(BattlefieldPlayerController player, Vector3 pos) {
        ServerMine mine = new ServerMine();
        mine.setId(player.tank.id + "_" + this._incrationId);
        mine.setOwner(player);
        mine.setPosition(pos);
        List<ServerMine> userMines = this.mines.get(player);
        if (userMines == null) {
            userMines = new ArrayList<>(Collections.singletonList(mine));
            this.mines.put(player, userMines);
        } else {
            userMines.add(mine);
        }

        this.minesActivatorService.activate(this.bfModel, mine);
        ++this._incrationId;
    }

    public void playerDied(BattlefieldPlayerController player) {
        List<ServerMine> _mines;
        if ((_mines = this.mines.get(player)) != null) {
            _mines.clear();
            this.bfModel.sendToAllPlayers(Type.BATTLE, REMOVE_MINES_COMMAND, player.tank.id);
        }

    }

    public void hitMine(BattlefieldPlayerController whoHiter, String mineId) {
        BattlefieldPlayerController mineOwner = null;

        for (List<ServerMine> serverMines : this.mines.values()) {
            for (int i = 0; i < serverMines.size(); ++i) {
                ServerMine _mine = serverMines.get(i);
                if (_mine.getId().equals(mineId)) {
                    mineOwner = _mine.getOwner();
                    serverMines.remove(i);
                    break;
                }
            }
        }

        this.bfModel.sendToAllPlayers(Type.BATTLE, HIT_MINE_COMMAND, mineId, whoHiter.tank.id);
        if (mineOwner != null) {
            this.bfModel.tanksKillModel.damageTank(whoHiter, mineOwner, RandomUtils.getRandom((float) minDamage, (float) maxDamage), false);
        }
    }
}
