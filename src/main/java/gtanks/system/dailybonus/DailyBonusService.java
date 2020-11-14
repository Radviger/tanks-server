package gtanks.system.dailybonus;

import gtanks.lobby.LobbyManager;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.system.dailybonus.crystalls.CrystalsBonusModel;
import gtanks.system.dailybonus.ui.DailyBonusUIModel;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.garage.GarageItemsLoader;
import gtanks.users.garage.items.Item;

import java.util.*;

public enum DailyBonusService {
    INSTANCE;

    public static final String[] SUPPLIES_IDS = new String[]{"armor", "double_damage", "n2o"};

    private static final DatabaseManager databaseManager = DatabaseManagerHibernate.INSTANCE;
    private static final Map<LobbyManager, Data> waitingUsers = new HashMap<>();
    private static final DailyBonusUIModel uiModel = new DailyBonusUIModel();
    private static final CrystalsBonusModel crystalsBonus = new CrystalsBonusModel();
    private static final Random random = new Random();

    public void userInitialized(LobbyManager lobby) {
        User user = lobby.getLocalUser();
        if (user.getRang() + 1 > 2 && this.canGetBonus(user)) {
            int fund = (int) (((double) (user.getRang() + 1) - 1.75D) * 2.4D) * 5;
            if (fund > 0) {
                DailyBonusService.Data bonusData = new Data();
                List<BonusListItem> bonusList = bonusData.bonusList;
                int rankFirstAid = GarageItemsLoader.items.get("health").rankId;
                int itemCrystalPrice = GarageItemsLoader.items.get("health").price;
                int countFirstAid = fund / itemCrystalPrice / 2;
                itemCrystalPrice = GarageItemsLoader.items.get("mine").price;
                int countMine = fund / itemCrystalPrice / 2;
                int rankMine = GarageItemsLoader.items.get("mine").rankId;
                if ((double) random.nextFloat() < 0.1D) {
                    bonusData.type = 1;
                } else {
                    bonusData.type = 3;
                    int count;
                    int price;
                    Item bonus;
                    int nextInt;
                    if ((double) random.nextFloat() < 0.3D && countFirstAid > 0 && user.getRang() >= rankFirstAid) {
                        bonus = GarageItemsLoader.items.get("health");
                        price = bonus.price;
                        count = fund / price / 2 + 1;
                    } else if ((double) random.nextFloat() < 0.3D && countMine > 0 && user.getRang() >= rankMine) {
                        bonus = GarageItemsLoader.items.get("mine");
                        price = bonus.price;
                        count = fund / price / 2 + 1;
                    } else {
                        nextInt = random.nextInt(3);
                        bonus = GarageItemsLoader.items.get(SUPPLIES_IDS[nextInt]);
                        price = bonus.price;
                        count = fund / price / 2;
                    }

                    bonusList.add(new BonusListItem(bonus, count));
                    fund -= price * count;
                    nextInt = random.nextInt(3);
                    bonus = GarageItemsLoader.items.get(SUPPLIES_IDS[nextInt]);
                    price = bonus.price;
                    if (bonusList.get(0).getBonus().equals(bonus)) {
                        bonusList.get(0).addCount(fund / price);
                    } else {
                        bonusList.add(new BonusListItem(bonus, fund / price));
                    }
                }

                waitingUsers.put(lobby, bonusData);
                Garage garage = user.getGarage();

                for (BonusListItem item : bonusList) {
                    Item bonusItem = garage.getItemById(item.getBonus().id);
                    if (bonusItem == null) {
                        bonusItem = GarageItemsLoader.items.get(item.getBonus().id).clone();
                        garage.items.add(bonusItem);
                    }
                    bonusItem.count += item.getCount();
                }

                garage.parseJSONData();
                databaseManager.update(garage);
            }
        }

    }

    public void userLoaded(LobbyManager lobby) {
        DailyBonusService.Data data = waitingUsers.remove(lobby);
        if (data != null) {
            if (data.type == 1) {
                crystalsBonus.applyBonus(lobby);
                uiModel.showCrystalls(lobby, crystalsBonus.getBonus(lobby.getLocalUser().getRang()));
            } else if (data.type == 3) {
                uiModel.showBonuses(lobby, data.bonusList);
            }

            saveLastDate(lobby.getLocalUser());
        }
    }

    public boolean canGetBonus(User user) {
        if (user == null) {
            return false;
        } else {
            boolean result = false;
            Date lastDate = user.getLastIssueBonus();
            Date now = new Date(System.currentTimeMillis() - 14400000L);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTime(now);
            Calendar lastCal = Calendar.getInstance();
            if (lastDate != null) {
                lastCal.setTime(lastDate);
            }

            if (lastDate == null || nowCal.get(5) > lastCal.get(5) || nowCal.get(2) > lastCal.get(2)) {
                result = true;
            }

            return result;
        }
    }

    private void saveLastDate(User user) {
        Date now = new Date(System.currentTimeMillis() - 14400000L);
        user.setLastIssueBonus(now);
        databaseManager.update(user);
    }

    private static class Data {
        public int type = 0;
        public List<BonusListItem> bonusList = new ArrayList<>();
    }
}
