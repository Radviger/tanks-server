package gtanks.users.garage.items;

import com.google.gson.JsonObject;
import gtanks.StringUtils;
import gtanks.system.localization.strings.LocalizedString;
import gtanks.users.garage.Garage;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.modification.ModificationInfo;

public class Item {
    public String id;
    public LocalizedString description;
    public boolean isInventory;
    public int index;
    public PropertyItem[] properties;
    public ItemType itemType;
    public int modificationIndex;
    public LocalizedString name;
    public PropertyItem[] nextProperty;
    public int nextPrice;
    public int nextRankId;
    public int price;
    public int rankId;
    public ModificationInfo[] modifications;
    public boolean specialItem;
    public int count;

    public Item(String id, LocalizedString description, boolean isInventory, int index, PropertyItem[] properties, ItemType weapon, int modificationIndex, LocalizedString name, PropertyItem[] nextProperty, int nextPrice, int nextRankId, int price, int rankId, ModificationInfo[] modifications, boolean specialItem, int count) {
        this.id = id;
        this.description = description;
        this.isInventory = isInventory;
        this.index = index;
        this.properties = properties;
        this.itemType = weapon;
        this.modificationIndex = modificationIndex;
        this.name = name;
        this.nextProperty = nextProperty;
        this.nextPrice = nextPrice;
        this.nextRankId = nextRankId;
        this.price = price;
        this.rankId = rankId;
        this.modifications = modifications;
        this.specialItem = specialItem;
        this.count = count;
    }

    public String getId() {
        return StringUtils.concatStrings(this.id, "_m", String.valueOf(this.modificationIndex));
    }

    @Override
    public Item clone() {
        return new Item(this.id, this.description, this.isInventory, this.index, this.properties, this.itemType, this.modificationIndex, this.name, this.nextProperty, this.nextPrice, this.nextRankId, this.price, this.rankId, this.modifications, this.specialItem, this.count);
    }

    public JsonObject serialize(Garage garage) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        switch (itemType) {
            case ARMOR: {
                obj.addProperty("modification", modificationIndex);
                obj.addProperty("mounted", this == garage.mountHull);
                break;
            }
            case COLOR: {
                obj.addProperty("modification", modificationIndex);
                obj.addProperty("mounted", this == garage.mountColormap);
                break;
            }
            case WEAPON: {
                obj.addProperty("modification", modificationIndex);
                obj.addProperty("mounted", this == garage.mountTurret);
                break;
            }
            case INVENTORY: {
                obj.addProperty("count", count);
                break;
            }
        }
        return obj;
    }
}
