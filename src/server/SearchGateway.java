package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SearchGateway extends Remote {
    List<String> search(String termo) throws RemoteException;
    Set<String> getBacklinks(String url) throws RemoteException;
    Map<String, Integer> getTopSearches() throws RemoteException;
    double getAverageSearchTime() throws RemoteException;
    List<String> getActiveBarrels() throws RemoteException;
}
