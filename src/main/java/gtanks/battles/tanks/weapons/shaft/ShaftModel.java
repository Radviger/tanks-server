package gtanks.battles.tanks.weapons.shaft;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.IWeapon;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;

public class ShaftModel implements IWeapon {
    private BattlefieldModel bfModel;
    private BattlefieldPlayerController player;
    private ShaftEntity entity;
    private WeaponWeakeningData weakeingData;

    public ShaftModel(ShaftEntity entity, WeaponWeakeningData weakeingData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeingData = weakeingData;
    }

    @Override
    public void fire(String json) {
    }

    @Override
    public void startFire(String json) {
    }

    @Override
    public void stopFire() {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targetsTanks, int distance) {
    }

    @Override
    public IEntity getEntity() {
        return this.entity;
    }
}
