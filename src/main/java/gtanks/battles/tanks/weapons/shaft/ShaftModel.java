package gtanks.battles.tanks.weapons.shaft;

import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.WeaponModel;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;

public class ShaftModel implements WeaponModel {
    private final BattlefieldModel bfModel;
    private final BattlefieldPlayerController player;
    private final ShaftEntity entity;
    private final WeaponWeakeningData weakeningData;

    public ShaftModel(ShaftEntity entity, WeaponWeakeningData weakeningData, BattlefieldModel bfModel, BattlefieldPlayerController player) {
        this.entity = entity;
        this.bfModel = bfModel;
        this.player = player;
        this.weakeningData = weakeningData;
    }

    @Override
    public void fire(JsonObject data) {
    }

    @Override
    public void startFire(JsonObject data) {
    }

    @Override
    public void stopFire() {
    }

    @Override
    public void onTarget(BattlefieldPlayerController[] targets, int distance) {
    }

    @Override
    public WeaponEntity getEntity() {
        return this.entity;
    }
}
