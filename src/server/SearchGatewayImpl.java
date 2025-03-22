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
                System.out.println("[Gateway] Tentando conectar a: " + address);
                SearchService barrel = (SearchService) Naming.lookup(address);
                System.out.println("[Gateway] Conectado a: " + address);
                return barrel;
            } catch (Exception e) {
                System.out.println("[Gateway] Falha ao conectar a: " + address);
            }
        }

        System.out.println("[Gateway] Nenhum barrel disponível.");
        return null;
    }

    public List<String> search(String termo) throws RemoteException {
        System.out.println("[Gateway] Pedido de pesquisa: " + termo);
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) {
            List<String> resultados = barrel.search(termo);
            System.out.println("[Gateway] Resultados devolvidos: " + resultados.size());
            return resultados;
        }
        return List.of();
    }

    public Set<String> getBacklinks(String url) throws RemoteException {
        System.out.println("[Gateway] Pedido de backlinks para: " + url);
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getBacklinks(url);
        return Set.of();
    }

    public Map<String, Integer> getTopSearches() throws RemoteException {
        System.out.println("[Gateway] Pedido de top pesquisas");
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getTopSearches();
        return Map.of();
    }

    public double getAverageSearchTime() throws RemoteException {
        System.out.println("[Gateway] Pedido de tempo médio de resposta");
        SearchService barrel = getAvailableBarrel();
        if (barrel != null) return barrel.getAverageSearchTime();
        return -1;
    }
}
