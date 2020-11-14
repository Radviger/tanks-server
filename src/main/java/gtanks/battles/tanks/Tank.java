package gtanks.battles.tanks;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.effects.Effect;
import gtanks.battles.effects.EffectType;
import gtanks.battles.tanks.colormaps.Colormap;
import gtanks.battles.tanks.data.DamageTankData;
import gtanks.battles.tanks.hulls.Hull;
import gtanks.battles.tanks.math.Vector3;
import gtanks.battles.tanks.weapons.WeaponModel;
import gtanks.battles.tanks.weapons.frezee.effects.FreezeEffectModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Tank {
    public static final int MAX_HEALTH_TANK = 10000;
    public Vector3 position;
    public Vector3 orientation;
    public Vector3 linVel;
    public Vector3 angVel;
    public double turretDir;
    public int controllBits;
    public String id;
    public float speed;
    public float turnSpeed;
    public float turretRotationSpeed;
    public int health = MAX_HEALTH_TANK;
    public int incrationId;
    public String state = "active";
    public FreezeEffectModel freezeEffect;
    public final List<Effect> activeEffects = new ArrayList<>();
    public Map<BattlefieldPlayerController, DamageTankData> lastDamagers = new LinkedHashMap<>();
    private WeaponModel weapon;
    private Hull hull;
    private Colormap colormap;

    public Tank(Vector3 position) {
        this.position = position;
    }

    public WeaponModel getWeapon() {
        return this.weapon;
    }

    public void setWeapon(WeaponModel weapon) {
        this.weapon = weapon;
        this.turretRotationSpeed = weapon.getEntity().getShotData().turretRotationSpeed;
    }

    public Hull getHull() {
        return this.hull;
    }

    public void setHull(Hull hull) {
        this.hull = hull;
        this.speed = hull.speed;
        this.turnSpeed = hull.turnSpeed;
    }

    public Colormap getColormap() {
        return this.colormap;
    }

    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    public boolean isUsedEffect(EffectType type) {
        for (Object activeEffect : this.activeEffects) {
            Effect effect = (Effect) activeEffect;
            if (effect.getEffectType() == type) {
                return true;
            }
        }

        return false;
    }
}
