package gtanks.rmi.payments.kit;

import gtanks.users.User;

import java.util.List;

public class Kit {
    private final List<KitItem> kitItems;
    private int price;

    public Kit(List<KitItem> kitItems, int price) {
        this.kitItems = kitItems;
        this.setPrice(price);
    }

    public List<KitItem> getKitItems() {
        return this.kitItems;
    }

    public void enroll(User user) {
    }

    public int getPrice() {
        return this.price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
