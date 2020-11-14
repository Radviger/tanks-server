package gtanks.battles.effects.impl;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.activator.EffectActivatorService;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;

public class HealthEffect extends Thread implements Effect {
    private static final int HP_IN_SEC = 30;
    private static final int HP_FOR_ITERATION = 15;
    private int resource;
    private int accumulatedResource;

    private final EffectActivatorService effectActivatorService = EffectActivatorService.getInstance();
    private BattlefieldPlayerController player;
    private boolean deactivated;

    @Override
    public void activate(BattlefieldPlayerController player, boolean fromInventory, Vector3 tankPos) {
        this.player = player;
        this.resource = fromInventory ? (int) player.tank.getHull().hp : (int) player.tank.getHull().hp / 2;
        player.tank.activeEffects.add(this);
        this.start();
    }

    @Override
    public void deactivate() {
        this.deactivated = true;
        this.player.tank.activeEffects.remove(this);
        this.player.battle.sendToAllPlayers(Type.BATTLE, "disnable_effect", this.player.getUser().getNickname(), String.valueOf(this.getID()));
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!this.deactivated) {
                    if (this.accumulatedResource + HP_FOR_ITERATION > this.resource) {
                        this.healTank(this.resource - this.accumulatedResource);
                    } else {
                        this.healTank(HP_FOR_ITERATION);
                        this.accumulatedResource += HP_FOR_ITERATION;
                        sleep(500L);
                        if (this.accumulatedResource <= this.resource) {
                            continue;
                        }
                    }
                }

                if (!this.deactivated) {
                    this.deactivate();
                }
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }

            return;
        }
    }

    private void healTank(int hp) {
        this.player.battle.tanksKillModel.healPlayer((BattlefieldPlayerController) null, this.player, (float) hp);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.HEALTH;
    }

    @Override
    public int getID() {
        return 1;
    }

    @Override
    public int getDurationTime() {
        return 5000;
    }
}
