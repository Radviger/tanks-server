package gtanks.battles.tanks.weapons;

import com.google.gson.JsonObject;
import gtanks.battles.BattlefieldPlayerController;

public interface WeaponModel {
    void fire(JsonObject data);

    void startFire(JsonObject data);

    void stopFire();

    void onTarget(BattlefieldPlayerController[] targets, int distance);

    WeaponEntity getEntity();
}
