package gtanks.battles.maps.parser.map.spawn;

public enum SpawnPositionType {
    BLUE, RED, NONE;

    public static SpawnPositionType getType(String value) {
        switch (value) {
            case "blue":
                return BLUE;
            case "red":
                return RED;
            case "dm":
            default:
                return NONE;
        }
    }
}
