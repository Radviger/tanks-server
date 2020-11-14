package gtanks.users.garage.enums;

public enum ItemType {
    WEAPON,
    ARMOR,
    COLOR,
    INVENTORY,
    PLUGIN;

    @Override
    public String toString() {
        return Integer.toString(ordinal() + 1);
    }
}
