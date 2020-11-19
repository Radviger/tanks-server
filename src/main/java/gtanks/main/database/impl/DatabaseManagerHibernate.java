package gtanks.main.database.impl;

import gtanks.lobby.top.HallOfFame;
import gtanks.logger.Logger;
import gtanks.logger.Type;
import gtanks.logger.remote.LogObject;
import gtanks.main.database.DatabaseManager;
import gtanks.main.netty.blackip.BlackIP;
import gtanks.rmi.payments.mapping.Payment;
import gtanks.services.hibernate.Database;
import gtanks.users.TypeUser;
import gtanks.users.User;
import gtanks.users.garage.Garage;
import gtanks.users.karma.Karma;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public enum DatabaseManagerHibernate implements DatabaseManager {
    INSTANCE;

    private final Map<String, User> cache = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    public void register(User user) {
        this.configureNewAccount(user);
        Garage garage = new Garage();
        garage.parseJSONData();
        garage.setUserId(user.getNickname());
        Karma emptyKarma = new Karma();
        emptyKarma.setUserId(user.getNickname());

        Database.execute(session -> {
            session.save(user);
            session.save(garage);
            session.save(emptyKarma);
        });
    }

    @Override
    public void update(Karma karma) {
        User user = this.cache.get(karma.getUserId());

        if (user != null) {
            user.setKarma(karma);
        }

        Database.execute(session -> session.update(karma));
    }

    @Override
    public void update(Garage garage) {
        Database.execute(session -> session.update(garage));
    }

    @Override
    public void update(User user) {
        Database.execute(session -> session.update(user));
    }

    @Override
    public User getUserById(String nickname) {
        User user = getUserByIdFromCache(nickname);
        if (user == null) {
            return Database.query(session -> {
                Query<User> query = session.createQuery("FROM User U WHERE U.nickname = :nickname", User.class);
                query.setParameter("nickname", nickname, StringType.INSTANCE);
                return query.uniqueResult();
            });
        }
        return user;
    }

    @Override
    public void cache(User user) {
        if (user == null) {
            Logger.log(Type.ERROR, "DatabaseManagerImpl::cache user is null!");
        } else {
            this.cache.put(user.getNickname(), user);
        }
    }

    @Override
    public void uncache(String id) {
        this.cache.remove(id);
    }

    @Override
    public User getUserByIdFromCache(String nickname) {
        return this.cache.get(nickname);
    }

    @Override
    public boolean contains(String nickname) {
        return this.getUserById(nickname) != null;
    }

    public void configureNewAccount(User user) {
        user.setCrystall(5);
        user.setNextScore(100);
        user.setType(TypeUser.DEFAULT);
        user.setEmail(null);
    }

    @Override
    public int getCacheSize() {
        return this.cache.size();
    }

    @Override
    public void initHallOfFame() {
        Database.execute(session -> {
            List<User> users = session.createCriteria(User.class).list();
            HallOfFame.INSTANCE.initHallFromCollection(users);
        });
    }

    @Override
    public Garage getGarageByUser(User user) {
        return Database.query(session -> {
            Query<Garage> query = session.createQuery("FROM Garage G WHERE G.userId = :nickname", Garage.class);
            query.setParameter("nickname", user.getNickname(), StringType.INSTANCE);
            return query.uniqueResult();
        });
    }

    @Override
    public Karma getKarmaByUser(User user) {
        if (user.getKarma() != null) {
            return user.getKarma();
        } else {
            return Database.query(session -> {
                Query<Karma> query = session.createQuery("FROM Karma K WHERE K.userId = :nickname", Karma.class);
                query.setParameter("nickname", user.getNickname(), StringType.INSTANCE);
                return query.uniqueResult();
            });
        }
    }

    @Override
    public BlackIP getBlackIPbyAddress(String address) {
        return Database.query(session -> {
            Query<BlackIP> query = session.createQuery("FROM BlackIP B WHERE B.ip = :ip", BlackIP.class);
            query.setParameter("ip", address, StringType.INSTANCE);
            return query.uniqueResult();
        });
    }

    @Override
    public void register(BlackIP blackIP) {
        if (getBlackIPbyAddress(blackIP.getIp()) == null) {
            Database.execute(session -> session.saveOrUpdate(blackIP));
        }
    }

    @Override
    public void unregister(BlackIP blackIP) {
        Database.execute(session -> {
            Query<?> query = session.createQuery("DELETE FROM BlackIP B WHERE B.ip = :ip");
            query.setParameter("ip", blackIP.getIp(), StringType.INSTANCE);
            query.executeUpdate();
        });
    }

    @Override
    public void register(LogObject log) {
        Database.execute(session -> session.save(log));
    }

    @Override
    public List<LogObject> collectLogs() {
        return Database.query(session -> session.createCriteria(LogObject.class).list());
    }

    @Override
    public Payment getPaymentById(long paymentId) {
        return Database.query(session -> {
            Query<Payment> query = session.createQuery("FROM Payment p WHERE p.idPayment = :pid", Payment.class);
            query.setParameter("pid", paymentId, LongType.INSTANCE);
            return query.uniqueResult();
        });
    }

    @Override
    public void update(Payment payment) {
        Database.execute(session -> session.update(payment));
    }

    @Override
    public List<Garage> collectGarages() {
        return Database.query(session -> session.createCriteria(Garage.class).list());
    }
}
