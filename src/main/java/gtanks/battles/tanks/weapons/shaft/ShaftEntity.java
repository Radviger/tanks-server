package gtanks.battles.tanks.weapons.shaft;

import gtanks.battles.tanks.weapons.EntityType;
import gtanks.battles.tanks.weapons.IEntity;
import gtanks.battles.tanks.weapons.ShotData;

public class ShaftEntity implements IEntity {
    public final EntityType type;
    public float damage_min;
    public float damage_max;
    private ShotData shotData;

    public ShaftEntity(ShotData shotData, float damage_min, float damage_max) {
        this.type = EntityType.SHAFT;
        this.damage_min = damage_min;
        this.damage_max = damage_max;
        this.shotData = shotData;
    }

    @Override
    public ShotData getShotData() {
        return this.shotData;
    }

    @Override
    public EntityType getType() {
        return this.type;
    }
}
