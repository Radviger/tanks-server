package gtanks.system.dailybonus.ui;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gtanks.commands.Type;
import gtanks.lobby.LobbyManager;
import gtanks.system.dailybonus.BonusListItem;

import java.util.List;

public class DailyBonusUIModel {
    private static final Gson GSON = new Gson();

    public void showBonuses(LobbyManager lobby, List<BonusListItem> bonusesData) {
        JsonObject json = new JsonObject();
        JsonArray items = new JsonArray();

        for (BonusListItem item : bonusesData) {
            JsonObject _item = new JsonObject();
            _item.addProperty("id", item.getBonus().id);
            _item.addProperty("count", item.getCount());
            items.add(_item);
        }

        json.add("items", items);
        lobby.send(Type.LOBBY, "show_bonuses", GSON.toJson(json));
    }

    public void showCrystalls(LobbyManager lobby, int count) {
        lobby.send(Type.LOBBY, "show_crystalls", String.valueOf(count));
    }

    public void showNoSupplies() {
    }
}
