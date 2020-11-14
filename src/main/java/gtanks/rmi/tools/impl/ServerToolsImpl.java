package gtanks.rmi.tools.impl;

import gtanks.logger.Logger;
import gtanks.rmi.tools.ServerTools;
import gtanks.system.restart.ServerRestartService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerToolsImpl extends UnicastRemoteObject implements ServerTools {
    private static final long serialVersionUID = 1034275549315539686L;
    private static final ServerRestartService serverRestartService = ServerRestartService.INSTANCE;

    public ServerToolsImpl() throws RemoteException {
        Logger.log("RMI ServerTools service is runned!");
    }

    @Override
    public void restart() throws RemoteException {
        serverRestartService.restart();
    }
}
