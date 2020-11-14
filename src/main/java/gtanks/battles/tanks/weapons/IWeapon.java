package gtanks.battles.tanks.weapons;

import gtanks.battles.BattlefieldPlayerController;

public interface IWeapon {
    void fire(String var1);

    void startFire(String var1);

    void stopFire();

    void onTarget(BattlefieldPlayerController[] var1, int var2);

    IEntity getEntity();
}
