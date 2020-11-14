package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;

import java.util.TimerTask;

public class Mine extends TimerTask implements Effect {
    private static final EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();
    private BattlefieldPlayerController player;
    private boolean deactivated;

    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        if (!fromInventory) {
            throw new IllegalArgumentException("Effect 'Mine' was not caused from inventory!");
        } else {
            this.player = player;
            synchronized (player.tank.activeEffects) {
                player.tank.activeEffects.add(this);
            }

            player.battle.battleMinesModel.tryPutMine(player, tankPos);
            effectActivatorService.activateEffect(this, (long) this.getDurationTime());
        }
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.MINE;
    }

    @Override
    public int getID() {
        return 5;
    }

    @Override
    public int getDurationTime() {
        return 30000;
    }

    @Override
    public void run() {
        if (!this.deactivated) {
            this.deactivate();
        }
    }
}
