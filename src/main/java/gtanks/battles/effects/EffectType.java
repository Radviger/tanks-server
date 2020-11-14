package gtanks.battles.effects;

public enum EffectType {
    HEALTH,
    ARMOR,
    DAMAGE,
    NITRO,
    MINE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
