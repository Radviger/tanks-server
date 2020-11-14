package gtanks.battles.bonuses.model;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.bonuses.Bonus;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.effects.impl.ArmorEffect;
import gtanks.battles.effects.impl.DamageEffect;
import gtanks.battles.effects.impl.HealthEffect;
import gtanks.battles.effects.impl.NitroEffect;
import gtanks.battles.tanks.math.Vector3;
import gtanks.commands.Type;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;

public class BonusTakeModel {
    private static final String SET_CRY = "set_cry";
    private static final String ENABLE_EFFECT_COMMAND = "enable_effect";
    private static final int CRYSTAL_BONUS_COST = 1;
    private static final int GOLD_BONUS_COST = 100;
    private final BattlefieldModel bfModel;
    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;

    public BonusTakeModel(BattlefieldModel bfModel) {
        this.bfModel = bfModel;
    }

    public boolean onTakeBonus(Bonus bonus, Vector3 realtimePosTank, BattlefieldPlayerController player) {
        switch (bonus.type) {
            case GOLD:
                this.bfModel.sendUserLogMessage(player.parentLobby.getLocalUser().getNickname(), "взял золотой ящик");
                player.parentLobby.getLocalUser().addCrystall(GOLD_BONUS_COST);
                player.send(Type.BATTLE, SET_CRY, String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
                break;
            case CRYSTAL:
                player.parentLobby.getLocalUser().addCrystall(CRYSTAL_BONUS_COST);
                player.send(Type.BATTLE, SET_CRY, String.valueOf(player.parentLobby.getLocalUser().getCrystall()));
                this.database.update(player.getUser());
                break;
            case ARMOR:
                this.activateDrop(new ArmorEffect(), player);
                break;
            case HEALTH:
                this.activateDrop(new HealthEffect(), player);
                break;
            case DAMAGE:
                this.activateDrop(new DamageEffect(), player);
                break;
            case NITRO:
                this.activateDrop(new NitroEffect(), player);
        }

        return true;
    }

    private void activateDrop(Effect effect, BattlefieldPlayerController player) {
        if (!player.tank.isUsedEffect(effect.getEffectType())) {
            effect.activate(player, false, player.tank.position);
            player.battle.sendToAllPlayers(Type.BATTLE, ENABLE_EFFECT_COMMAND, player.getUser().getNickname(), String.valueOf(effect.getID()), effect.getEffectType() == EffectType.HEALTH ? String.valueOf(10000) : String.valueOf(40000));
        }
    }
}
