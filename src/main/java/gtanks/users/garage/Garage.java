package gtanks.users.garage;

import com.google.gson.*;
import gtanks.users.garage.enums.ItemType;
import gtanks.users.garage.items.Item;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@org.hibernate.annotations.Entity
@Table(
    name = "garages"
)
public class Garage implements Serializable {
    private static final Gson GSON = new Gson();
    private static final long serialVersionUID = 2342422342L;
    @Transient
    public List<Item> items = new ArrayList<>();
    @Transient
    public Item mountTurret;
    @Transient
    public Item mountHull;
    @Transient
    public Item mountColormap;
    @Id
    @GeneratedValue(
        strategy = GenerationType.IDENTITY
    )
    @Column(
        name = "uid",
        nullable = false,
        unique = true
    )
    private long id;
    @Column(
        name = "turrets",
        nullable = false
    )
    private String _json_turrets;
    @Column(
        name = "hulls",
        nullable = false
    )
    private String _json_hulls;
    @Column(
        name = "colormaps",
        nullable = false
    )
    private String _json_colormaps;
    @Column(
        name = "inventory",
        nullable = false
    )
    private String _json_inventory;
    @Column(
        name = "userid",
        nullable = false,
        unique = true
    )
    private String userId;

    public Garage() {
        Map<String, Item> items = GarageItemsLoader.items;
        this.items.add(items.get("smoky").clone());
        this.items.add(items.get("wasp").clone());
        this.items.add(items.get("green").clone());
        this.items.add(items.get("holiday").clone());
        this.mountItem("wasp_m0");
        this.mountItem("smoky_m0");
        this.mountItem("green_m0");
    }

