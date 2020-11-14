package gtanks.groups;

import gtanks.logger.Logger;
import gtanks.users.TypeUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGroupsLoader {
    private static final String FILE_FORMAT = ".group";
    private static final Map<TypeUser, UserGroup> usersGroups = new HashMap<>();

    public static UserGroup getUserGroup(TypeUser typeUser) {
        return usersGroups.get(typeUser);
    }

    public static void load(String root) {
        File folder = new File(root);

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith(FILE_FORMAT)) {
                parseFile(file);
            }
        }
    }

    private static void parseFile(File file) {
        try {
            List<String> availableChatCommands = Files.readAllLines(file.toPath());
            TypeUser typeUser = getTypeUser(file.getName().split(FILE_FORMAT)[0]);
            UserGroup userGroup = new UserGroup(availableChatCommands);
            userGroup.setGroupName(typeUser.name().toLowerCase());
            usersGroups.put(typeUser, userGroup);
            Logger.log("User group " + typeUser.toString() + " has been inited.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TypeUser getTypeUser(String name) {
        for (TypeUser type : TypeUser.values()) {
            if (type.name().toLowerCase().equals(name)) {
                return type;
            }
        }

        return null;
    }
}
