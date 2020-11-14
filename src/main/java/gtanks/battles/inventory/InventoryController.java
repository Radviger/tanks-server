package gtanks.battles.inventory;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.impl.*;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.users.garage.items.Item;

public class InventoryController {
    private static final String INIT_INVENTORY_COMMAND = "init_inventory";
    private static final String ACTIVATE_ITEM_COMMAND = "activate_item";
    private static final String ENABLE_EFFECT_COMMAND = "enable_effect";

    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private final BattlefieldPlayerController player;

    public InventoryController(BattlefieldPlayerController player) {
        this.player = player;
    }

    public void init() {
        this.player.send(Type.BATTLE, INIT_INVENTORY_COMMAND, JsonUtils.parseInitInventoryComand(this.player.getGarage()));
    }

    public void activateItem(String id, Vector3 tankPos) {
        Item item = this.player.getGarage().getItemById(id);
        if (item != null && item.count >= 1) {
            Effect effect = this.getEffectById(id);
            if (!this.player.tank.isUsedEffect(effect.getEffectType())) {
                effect.activate(this.player, true, tankPos);
                this.onActivatedItem(item, effect.getDurationTime());
                --item.count;
                if (item.count <= 0) {
                    this.player.getGarage().items.remove(item);
                }

                (new Thread(() -> {
                    this.player.getGarage().parseJSONData();
                    this.database.update(this.player.getGarage());
                })).start();
            }

        }
    }

    private void onActivatedItem(Item item, int durationTime) {
        this.player.send(Type.BATTLE, "activate_item", item.id);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "enable_effect", this.player.getUser().getNickname(), String.valueOf(item.index), String.valueOf(durationTime));
    }

    private Effect getEffectById(String id) {
        switch (id) {
            case "health":
                return new HealthEffect();
            case "double_damage":
                return new DamageEffect();
            case "n2o":
                return new NitroEffect();
            case "mine":
                return new Mine();
            case "armor":
                return new ArmorEffect();
        }

        Logger.log("Effect with id:" + id + " not found!");
        return null;
    }
}
