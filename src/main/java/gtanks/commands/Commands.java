package gtanks.commands;

import org.apache.commons.lang3.ArrayUtils;

public class Commands {
    public static final String SPLITTER_ARGS = ";";

    public static Command decrypt(String crypt) {
        String[] temp = crypt.split(SPLITTER_ARGS);
        Type type;
        try {
            type = Type.valueOf(temp[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            type = Type.UNKNOWN;
        }

        String[] args = ArrayUtils.removeElement(temp, temp[0]);
        return new Command(type, args);
    }
}
