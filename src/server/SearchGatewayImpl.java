package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SearchGatewayImpl extends UnicastRemoteObject implements SearchGateway {
    private final List<String> barrels;
    private int lastUsedIndex = -1;

    public SearchGatewayImpl() throws RemoteException {
        super();
        barrels = new ArrayList<>(List.of(
            "rmi://localhost/Barrel1",
            "rmi://localhost/Barrel2"
        ));
    }

    private synchronized String getNextBarrelAddress() {
        int attempts = 0;
        while (attempts < barrels.size()) {
            lastUsedIndex = (lastUsedIndex + 1) % barrels.size();
            String address = barrels.get(lastUsedIndex);
            try {
                Naming.lookup(address);
                return address;
            } catch (Exception e) {
                System.out.println("[Gateway] Barrel offline: " + address);
                attempts++;
            }
        }
        return null;
    }

    private <T> T tryBarrels(FunctionWrapper<T> fn, String actionName) {
        List<Integer> tried = new ArrayList<>();
        int attempts = 0;
        while (attempts < barrels.size()) {
            String address = getNextBarrelAddress();
            if (address == null || tried.contains(lastUsedIndex)) {
                attempts++;
                continue;
            }
            tried.add(lastUsedIndex);
            try {
                SearchService barrel = (SearchService) Naming.lookup(address);
                return fn.apply(barrel);
            } catch (Exception e) {
                System.out.println("[Gateway] " + actionName + " failed on " + address);
            }
            attempts++;
        }
        System.out.println("[Gateway] All barrels failed for " + actionName);
        return null;
    }

    public List<String> search(String term) throws RemoteException {
        List<String> result = tryBarrels(barrel -> barrel.search(term), "search");
        return result != null ? result : List.of();
    }

    public Set<String> getBacklinks(String url) throws RemoteException {
        Set<String> result = tryBarrels(barrel -> barrel.getBacklinks(url), "getBacklinks");
        return result != null ? result : Set.of();
    }

    public Map<String, Integer> getTopSearches() throws RemoteException {
        Map<String, Integer> result = tryBarrels(barrel -> barrel.getTopSearches(), "getTopSearches");
        return result != null ? result : Map.of();
    }

    public double getAverageSearchTime() throws RemoteException {
        Double result = tryBarrels(barrel -> barrel.getAverageSearchTime(), "getAverageSearchTime");
        return result != null ? result : -1;
    }

    public List<String> getActiveBarrels() throws RemoteException {
        List<String> active = new ArrayList<>();
        for (String address : barrels) {
            try {
                Naming.lookup(address);
                active.add(address);
            } catch (Exception ignored) {}
        }
        return active;
    }

    @FunctionalInterface
    private interface FunctionWrapper<T> {
        T apply(SearchService barrel) throws Exception;
    }
}