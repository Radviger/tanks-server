package gtanks.battles.maps.parser.map.bonus;

public enum BonusType {
    NITRO("nitro"),
    DAMAGE("damageup"),
    ARMOR("armorup"),
    HEAL("medkit"),
    CRYSTALL("crystal"),
    CRYSTALL_100("crystal_100");

    private String value;

    BonusType(String value) {
        this.value = value;
    }

    public static BonusType getType(String value) {
        if (value.equals("medkit")) {
            return HEAL;
        } else if (value.equals("armorup")) {
            return ARMOR;
        } else if (value.equals("damageup")) {
            return DAMAGE;
        } else if (value.equals("nitro")) {
            return NITRO;
        } else if (value.equals("crystal")) {
            return CRYSTALL;
        } else if (value.equals("crystal_100")) {
            return CRYSTALL_100;
        } else {
            return null;
        }
    }

    public String getValue() {
        return this.value;
    }
}
