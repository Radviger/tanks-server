package gtanks.rmi.payments.impl;

import gtanks.lobby.LobbyManager;
import gtanks.logger.Logger;
import gtanks.main.database.DatabaseManager;
import gtanks.main.database.impl.DatabaseManagerHibernate;
import gtanks.rmi.payments.RMIPaymentCallback;
import gtanks.rmi.payments.mapping.Payment;
import gtanks.services.LobbyServices;
import gtanks.services.TanksServices;
import gtanks.users.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RMIPaymentCallbackImpl extends UnicastRemoteObject implements RMIPaymentCallback {
    private static final long serialVersionUID = 13322234112L;
    private static final int RUB_COURSE = 3;
    private final DatabaseManager database = DatabaseManagerHibernate.INSTANCE;
    private final TanksServices tanksServices = TanksServices.INSTANCE;
    private final LobbyServices lobbyServices = LobbyServices.INSTANCE;

    public RMIPaymentCallbackImpl() throws RemoteException {
        Logger.log("RMI Payment service is runned!");
    }

    @Override
    public boolean paymentAccepted(long idPayment, String userId, int sum) throws RemoteException {
        Payment payment = this.database.getPaymentById(idPayment);
        if (payment == null) {
            return false;
        } else if (payment.getStatus() == 1) {
            return false;
        } else if (payment.getSum() != sum) {
            return false;
        } else {
            User user = this.database.getUserById(userId);
            if (user == null) {
                return false;
            } else {
                user.addCrystall(sum * RUB_COURSE);
                payment.setStatus((byte) 1);
                LobbyManager userLobby = this.lobbyServices.getLobbyByNick(userId);
                if (userLobby != null) {
                    this.tanksServices.dummyAddCrystal(userLobby, sum * RUB_COURSE);
                }

                this.database.update(user);
                this.database.update(payment);
                return true;
            }
        }
    }
}
