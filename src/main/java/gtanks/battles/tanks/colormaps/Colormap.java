package gtanks.battles.tanks.colormaps;

import gtanks.battles.tanks.weapons.EntityType;

import java.util.HashMap;
import java.util.Map;

public class Colormap {
    private Map<ColormapResistanceType, Integer> resistances = new HashMap<>();

    public void addResistance(ColormapResistanceType type, int percent) {
        this.resistances.put(type, percent);
    }

    public Integer getResistance(EntityType weaponType) {
        return this.resistances.get(weaponType.colormap);
    }
}
