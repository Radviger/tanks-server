package gtanks.system.localization.strings;

import gtanks.system.localization.Localization;

import java.util.HashMap;
import java.util.Map;

public class LocalizedString {
    private final Map<Localization, String> localization = new HashMap<>();

    protected LocalizedString(String ruVersion, String enVersion) {
        this.localization.put(Localization.RU, ruVersion);
        this.localization.put(Localization.EN, enVersion);
    }

    public String localize(Localization loc) {
        String string = this.localization.get(loc);
        return string == null ? "null" : string;
    }
}
