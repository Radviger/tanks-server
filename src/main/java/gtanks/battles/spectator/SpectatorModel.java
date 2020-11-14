package gtanks.battles.spectator;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.spectator.chat.SpectatorChatModel;
import gtanks.commands.Type;
import gtanks.system.BattlesGC;

import java.util.HashMap;
import java.util.Map;

public class SpectatorModel {
    private final Map<String, SpectatorController> spectators = new HashMap<>();
    private final SpectatorChatModel chatModel;
    private final BattlefieldModel bfModel;

    public SpectatorModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
        chatModel = new SpectatorChatModel(this);
    }

    public void addSpectator(SpectatorController spec) {
        this.spectators.put(spec.getId(), spec);
        BattlesGC.cancelRemoving(this.bfModel);
    }

    public void removeSpectator(SpectatorController spec) {
        this.spectators.remove(spec.getId());
        if (this.bfModel != null) {
            if (this.bfModel.players != null) {
                if (this.bfModel.players.size() == 0 && this.spectators.size() == 0) {
                    BattlesGC.addBattleForRemove(this.bfModel);
                }
            }
        }
    }

    public SpectatorChatModel getChatModel() {
        return this.chatModel;
    }

    public BattlefieldModel getBattleModel() {
        return this.bfModel;
    }

    public void sendCommandToSpectators(Type type, String... args) {
        for (SpectatorController sc : this.spectators.values()) {
            sc.sendCommand(type, args);
        }
    }
}
