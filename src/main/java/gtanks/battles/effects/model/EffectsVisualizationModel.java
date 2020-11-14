package gtanks.battles.effects.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.spectator.SpectatorController;
import gtanks.commands.Type;

public class EffectsVisualizationModel {
    private static final Gson GSON = new Gson();
    private BattlefieldModel bfModel;

    public EffectsVisualizationModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public void sendInitData(BattlefieldPlayerController player) {
        JsonObject obj = new JsonObject();
        JsonArray effects = new JsonArray();
        for (BattlefieldPlayerController p : this.bfModel.players) {
            if (p != player) {
                synchronized (p.tank.activeEffects) {
                    for (Effect effect : p.tank.activeEffects) {
                        JsonObject e = new JsonObject();
                        e.addProperty("userID", p.getUser().getNickname());
                        e.addProperty("itemIndex", effect.getID());
                        e.addProperty("durationTime", 60000);
                        effects.add(e);
                    }
                }
            }
        }
        obj.add("effects", effects);
        player.send(Type.BATTLE, "init_effects", GSON.toJson(obj));
    }

    public void sendInitData(SpectatorController player) {
        JsonObject obj = new JsonObject();
        JsonArray array = new JsonArray();

        for (BattlefieldPlayerController p : this.bfModel.players.values()) {
            synchronized (p.tank.activeEffects) {
                for (Effect effect : p.tank.activeEffects) {
                    JsonObject e = new JsonObject();
                    e.addProperty("userID", p.getUser().getNickname());
                    e.addProperty("itemIndex", effect.getID());
                    e.addProperty("durationTime", 60000);
                    array.add(e);
                }
            }
        }

        obj.add("effects", array);
        player.sendCommand(Type.BATTLE, "init_effects", GSON.toJson(obj));
    }
}
