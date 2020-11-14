package gtanks.battles.spectator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.spectator.comands.SpectatorControllerComandsConst;
import gtanks.battles.tanks.loaders.WeaponsFactory;
import gtanks.commands.Command;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.lobby.LobbyManager;
import gtanks.lobby.battles.BattleType;
import gtanks.logger.Logger;
import gtanks.network.listeners.DisconnectListener;
import gtanks.users.User;

public class SpectatorController extends SpectatorControllerComandsConst implements DisconnectListener {
    private static final String NULL_JSON_STRING = "{}";
    private LobbyManager lobby;
    private BattlefieldModel bfModel;
    private SpectatorModel specModel;
    private boolean inited;

    public SpectatorController(LobbyManager lobby, BattlefieldModel bfModel, SpectatorModel specModel) {
        this.lobby = lobby;
        this.bfModel = bfModel;
        this.specModel = specModel;
    }

    public void executeCommand(Command cmd) {
        switch (cmd.type) {
            case BATTLE:
                switch (cmd.args[0]) {
                    case "spectator_user_init":
                        this.initUser();
                        break;
                    case "i_exit_from_battle":
                        this.lobby.onExitFromBattle();
                        break;
                    case "chat":
                        this.specModel.getChatModel().onMessage(cmd.args[1], this);
                        break;
                }
                break;
            default:
                Logger.log("[executeCommand(Command)::SpectatorController] : non-battle command " + cmd);
        }

    }

    private void initUser() {
        try {
            this.inited = true;
            this.sendShotsData();
            if (this.bfModel.battleInfo.battleType == BattleType.CTF) {
                this.sendCommand(Type.BATTLE, INIT_CTF_MODEL, JsonUtils.parseCTFModelData(this.bfModel));
            }

            this.sendCommand(Type.BATTLE, INIT_GUI_MODEL, JsonUtils.parseBattleData(this.bfModel));
            this.sendCommand(Type.BATTLE, INIT_INVENTORY_COMAND, NULL_JSON_STRING);
            this.bfModel.battleMinesModel.initModel(this);
            this.bfModel.battleMinesModel.sendMines(this);
            this.bfModel.sendAllTanks(this);
            this.bfModel.effectsModel.sendInitData(this);
        } catch (Exception e) {
            this.lobby.kick();
        }
    }

    public String getId() {
        return this.lobby.getLocalUser().getNickname();
    }

    public User getUser() {
        return this.lobby.getLocalUser();
    }

    public void sendCommand(Type type, String... args) {
        if (this.inited) {
            this.lobby.send(type, args);
        }

    }

    private void sendShotsData() {
        this.sendCommand(Type.BATTLE, "init_shots_data", WeaponsFactory.getJSONList());
    }

    @Override
    public void onDisconnect() {
        this.bfModel.spectatorModel.removeSpectator(this);
    }
}
