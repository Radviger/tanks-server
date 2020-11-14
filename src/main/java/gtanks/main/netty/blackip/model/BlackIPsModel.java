package gtanks.main.netty.blackip.model;

import gtanks.collections.FastHashMap;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.main.netty.blackip.BlackIP;

import java.util.Map;

public class BlackIPsModel {
    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private Map<String, Boolean> cache = new FastHashMap<>();

    public boolean contains(String ip) {
        if (this.cache.containsKey(ip)) {
            return true;
        } else {
            BlackIP obj = this.database.getBlackIPbyAddress(ip);
            boolean contains_ = obj != null;
            if (contains_) {
                this.cache.put(ip, true);
            }

            return contains_;
        }
    }

    public void block(String ip) {
        BlackIP blackIP = new BlackIP();
        blackIP.setIp(ip);
        this.database.register(blackIP);
    }

    public void unblock(String ip) {
        BlackIP blackIP = new BlackIP();
        blackIP.setIp(ip);
        this.database.unregister(blackIP);
    }
}
