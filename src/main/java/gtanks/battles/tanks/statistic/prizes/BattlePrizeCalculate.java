package gtanks.battles.tanks.statistic.prizes;

import gtanks.battles.BattlefieldPlayerController;
import gtanks.battles.tanks.statistic.PlayerStatistic;
import gtanks.services.TanksServices;

import java.util.Collections;
import java.util.List;

public class BattlePrizeCalculate {
    private static final TanksServices tankServices = TanksServices.INSTANCE;

    public static void calc(List<BattlefieldPlayerController> users, int fund) {
        if (users != null && users.size() != 0) {
            BattlefieldPlayerController _first = Collections.max(users, (a, b) -> (int) (a.statistic.getScore() - b.statistic.getScore()));
            PlayerStatistic first = _first.statistic;
            double sumSquare = 0.0D;
            int countFirstUsers = 0;

            for (BattlefieldPlayerController user : users) {
                long value = user.statistic.getScore();
                if (value != first.getScore()) {
                    sumSquare += (double) (value * value);
                } else {
                    ++countFirstUsers;
                }
            }

            sumSquare += (double) (first.getScore() * first.getScore() * (long) countFirstUsers * (long) countFirstUsers);
            int allSum = 0;

            for (BattlefieldPlayerController user : users) {
                if (user.statistic.getScore() != first.getScore()) {
                    int prize = (int) ((double) ((long) fund * user.statistic.getScore() * user.statistic.getScore()) / sumSquare);
                    if (prize < 0) {
                        prize = Math.abs(prize);
                    }

                    allSum += prize;
                    user.statistic.setPrize(prize);
                    tankServices.addCrystal(user.parentLobby, prize);
                }
            }

            int delta = (fund - allSum) / countFirstUsers;

            for (BattlefieldPlayerController user : users) {
                PlayerStatistic _user = user.statistic;
                if (_user.getScore() == first.getScore() && user != _first) {
                    _user.setPrize(delta);
                    tankServices.addCrystal(user.parentLobby, delta);
                    allSum += delta;
                }
            }

            first.setPrize(first.getPrize() + (fund - allSum));
            tankServices.addCrystal(_first.parentLobby, first.getPrize());
        }
    }

    public static void calculateForTeam(List<BattlefieldPlayerController> redUsers, List<BattlefieldPlayerController> blueUsers, int scoreRed, int scoreBlue, double looseKoeff, int fund) {
        List<BattlefieldPlayerController> usersWin;
        List<BattlefieldPlayerController> usersLoose;
        int prizeWin;
        int prizeLoose;
        if (scoreRed != scoreBlue) {
            int scoreWin = Math.max(scoreRed, scoreBlue);
            int scoreLoose = Math.min(scoreRed, scoreBlue);
            prizeLoose = (int) ((double) fund * looseKoeff * (double) scoreLoose / (double) scoreWin);
            prizeWin = fund - prizeLoose;
            usersWin = scoreRed > scoreBlue ? redUsers : blueUsers;
            usersLoose = scoreRed > scoreBlue ? blueUsers : redUsers;
        } else {
            prizeLoose = (int) Math.ceil((float) fund / 2.0F);
            prizeWin = (int) Math.ceil((float) fund / 2.0F);
            usersWin = redUsers;
            usersLoose = blueUsers;
        }

        calc(usersWin, prizeWin);
        calc(usersLoose, prizeLoose);
    }
}
