package gtanks.battles.tanks.weapons.anticheats;

import gtanks.battles.tanks.weapons.WeaponModel;

public abstract class TickableWeaponModel implements WeaponModel {
    private final int normalTickTime;

    public TickableWeaponModel(int normalTickTime) {
        this.normalTickTime = normalTickTime;
    }

    public boolean check(int timeFromClient) {
        return this.normalTickTime == timeFromClient;
    }
}
