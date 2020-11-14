package gtanks.battles.tanks.weapons;

public interface WeaponEntity {
    ShotData getShotData();

    WeaponType getType();

    String toString();
}
