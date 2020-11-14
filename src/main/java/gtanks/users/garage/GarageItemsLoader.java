package gtanks.users.garage;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gtanks.battles.tanks.colormaps.Colormap;
import gtanks.battles.tanks.colormaps.ColormapsFactory;
import gtanks.system.localization.strings.LocalizedString;
import gtanks.system.localization.strings.StringsLocalizationBundle;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.enums.PropertyType;
import gtanks.users.garage.items.Item;
import gtanks.users.garage.items.PropertyItem;
import gtanks.users.garage.items.modification.ModificationInfo;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class GarageItemsLoader {
    private static final Gson GSON = new Gson();
    public static Map<String, Item> items = new HashMap<>();
    private static int index = 1;

    public static void loadFromConfig(String turrets, String armor, String color, String inventory, String subscription) {
        for (int i = 0; i < 5; ++i) {
            File file = new File(i == 0 ? inventory : (i == 1 ? turrets : (i == 2 ? armor : color)));
            try {
                try (Reader reader = new FileReader(file)) {
                    parseAndInitItems(reader, i == 0 ? ItemType.INVENTORY : (i == 1 ? ItemType.WEAPON : (i == 2 ? ItemType.ARMOR : ItemType.COLOR)));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error loading config " + file, e);
            }
        }
    }

    private static LocalizedString getName(JsonObject item) {
        JsonElement ru = item.get("name_ru");
        JsonElement en = item.get("name_ru");
        if (ru != null && en != null) {
            return StringsLocalizationBundle.registerString(ru.getAsString(), en.getAsString());
        } else {
            JsonElement generic = item.get("name");
            return StringsLocalizationBundle.registerString(generic.getAsString(), generic.getAsString());
        }
    }

    private static LocalizedString getDesc(JsonObject item) {
        JsonElement ru = item.get("description_ru");
        JsonElement en = item.get("description_ru");
        if (ru != null && en != null) {
            return StringsLocalizationBundle.registerString(ru.getAsString(), en.getAsString());
        } else {
            JsonElement generic = item.get("description");
            return StringsLocalizationBundle.registerString(generic.getAsString(), generic.getAsString());
        }
    }

    private static void parseAndInitItems(Reader json, ItemType typeItem) {
        JsonObject obj = GSON.fromJson(json, JsonObject.class);
        JsonArray items = obj.getAsJsonArray("items");

        for (int i = 0; i < items.size(); ++i) {
            JsonObject item = items.get(i).getAsJsonObject();
            LocalizedString name = getName(item);
            LocalizedString description = getDesc(item);
            String id = item.get("id").getAsString();
            int priceM0 = Integer.parseInt(item.get("price_m0").getAsString());
            int priceM1 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("price_m1").getAsString()) : priceM0;
            int priceM2 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("price_m2").getAsString()) : priceM0;
            int priceM3 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("price_m3").getAsString()) : priceM0;
            int rangM0 = Integer.parseInt(item.get("rang_m0").getAsString());
            int rangM1 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("rang_m1").getAsString()) : rangM0;
            int rangM2 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("rang_m2").getAsString()) : rangM0;
            int rangM3 = typeItem != ItemType.COLOR && typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? Integer.parseInt(item.get("rang_m3").getAsString()) : rangM0;
            PropertyItem[] propertysItemM0 = null;
            PropertyItem[] propertysItemM1 = null;
            PropertyItem[] propertysItemM2 = null;
            PropertyItem[] propertysItemM3 = null;
            int countModification = typeItem == ItemType.COLOR ? 1 : (typeItem != ItemType.INVENTORY && typeItem != ItemType.PLUGIN ? 4 : item.get("count_modifications").getAsInt());

            for (int m = 0; m < countModification; ++m) {
                JsonArray propertys = item.getAsJsonArray("propertys_m" + m);
                PropertyItem[] property = new PropertyItem[propertys.size()];

                for (int p = 0; p < propertys.size(); ++p) {
                    JsonObject prop = propertys.get(p).getAsJsonObject();
                    property[p] = new PropertyItem(getType(prop.get("type").getAsString()), prop.get("value").getAsString());
                }

                switch (m) {
                    case 0:
                        propertysItemM0 = property;
                        break;
                    case 1:
                        propertysItemM1 = property;
                        break;
                    case 2:
                        propertysItemM2 = property;
                        break;
                    case 3:
                        propertysItemM3 = property;
                }
            }

            if (typeItem == ItemType.COLOR || typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN) {
                propertysItemM1 = propertysItemM0;
                propertysItemM2 = propertysItemM0;
                propertysItemM3 = propertysItemM0;
            }

            ModificationInfo[] mods = new ModificationInfo[4];
            mods[0] = new ModificationInfo(id + "_m0", priceM0, rangM0);
            mods[0].propertys = propertysItemM0;
            mods[1] = new ModificationInfo(id + "_m1", priceM1, rangM1);
            mods[1].propertys = propertysItemM1;
            mods[2] = new ModificationInfo(id + "_m2", priceM2, rangM2);
            mods[2].propertys = propertysItemM2;
            mods[3] = new ModificationInfo(id + "_m3", priceM3, rangM3);
            mods[3].propertys = propertysItemM3;
            boolean specialItem = item.has("special_item") && item.get("special_item").getAsBoolean();
            GarageItemsLoader.items.put(id, new Item(id, description, typeItem == ItemType.INVENTORY || typeItem == ItemType.PLUGIN, index, propertysItemM0, typeItem, 0, name, propertysItemM1, priceM1, rangM1, priceM0, rangM0, mods, specialItem, 0));
            ++index;
            if (typeItem == ItemType.COLOR) {
                ColormapsFactory.addColormap(id + "_m0", new Colormap() {
                    {
                        for (PropertyItem _property : mods[0].propertys) {
                            this.addResistance(ColormapsFactory.getResistanceType(_property.property), GarageItemsLoader.getInt(_property.value.replace("%", "")));
                        }
                    }
                });
            }
        }
    }

    private static int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private static PropertyType getType(String s) {
        for (PropertyType type : PropertyType.values()) {
            if (type.toString().equals(s)) {
                return type;
            }
        }

        return null;
    }
}
