package server;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface CentralURLQueue extends Remote {

    
    void addUrl(String url) throws RemoteException;
    String getNextUrl() throws RemoteException;
}