package gtanks.battles.tanks.weapons.thunder;

import gtanks.battles.tanks.weapons.WeaponType;
import gtanks.battles.tanks.weapons.WeaponEntity;
import gtanks.battles.tanks.weapons.ShotData;
import gtanks.battles.tanks.weapons.WeaponWeakeningData;

public class ThunderEntity implements WeaponEntity {
    public float maxSplashDamageRadius;
    public float minSplashDamageRadius;
    public float minSplashDamagePercent;
    public float impactForce;
    public WeaponWeakeningData wwd;
    public float damage_min;
    public float damage_max;
    private ShotData shotData;

    public ThunderEntity(float maxSplashDamageRadius, float minSplashDamageRadius, float minSplashDamagePercent, float impactForce, ShotData shotData, float damage_min, float damage_max, WeaponWeakeningData wwd) {
        this.maxSplashDamageRadius = maxSplashDamageRadius;
        this.minSplashDamageRadius = minSplashDamageRadius;
        this.minSplashDamagePercent = minSplashDamagePercent;
        this.impactForce = impactForce;
        this.shotData = shotData;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.wwd = wwd;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public WeaponType getType() {
        return WeaponType.THUNDER;
    }
}
