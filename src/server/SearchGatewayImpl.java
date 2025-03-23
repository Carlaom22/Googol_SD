package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SearchGatewayImpl extends UnicastRemoteObject implements SearchGateway {
    public SearchGatewayImpl() throws RemoteException {
        super();
    }

    private SearchService getAvailableBarrel() {
        String[] barrels = {"rmi://localhost/Barrel1", "rmi://localhost/Barrel2"};
        List<String> list = Arrays.asList(barrels);
        Collections.shuffle(list);

        for (String address : list) {
            try {
                System.out.println("[Gateway] Trying to connect to " + address);
                SearchService barrel = (SearchService) Naming.lookup(address);
                System.out.println("[Gateway] Successfully connected to " + address);
                return barrel;
            } catch (Exception e) {
                System.out.println("[Gateway] Failed to connect to " + address);
            }
        }

        System.out.println("[Gateway] No barrel available.");
        return null;
    }

    public List<String> search(String term) throws RemoteException {
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.search(term);
        return List.of();
    }

    public Set<String> getBacklinks(String url) throws RemoteException {
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getBacklinks(url);
        return Set.of();
    }

    public Map<String, Integer> getTopSearches() throws RemoteException {
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getTopSearches();
        return Map.of();
    }

    public double getAverageSearchTime() throws RemoteException {
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getAverageSearchTime();
        return -1;
    }
}
