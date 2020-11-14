package gtanks.battles.bonuses;

import gtanks.battles.tanks.math.Vector3;

public class Bonus {
    public Vector3 position;
    public BonusType type;
    public long spawnTime;

    public Bonus(Vector3 position, BonusType type) {
        this.position = position;
        this.type = type;
        this.spawnTime = System.currentTimeMillis();
    }
}
