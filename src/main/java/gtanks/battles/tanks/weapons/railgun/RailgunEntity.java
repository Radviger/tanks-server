package gtanks.battles.tanks.weapons.railgun;

import gtanks.battles.tanks.weapons.WeaponType;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class RailgunEntity implements WeaponEntity {
    public final WeaponType type;
    public int chargingTime;
    public int weakeningCoefficient;
    public float damage_min;
    public float damage_max;
    private final ShotData shotData;

    public RailgunEntity(ShotData shotData, int chargingTime, int weakeningCoefficient, float damage_min, float damage_max) {
        this.type = WeaponType.RAILGUN;
        this.chargingTime = chargingTime;
        this.weakeningCoefficient = weakeningCoefficient;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.shotData = shotData;
    }

    @Override
    public String toString() {
        return "chargingTime: " + this.chargingTime + "\nweakeningCoeff: " + this.weakeningCoefficient + "\ndamage_min: " + this.damage_min + "\ndamage_max: " + this.damage_max;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public WeaponType getType() {
        return this.type;
    }
}
