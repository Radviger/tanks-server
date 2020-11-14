package gtanks.battles.tanks.weapons;

import gtanks.battles.tanks.colormaps.ColormapResistanceType;

public enum EntityType {
    SMOKY(ColormapResistanceType.SMOKY),
    FLAMETHROWER(ColormapResistanceType.FLAMETHROWER),
    TWINS(ColormapResistanceType.TWINS),
    RAILGUN(ColormapResistanceType.RAILGUN),
    ISIDA(ColormapResistanceType.ISIDA),
    THUNDER(ColormapResistanceType.THUNDER),
    FREZZE(ColormapResistanceType.FREEZE),
    RICOCHET(ColormapResistanceType.RICOCHET),
    SHAFT(null),
    SNOWMAN(null);

    public final ColormapResistanceType colormap;

    EntityType(ColormapResistanceType colormap) {
        this.colormap = colormap;
    }
}
