package gtanks.lobby.battles;

import gtanks.battles.BattlefieldModel;
import gtanks.battles.maps.Map;

public class BattleInfo {
    public String battleId;
    public Map map;
    public BattleType battleType = BattleType.DM;
    public String name;
    public boolean team;
    public int redPeople;
    public int bluePeople;
    public int countPeople;
    public int maxPeople;
    public int minRank;
    public int maxRank;
    public boolean isPaid;
    public boolean isPrivate;
    public int time;
    public int numKills;
    public int numFlags;
    public boolean friendlyFire;
    public MapInfo info;
    public int scoreBlue = 0;
    public int scoreRed = 0;
    public boolean autobalance;
    public boolean inventory;
    public boolean withBonus;
    public BattlefieldModel model;

    public String toString() {
        return "{" + this.name + " | " + this.battleId + "}";
    }
}
