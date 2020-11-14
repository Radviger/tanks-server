package gtanks.groups;

import java.util.Collections;
import java.util.List;

public class UserGroup {
    private final List<String> availableChatCommands;
    private String groupName;

    public UserGroup(List<String> availableChatCommands) {
        this.availableChatCommands = Collections.unmodifiableList(availableChatCommands);
    }

    public boolean isCommandAvailable(String command) {
        return this.availableChatCommands.contains(command);
    }

    public String getGroupName() {
        return this.groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User group: ").append(this.getGroupName()).append(". ").append("Available chat commands:\n");

        for (String command : this.availableChatCommands) {
            sb.append("        ---- /").append(command).append('\n');
        }

        return sb.toString();
    }
}
