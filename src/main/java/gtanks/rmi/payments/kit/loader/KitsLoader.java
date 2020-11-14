package gtanks.rmi.payments.kit.loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gtanks.logger.Logger;
import gtanks.rmi.payments.kit.Kit;
import gtanks.rmi.payments.kit.KitItem;
import gtanks.rmi.payments.kit.KitItemType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitsLoader {
    private static final Map<String, Kit> kits = new HashMap<>();
    private static final Gson GSON = new Gson();

    public static void load(String config) throws FileNotFoundException, IOException, JsonParseException {
        load(new File(config));
    }

    public static void load(File file) throws FileNotFoundException, IOException, JsonParseException {
        JsonObject json = GSON.fromJson(new FileReader(file), JsonObject.class);

        for (JsonElement _kit : json.getAsJsonArray("kits")) {
            JsonObject kit = _kit.getAsJsonObject();
            String kitId = kit.get("kit_id").getAsString();
            Logger.log("Load " + kit.get("name") + "(" + kitId + ") kit...");
            List<KitItem> kitItems = new ArrayList<>();

            for (JsonElement _item : kit.getAsJsonArray("items")) {
                JsonObject item = _item.getAsJsonObject();
                KitItemType type = KitItemType.valueOf(item.get("type").getAsString());
                String itemId = item.get("item_id").getAsString();
                int count = item.get("count").getAsInt();
                kitItems.add(new KitItem(type, itemId, count));
            }

            kits.put(kitId, new Kit(kitItems, kit.get("price").getAsInt()));
        }

    }
}
