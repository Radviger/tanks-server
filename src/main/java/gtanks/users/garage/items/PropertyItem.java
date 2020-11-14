package gtanks.users.garage.items;

import gtanks.users.garage.enums.PropertyType;

public class PropertyItem {
    public PropertyType property;
    public String value;

    public PropertyItem(PropertyType property, String value) {
        this.property = property;
        this.value = value;
    }
}
