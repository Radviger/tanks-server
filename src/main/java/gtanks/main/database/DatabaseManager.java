package gtanks.main.database;

import gtanks.logger.remote.LogObject;
import gtanks.main.netty.blackip.BlackIP;
import gtanks.rmi.payments.mapping.Payment;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.karma.Karma;

import java.util.List;

public interface DatabaseManager {
    User getUserById(String var1);

    User getUserByIdFromCache(String var1);

    Garage getGarageByUser(User var1);

    Karma getKarmaByUser(User var1);

    BlackIP getBlackIPbyAddress(String var1);

    Payment getPaymentById(long var1);

    List<LogObject> collectLogs();

    List<Garage> collectGarages();

    void update(User var1);

    void update(Garage var1);

    void update(Karma var1);

    void update(Payment var1);

    void register(User var1);

    void register(BlackIP var1);

    void register(LogObject var1);

    void unregister(BlackIP var1);

    void cache(User var1);

    void uncache(String var1);

    void initHallOfFame();

    boolean contains(String var1);

    int getCacheSize();
}
