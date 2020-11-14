package gtanks.rmi.payments.kit;

public class KitItem {
    private KitItemType type;
    private String itemId;
    private int count;

    public KitItem(KitItemType type, String itemId, int count) {
        this.type = type;
        this.itemId = itemId;
        this.count = count;
    }

    public KitItemType getType() {
        return this.type;
    }

    public void setType(KitItemType type) {
        this.type = type;
    }

    public String getItemId() {
        return this.itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