    public boolean containsItem(String id) {
        for (Item item : this.items) {
            if (item.id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Item getItemById(String id) {
        for (Item item : this.items) {
            if (item.id.equals(id)) {
                return item;
            }
        }
        return null;
    }

    public boolean mountItem(String id) {
        Item item = this.getItemById(id.substring(0, id.length() - 3));
        if (item != null && Integer.parseInt(id.substring(id.length() - 1, id.length())) == item.modificationIndex) {
            if (item.itemType == ItemType.WEAPON) {
                this.mountTurret = item;
                return true;
            }

            if (item.itemType == ItemType.ARMOR) {
                this.mountHull = item;
                return true;
            }

            if (item.itemType == ItemType.COLOR) {
                this.mountColormap = item;
                return true;
            }
        }

        return false;
    }

    public boolean updateItem(String id) {
        String i = id.substring(0, id.length() - 3);
        Item item = getItemById(i);
        int modificationID = Integer.parseInt(id.substring(id.length() - 1));
        if (modificationID < 3 && item.modificationIndex == modificationID) {
            ++item.modificationIndex;
            item.nextPrice = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].price;
            item.nextProperty = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].propertys;
            item.nextRankId = item.modifications[item.modificationIndex + 1 != 4 ? item.modificationIndex + 1 : item.modificationIndex].rank;
            this.replaceItems(getItemById(i), item);
            return true;
        } else {
            return false;
        }
    }

    public boolean buyItem(String id, int count) {
        Item temp = GarageItemsLoader.items.get(id.substring(0, id.length() - 3));
        if (temp.specialItem) {
            return false;
        } else {
            Item item = temp.clone();
            if (!this.items.contains(this.getItemById(id))) {
                if (item.isInventory) {
                    item.count += count;
                }

                this.items.add(item);
                return true;
            } else if (item.isInventory) {
                Item fromUser = this.getItemById(id);
                fromUser.count += count;
                return true;
            } else {
                return false;
            }
        }
    }

    public Item buyItem(String id, int count, int nul) {
        id = id.substring(0, id.length() - 3);
        Item temp = GarageItemsLoader.items.get(id);
        if (temp.specialItem) {
            return null;
        } else {
            Item item = temp.clone();
            if (!this.items.contains(this.getItemById(id))) {
                if (item.itemType == ItemType.INVENTORY) {
                    item.count += count;
                }

                this.items.add(item);
                return item;
            } else if (item.itemType == ItemType.INVENTORY) {
                Item fromUser = this.getItemById(id);
                fromUser.count += count;
                return fromUser;
            } else {
                return null;
            }
        }
    }

    private void replaceItems(Item old, Item newItem) {
        if (this.items.contains(old)) {
            this.items.set(this.items.indexOf(old), newItem);
        }

    }

    public List<Item> getInventoryItems() {
        List<Item> items = new ArrayList<>();

        for (Item item : this.items) {
            if (item.itemType == ItemType.INVENTORY) {
                items.add(item);
            }
        }

        return items;
    }

    public void parseJSONData() {
        JsonObject hulls = new JsonObject();
        JsonArray _hulls = new JsonArray();
        JsonObject colormaps = new JsonObject();
        JsonArray _colormaps = new JsonArray();
        JsonObject turrets = new JsonObject();
        JsonArray _turrets = new JsonArray();
        JsonObject inventory_items = new JsonObject();
        JsonArray _inventory = new JsonArray();

        for (Item item : this.items) {
            JsonObject i = item.serialize(this);
            switch (item.itemType) {
                case ARMOR: {
                    _hulls.add(i);
                    break;
                }
                case COLOR: {
                    _colormaps.add(i);
                    break;
                }
                case WEAPON: {
                    _turrets.add(i);
                    break;
                }
                case INVENTORY: {
                    _inventory.add(i);
                    break;
                }
            }
        }

        hulls.add("hulls", _hulls);
        colormaps.add("colormaps", _colormaps);
        turrets.add("turrets", _turrets);
        inventory_items.add("inventory", _inventory);
        this._json_colormaps = GSON.toJson(colormaps);
        this._json_hulls = GSON.toJson(hulls);
        this._json_turrets = GSON.toJson(turrets);
        this._json_inventory = GSON.toJson(inventory_items);
    }

    public void unparseJSONData() throws JsonParseException {
        this.items.clear();
        JsonObject turrets = GSON.fromJson(_json_turrets, JsonObject.class);
        JsonObject colormaps = GSON.fromJson(_json_colormaps, JsonObject.class);
        JsonObject hulls = GSON.fromJson(_json_hulls, JsonObject.class);
        JsonObject inventory;
        if (this._json_inventory != null && !this._json_inventory.isEmpty()) {
            inventory = GSON.fromJson(_json_inventory, JsonObject.class);
        } else {
            inventory = null;
        }

        for (JsonElement item : turrets.getAsJsonArray("turrets")) {
            JsonObject _item = item.getAsJsonObject();
            Item i = GarageItemsLoader.items.get(_item.get("id").getAsString()).clone();
            i.modificationIndex = _item.get("modification").getAsInt();
            i.nextRankId = i.modifications[i.modificationIndex == 3 ? 3 : i.modificationIndex + 1].rank;
            i.nextPrice = i.modifications[i.modificationIndex == 3 ? 3 : i.modificationIndex + 1].price;
            this.items.add(i);
            if (_item.get("mounted").getAsBoolean()) {
                this.mountTurret = i;
            }
        }

        for (JsonElement item : colormaps.getAsJsonArray("colormaps")) {
            JsonObject _item = item.getAsJsonObject();
            Item i = GarageItemsLoader.items.get(_item.get("id").getAsString()).clone();
            i.modificationIndex = _item.get("modification").getAsInt();
            this.items.add(i);
            if (_item.get("mounted").getAsBoolean()) {
                this.mountColormap = i;
            }
        }

        for (JsonElement item : hulls.getAsJsonArray("hulls")) {
            JsonObject _item = item.getAsJsonObject();
            Item i = GarageItemsLoader.items.get(_item.get("id").getAsString()).clone();
            i.modificationIndex = _item.get("modification").getAsInt();
            i.nextRankId = i.modifications[i.modificationIndex == 3 ? 3 : i.modificationIndex + 1].rank;
            i.nextPrice = i.modifications[i.modificationIndex == 3 ? 3 : i.modificationIndex + 1].price;
            this.items.add(i);
            if (_item.get("mounted").getAsBoolean()) {
                this.mountHull = i;
            }
        }

        if (inventory != null) {
            for (JsonElement item : inventory.getAsJsonArray("inventory")) {
                JsonObject _item = item.getAsJsonObject();
                Item i = GarageItemsLoader.items.get(_item.get("id").getAsString()).clone();
                i.modificationIndex = 0;
                i.count = _item.get("count").getAsInt();
                if (i.itemType == ItemType.INVENTORY) {
                    this.items.add(i);
                }
            }
        }

    }

    public String get_json_turrets() {
        return this._json_turrets;
    }

    public void set_json_turrets(String _json_turrets) {
        this._json_turrets = _json_turrets;
    }

    public String get_json_hulls() {
        return this._json_hulls;
    }

    public void set_json_hulls(String _json_hulls) {
        this._json_hulls = _json_hulls;
    }

    public String get_json_colormaps() {
        return this._json_colormaps;
    }

    public void set_json_colormaps(String _json_colormaps) {
        this._json_colormaps = _json_colormaps;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String get_json_inventory() {
        return this._json_inventory;
    }

    public void set_json_inventory(String _json_inventory) {
        this._json_inventory = _json_inventory;
    }
}
