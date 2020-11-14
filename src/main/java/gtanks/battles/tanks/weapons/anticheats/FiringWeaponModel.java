package gtanks.battles.tanks.weapons.anticheats;

import gtanks.battles.tanks.weapons.WeaponModel;

public abstract class FiringWeaponModel implements WeaponModel {
    private final int normalReloadTime;

    public FiringWeaponModel(int normalReloadTime) {
        this.normalReloadTime = normalReloadTime;
    }

    public boolean check(int reloadTimeFromClient) {
        return this.normalReloadTime == reloadTimeFromClient;
    }
}
