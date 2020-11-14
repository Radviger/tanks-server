package gtanks.services.hibernate;

import gtanks.logger.remote.RemoteDatabaseLogger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.function.Consumer;
import java.util.function.Function;

public class Database {
    private static SessionFactory sessionFactory = null;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }
        }
        return sessionFactory;
    }

    public static void execute(Consumer<Session> actor) {
        query(s -> {
            actor.accept(s);
            return (Void) null;
        });
    }

    public static <R> R query(Function<Session, R> actor) {
        Transaction tx = null;

        try {
            Session session = Database.getSessionFactory().getCurrentSession();
            tx = session.beginTransaction();
            R result = actor.apply(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            RemoteDatabaseLogger.error(e);
            throw new RuntimeException(e);
        }
    }
}
