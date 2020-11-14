package gtanks.battles.tanks.weapons.twins;

import gtanks.battles.tanks.weapons.EntityType;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class TwinsEntity implements IEntity {
    public float shotRange;
    public float shotSpeed;
    public float shotRadius;
    public float damage_min;
    public float damage_max;
    private ShotData shotData;

    public TwinsEntity(float shotRange, float shotSpeed, float shotRadius, float damage_min, float damage_max, ShotData shotData) {
        this.shotRange = shotRange;
        this.shotSpeed = shotSpeed;
        this.shotRadius = shotRadius;
        this.shotData = shotData;
        this.damage_max = damage_max;
        this.damage_min = damage_min;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return EntityType.TWINS;
    }
}
