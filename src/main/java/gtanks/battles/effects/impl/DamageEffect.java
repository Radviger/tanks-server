package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;

import java.util.TimerTask;

public class DamageEffect extends TimerTask implements Effect {
    private static final long INVENTORY_TIME_ACTION = 60000L;
    private static final long DROP_TIME_ACTION = 40000L;

    private final EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();
    private BattlefieldPlayerController player;
    private boolean deactivated;

    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        this.player = player;
        player.tank.activeEffects.add(this);
        this.effectActivatorService.activateEffect(this, fromInventory ? INVENTORY_TIME_ACTION : DROP_TIME_ACTION);
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
    }

    @Override
    public void run() {
        if (!this.deactivated) {
            this.deactivate();
        }
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.DAMAGE;
    }

    @Override
    public int getID() {
        return 3;
    }

    @Override
    public int getDurationTime() {
        return (int) INVENTORY_TIME_ACTION;
    }
}
