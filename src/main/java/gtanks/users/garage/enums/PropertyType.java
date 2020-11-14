package gtanks.users.garage.enums;

public enum PropertyType {
    DAMAGE,
    DAMAGE_PER_SECOND,
    AIMING_ERROR,
    CONE_ANGLE,
    SHOT_AREA,
    SHOT_FREQUENCY,
    SHOT_RANGE,
    TURN_SPEED,
    MECH_RESISTANCE,
    PLASMA_RESISTANCE,
    RAIL_RESISTANCE,
    VAMPIRE_RESISTANCE,
    ARMOR,
    TURRET_TURN_SPEED,
    FIRE_RESISTANCE,
    THUNDER_RESISTANCE,
    FREEZE_RESISTANCE,
    RICOCHET_RESISTANCE,
    HEALING_RADIUS,
    HEAL_RATE,
    VAMPIRE_RATE,
    SPEED,
    UNKNOWN;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
