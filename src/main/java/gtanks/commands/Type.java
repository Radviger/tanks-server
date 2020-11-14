package gtanks.commands;

public enum Type {
    AUTH,
    REGISTRATION,
    GARAGE,
    CHAT,
    LOBBY,
    LOBBY_CHAT,
    BATTLE,
    PING,
    UNKNOWN,
    HTTP,
    SYSTEM;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
