package gtanks.rmi.server;

import gtanks.logger.Logger;
import gtanks.rmi.payments.impl.RMIPaymentCallbackImpl;
import gtanks.rmi.tools.impl.ServerToolsImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIServer {
    public static void run() {
        System.setProperty("java.rmi.server.hostname", "localhost");
        (new Thread(() -> {
            try {
                RMIPaymentCallbackImpl payment = new RMIPaymentCallbackImpl();
                ServerToolsImpl tools = new ServerToolsImpl();
                Registry registry = LocateRegistry.createRegistry(1099);
                registry.bind("rmi://localhost:5252/payment", payment);
                registry.bind("rmi://localhost:5252/tools", tools);
                Logger.log("RMIServer started");
            } catch (Exception var3) {
                var3.printStackTrace();
            }

        })).start();
    }
}
