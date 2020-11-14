package gtanks.services;

import gtanks.collections.FastHashMap;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.users.User;
import gtanks.users.locations.UserLocation;

import java.util.Map;

public enum LobbyServices {
    INSTANCE;

    public Map<String, LobbyManager> lobbies = new FastHashMap<>();

    public void addLobby(LobbyManager lobby) {
        this.lobbies.put(lobby.getLocalUser().getNickname(), lobby);
    }

    public void removeLobby(LobbyManager lobby) {
        this.lobbies.remove(lobby.getLocalUser().getNickname(), lobby);
    }

    public boolean containsLobby(LobbyManager lobby) {
        return this.lobbies.containsKey(lobby.getLocalUser().getNickname());
    }

    public LobbyManager getLobbyByUser(User user) {
        return this.lobbies.get(user.getNickname());
    }

    public LobbyManager getLobbyByNick(String nick) {
        LobbyManager lobbyManager = null;

        for (LobbyManager lobby : this.lobbies.values()) {
            if (lobby.getLocalUser().getNickname().equals(nick)) {
                lobbyManager = lobby;
                break;
            }
        }

        return lobbyManager;
    }

    public void sendCommandToAllUsers(Type type, UserLocation onlyFor, String... args) {
        try {
            for (LobbyManager lobby : this.lobbies.values()) {
                if (onlyFor == UserLocation.ALL || onlyFor == lobby.getLocalUser().getUserLocation()) {
                    lobby.send(type, args);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommandToAllUsersBesides(Type type, UserLocation besides, String... args) {
        try {
            for (LobbyManager lobby : this.lobbies.values()) {
                if (lobby != null && lobby.getLocalUser().getUserLocation() != besides) {
                    lobby.send(type, args);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
