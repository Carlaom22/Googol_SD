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

    @Override
    public List<String> search(String termo) throws RemoteException {
        return searchMultiple(List.of(termo));
    }


    @Override
    public List<String> searchMultiple(List<String> terms) throws RemoteException {
        Set<String> finalResults = new LinkedHashSet<>();

        for (String term : terms) {
            List<String> results = tryBarrels(barrel -> barrel.search(term), "search");
            if (results != null) {
                finalResults.addAll(results);
            }
        }

        return new ArrayList<>(finalResults);
    }




    



    @Override
    public Set<String> getBacklinks(String url) throws RemoteException {
        Set<String> result = tryBarrels(barrel -> barrel.getBacklinks(url), "getBacklinks");
        return result != null ? result : Set.of();
    }

    @Override
    public Map<String, Integer> getTopSearches() throws RemoteException {
        Map<String, Integer> result = tryBarrels(barrel -> barrel.getTopSearches(), "getTopSearches");
        return result != null ? result : Map.of();
    }

    @Override
    public double getAverageSearchTime() throws RemoteException {
        Double result = tryBarrels(barrel -> barrel.getAverageSearchTime(), "getAverageSearchTime");
        return result != null ? result : -1;
    }

    
    @Override
    public List<String> getActiveBarrels() throws RemoteException {
        List<String> active = new ArrayList<>();
        for (String address : BarrelRegistry.getBarrelAddresses()) {
            try {
                SearchService barrel = (SearchService) Naming.lookup(address);
                barrel.ping(); 
                active.add(address);
            } catch (Exception e) {
                System.out.println("[Gateway] Barrel offline: " + address);
            }
        }
        return active;
    }

    @Override
    public String getStats() throws RemoteException {
        StringBuilder sb = new StringBuilder();

        sb.append("Top Pesquisas:\n");
        Map<String, Integer> topSearches = getTopSearches();
        if (topSearches.isEmpty()) {
            sb.append("Sem pesquisas registadas.\n");
        } else {
            for (Map.Entry<String, Integer> entry : topSearches.entrySet()) {
                sb.append("- ").append(entry.getKey())
                .append(": ").append(entry.getValue()).append(" vezes\n");
            }
        }

        sb.append("\nTempo médio de pesquisa: ")
        .append(String.format("%.2f", getAverageSearchTime()))
        .append(" ms\n");

        sb.append("\nBarrels ativos:\n");
        List<String> barrels = getActiveBarrels();
        if (barrels.isEmpty()) {
            sb.append("Nenhum barrel ativo.\n");
        } else {
            for (String b : barrels) {
                sb.append("- ").append(b).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public void addURL(String url) throws RemoteException {
        System.out.println("[Gateway] URL recebida para indexar: " + url);
        try {
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("//localhost:1099/CentralURLQueue");
            queue.addUrl(url);
        } catch (Exception e) {
            System.err.println("[Gateway] Erro ao contactar a CentralURLQueue: " + e.getMessage());
            e.printStackTrace();
        }
    }





    @FunctionalInterface
    private interface FunctionWrapper<T> {
        T apply(SearchService barrel) throws Exception;
    }
}