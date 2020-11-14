package gtanks.lobby.top;

import gtanks.logger.Logger;
import gtanks.users.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public enum HallOfFame {
    INSTANCE;

    private List<User> top = new ArrayList<>(100);

    public void addUser(User user) {
        Logger.log("User " + user.getNickname() + " has been added to top. " + (this.top.add(user) ? "DONE" : "ERROR"));
    }

    public void removeUser(User user) {
        Logger.log("User " + user.getNickname() + " has been removed of top. " + (this.top.remove(user) ? "DONE" : "ERROR"));
    }

    public void initHallFromCollection(Collection<User> collection) {
        this.top = new ArrayList<>(collection);
    }

    public List<User> getData() {
        return this.top;
    }
}
