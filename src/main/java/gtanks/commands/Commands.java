package gtanks.commands;

import org.apache.commons.lang3.ArrayUtils;

public class Commands {
    public static final String SPLITTER_ARGS = ";";

    public static Command decrypt(String crypt) {
        Type type;
        String[] temp;
        label47:
        {
            temp = crypt.split(SPLITTER_ARGS);
            String var3;
            switch ((var3 = temp[0]).hashCode()) {
                case -1396158280:
                    if (var3.equals("battle")) {
                        type = Type.BATTLE;
                        break label47;
                    }
                    break;
                case -1350309703:
                    if (var3.equals("registration")) {
                        type = Type.REGISTRATION;
                        break label47;
                    }
                    break;
                case -1253090521:
                    if (var3.equals("garage")) {
                        type = Type.GARAGE;
                        break label47;
                    }
                    break;
                case -887328209:
                    if (var3.equals("system")) {
                        type = Type.SYSTEM;
                        break label47;
                    }
                    break;
                case 3005864:
                    if (var3.equals("auth")) {
                        type = Type.AUTH;
                        break label47;
                    }
                    break;
                case 3052376:
                    if (var3.equals("chat")) {
                        type = Type.CHAT;
                        break label47;
                    }
                    break;
                case 3441010:
                    if (var3.equals("ping")) {
                        type = Type.PING;
                        break label47;
                    }
                    break;
                case 103144406:
                    if (var3.equals("lobby")) {
                        type = Type.LOBBY;
                        break label47;
                    }
                    break;
                case 820078113:
                    if (var3.equals("lobby_chat")) {
                        type = Type.LOBBY_CHAT;
                        break label47;
                    }
            }

            type = Type.UNKNOWN;
        }

        String[] args = ArrayUtils.removeElement(temp, temp[0]);
        return new Command(type, args);
    }
}
