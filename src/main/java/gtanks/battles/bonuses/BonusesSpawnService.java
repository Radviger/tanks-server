package gtanks.battles.bonuses;

import gtanks.RandomUtils;
import gtanks.battles.BattlefieldModel;
import gtanks.battles.maps.Map;
import gtanks.battles.tanks.math.Vector3;
import gtanks.logger.Logger;
import gtanks.logger.Type;

import java.util.Random;

public class BonusesSpawnService implements Runnable {
    private static final int DISAPPEARING_TIME_DROP = 30;
    private static final int DISAPPEARING_TIME_MONEY = 300;
    public BattlefieldModel battlefieldModel;
    private Random random = new Random();
    private int inc = 0;
    private int prevFund = 0;
    private int crystalFund;
    private int goldFund;
    private int nextGoldFund;

    public BonusesSpawnService(BattlefieldModel model) {
        this.battlefieldModel = model;
        this.nextGoldFund = (int) RandomUtils.getRandom(700.0F, 730.0F);
    }

    public void spawnRandomDrop() {
        int id = this.random.nextInt(4);
        BonusType bonusType = null;
        switch (id) {
            case 0:
                bonusType = BonusType.ARMOR;
                break;
            case 1:
                bonusType = BonusType.HEALTH;
                break;
            case 2:
                bonusType = BonusType.DAMAGE;
                break;
            case 3:
                bonusType = BonusType.NITRO;
        }

        int count = this.random.nextInt(4);

        for (int i = 0; i < count; ++i) {
            this.spawnBonus(bonusType);
        }
    }

    public void spawnRandomBonus() {
        boolean wasSpawned = this.random.nextBoolean();
        if (wasSpawned && this.battlefieldModel.players.size() > 0) {
            int id = this.random.nextInt(5);
            BonusType bonusType = null;
            switch (id) {
                case 0:
                    bonusType = BonusType.NITRO;
                    break;
                case 1:
                    bonusType = BonusType.ARMOR;
                    break;
                case 2:
                    bonusType = BonusType.HEALTH;
                    break;
                case 3:
                    bonusType = BonusType.DAMAGE;
                    break;
                case 4:
                    bonusType = BonusType.NITRO;
            }

            int count = this.random.nextInt(4);

            for (int i = 0; i < count; ++i) {
                this.spawnBonus(bonusType);
            }
        }

    }

    public void spawnBonus(BonusType type) {
        Map map = this.battlefieldModel.battleInfo.map;
        switch (type) {
            case GOLD:
                if (map.goldsRegions.size() > 0) {
                    int index = this.random.nextInt(map.goldsRegions.size());
                    BonusRegion region = map.goldsRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.GOLD);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_MONEY);
                }
                break;
            case CRYSTAL:
                if (map.crystallsRegions.size() > 0) {
                    int index = this.random.nextInt(map.crystallsRegions.size());
                    BonusRegion region = map.crystallsRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.CRYSTAL);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_MONEY);
                }
                break;
            case ARMOR:
                if (map.armorsRegions.size() > 0) {
                    int index = this.random.nextInt(map.armorsRegions.size());
                    BonusRegion region = map.armorsRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.ARMOR);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_DROP);
                }
                break;
            case HEALTH:
                if (map.healthsRegions.size() > 0) {
                    int index = this.random.nextInt(map.healthsRegions.size());
                    BonusRegion region = map.healthsRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.HEALTH);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_DROP);
                }
                break;
            case DAMAGE:
                if (map.damagesRegions.size() > 0) {
                    int index = this.random.nextInt(map.damagesRegions.size());
                    BonusRegion region = map.damagesRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.DAMAGE);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_DROP);
                }
                break;
            case NITRO:
                if (map.nitrosRegions.size() > 0) {
                    int index = this.random.nextInt(map.nitrosRegions.size());
                    BonusRegion region = map.nitrosRegions.get(index);
                    Bonus bonus = new Bonus(this.getRandomSpawnPostiton(region), BonusType.NITRO);
                    this.battlefieldModel.spawnBonus(bonus, this.inc, DISAPPEARING_TIME_DROP);
                }
        }

        ++this.inc;
    }

    public void battleFinished() {
        this.prevFund = 0;
        this.crystalFund = 0;
        this.goldFund = 0;
        this.nextGoldFund = (int) RandomUtils.getRandom(700.0F, 730.0F);
    }

    private Vector3 getRandomSpawnPostiton(BonusRegion region) {
        Vector3 f = new Vector3(0.0F, 0.0F, 0.0F);
        Random rand = new Random();
        f.x = region.min.x + (region.max.x - region.min.x) * rand.nextFloat();
        f.y = region.min.y + (region.max.y - region.min.y) * rand.nextFloat();
        f.z = region.max.z;
        return f;
    }

    public void updatedFund() {
        int deff = (int) this.battlefieldModel.tanksKillModel.getBattleFund() - this.prevFund;
        this.goldFund += deff;
        this.crystalFund += deff;
        if (this.goldFund >= this.nextGoldFund) {
            this.spawnBonus(BonusType.GOLD);
            this.nextGoldFund = (int) RandomUtils.getRandom(700.0F, 730.0F);
            this.goldFund = 0;
        }

        if (this.crystalFund >= 6) {
            for (int i = 0; i < (int) RandomUtils.getRandom(1.0F, 6.0F); ++i) {
                this.spawnBonus(BonusType.CRYSTAL);
            }

            this.crystalFund = 0;
        }

        this.prevFund = (int) this.battlefieldModel.tanksKillModel.getBattleFund();
    }

    @Override
    public void run() {
        if (this.battlefieldModel.battleInfo.map.crystallsRegions.size() <= 0 && this.battlefieldModel.battleInfo.map.goldsRegions.size() <= 0) {
            this.battlefieldModel = null;
        }

        while (this.battlefieldModel != null) {
            try {
                Thread.sleep(5000L);
                if (this.battlefieldModel == null || this.battlefieldModel.players == null) {
                    break;
                }

                this.spawnRandomBonus();
            } catch (InterruptedException var2) {
                Logger.log(Type.ERROR, var2.getMessage());
            }
        }

    }
}
