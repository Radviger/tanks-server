package gtanks.services;

import gtanks.RankUtils;
import gtanks.commands.Type;
import gtanks.lobby.LobbyCommandsConst;
import gtanks.lobby.LobbyManager;
import gtanks.logger.remote.RemoteDatabaseLogger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.users.User;

public enum TanksServices {
    INSTANCE;

    private static final int MAX_UPDATE_PROGRESS_NUMBER = 10000;

    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;

    public void addScore(LobbyManager lobby, int score) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addScore: lobby null!");
        } else {
            User user = lobby.getLocalUser();
            if (user == null) {
                RemoteDatabaseLogger.error("TanksServices::addScore: user null!");
            } else {
                user.addScore(score);
                boolean increase = user.getScore() >= user.getNextScore();
                boolean fall = user.getScore() < RankUtils.getRankByIndex(user.getRang()).min;
                if (increase || fall) {
                    user.setRang(RankUtils.getNumberRank(RankUtils.getRankByScore(user.getScore())));
                    user.setNextScore(user.getRang() == 26 ? RankUtils.getRankByIndex(user.getRang()).max : RankUtils.getRankByIndex(user.getRang()).max + 1);
                    lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(MAX_UPDATE_PROGRESS_NUMBER));
                    lobby.send(Type.LOBBY, "update_rang", String.valueOf(user.getRang() + 1), String.valueOf(user.getNextScore()));
                }

                int update = RankUtils.getUpdateNumber(user.getScore());
                lobby.send(Type.LOBBY, "update_rang_progress", String.valueOf(update));
                lobby.send(Type.LOBBY, "add_score", String.valueOf(user.getScore()));
                this.database.update(user);
            }
        }
    }

    public void addCrystal(LobbyManager lobby, int crystal) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::addCrystal: lobby null!");
        } else {
            User user = lobby.getLocalUser();
            if (user == null) {
                RemoteDatabaseLogger.error("TanksServices::addCrystal: user null!");
            } else {
                user.addCrystall(crystal);
                lobby.send(Type.LOBBY, LobbyCommandsConst.ADD_CRYSTAL, String.valueOf(user.getCrystall()));
                this.database.update(user);
            }
        }
    }

    public void dummyAddCrystal(LobbyManager lobby, int crystal) {
        if (lobby == null) {
            RemoteDatabaseLogger.error("TanksServices::dummyAddCrystal: lobby null!");
        } else {
            User user = lobby.getLocalUser();
            if (user == null) {
                RemoteDatabaseLogger.error("TanksServices::dummyAddCrystal: user null!");
            } else {
                lobby.send(Type.LOBBY, "add_crystall", String.valueOf(user.getCrystall()));
            }
        }
    }
}
