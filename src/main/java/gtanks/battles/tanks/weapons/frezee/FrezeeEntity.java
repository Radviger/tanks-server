package gtanks.battles.tanks.weapons.frezee;

import gtanks.battles.tanks.weapons.WeaponType;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class FrezeeEntity implements WeaponEntity {
    public float damageAreaConeAngle;
    public float damageAreaRange;
    public int energyCapacity;
    public int energyDischargeSpeed;
    public int energyRechargeSpeed;
    public int weaponTickMsec;
    public float coolingSpeed;
    public float damage_min;
    public float damage_max;
    private ShotData shotData;

    public FrezeeEntity(float damageAreaConeAngle, float damageAreaRange, int energyCapacity, int energyDischargeSpeed, int energyRechargeSpeed, int weaponTickMsec, float coolingSpeed, float damage_min, float damage_max, ShotData shotData) {
        this.damageAreaConeAngle = damageAreaConeAngle;
        this.damageAreaRange = damageAreaRange;
        this.energyCapacity = energyCapacity;
        this.energyDischargeSpeed = energyDischargeSpeed;
        this.energyRechargeSpeed = energyRechargeSpeed;
        this.weaponTickMsec = weaponTickMsec;
        this.coolingSpeed = coolingSpeed;
        this.shotData = shotData;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public WeaponType getType() {
        return WeaponType.FREZZE;
    }
}
