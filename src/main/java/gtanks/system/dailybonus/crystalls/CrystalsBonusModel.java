package gtanks.system.dailybonus.crystalls;

import gtanks.lobby.LobbyManager;
import gtanks.services.TanksServices;

public class CrystalsBonusModel {
    private static final TanksServices tanksServices = TanksServices.INSTANCE;
    private static final int[] CRYSTALS = new int[]{0, 0, 15, 25, 35, 50, 60, 75, 85, 95, 110, 120, 135, 145, 155, 170, 180, 195, 205, 215, 230, 240, 255, 265, 275, 290, 300};

    public void applyBonus(LobbyManager lobby) {
        int bonus = this.getBonus(lobby.getLocalUser().getRang());
        tanksServices.addCrystal(lobby, bonus);
    }

    public int getBonus(int rangIndex) {
        return CRYSTALS[rangIndex];
    }
}
