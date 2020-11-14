package gtanks.battles.tanks.weapons.snowman;

import gtanks.battles.tanks.weapons.EntityType;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class SnowmanEntity implements IEntity {
    public float shotRange;
    public float shotSpeed;
    public float shotRadius;
    public float damage_min;
    public float damage_max;
    public float frezeeSpeed;
    private ShotData shotData;

    public SnowmanEntity(float shotRange, float shotSpeed, float shotRadius, float damage_min, float damage_max, float frezeeSpeed, ShotData shotData) {
        this.shotRange = shotRange;
        this.shotSpeed = shotSpeed;
        this.shotRadius = shotRadius;
        this.shotData = shotData;
        this.damage_max = damage_max;
        this.damage_min = damage_min;
        this.frezeeSpeed = frezeeSpeed;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return EntityType.SNOWMAN;
    }
}
