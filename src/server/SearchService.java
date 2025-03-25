package server;

import index.InvertedIndex.IndexData;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SearchService extends Remote {
    void indexPage(String url, String content) throws RemoteException;
    void indexPage(String url, String content, List<String> links) throws RemoteException;
    void addBacklink(String fromUrl, String toUrl) throws RemoteException;

    List<String> search(String termo) throws RemoteException;
    Set<String> getBacklinks(String url) throws RemoteException;
    Map<String, Integer> getTopSearches() throws RemoteException;
    double getAverageSearchTime() throws RemoteException;

    // ✅ Método para replicação atômica
    IndexData exportIndexData() throws RemoteException;

    // ✅ Método de status geral do sistema
    String status() throws RemoteException;

    void ping() throws RemoteException;

}
