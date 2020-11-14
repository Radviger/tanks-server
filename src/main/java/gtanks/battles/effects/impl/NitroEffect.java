package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.json.JsonUtils;

import java.util.TimerTask;

public class NitroEffect extends TimerTask implements Effect {
    private static final String CHANGE_TANK_SPEC_COMMAND = "change_spec_tank";
    private static final long INVENTORY_TIME_ACTION = 60000L;
    private static final long DROP_TIME_ACTION = 40000L;
    private static final EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();

    private BattlefieldPlayerController player;
    private boolean deactivated;

    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        this.player = player;
        synchronized (player.tank.activeEffects) {
            player.tank.activeEffects.add(this);
        }

        player.tank.speed = this.addPercent(player.tank.speed, 30);
        player.battle.sendToAllPlayers(Type.BATTLE, "change_spec_tank", player.tank.id, JsonUtils.parseTankSpec(player.tank, true));
        effectActivatorService.activateEffect(this, fromInventory ? INVENTORY_TIME_ACTION : DROP_TIME_ACTION);
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
        this.player.tank.speed = this.player.tank.getHull().speed;
        this.player.battle.sendToAllPlayers(Type.BATTLE, "change_spec_tank", this.player.tank.id, JsonUtils.parseTankSpec(this.player.tank, true));
    }

    @Override
    public void run() {
        if (!this.deactivated) {
            this.deactivate();
        }
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.NITRO;
    }

    @Override
    public int getID() {
        return 4;
    }

    private float addPercent(float value, int percent) {
        return value / 100.0F * (float) percent + value;
    }

    @Override
    public int getDurationTime() {
        return (int) INVENTORY_TIME_ACTION;
    }
}
