package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import index.InvertedIndex;

public class SearchServiceImpl extends UnicastRemoteObject implements SearchService {
    private InvertedIndex index;

    public SearchServiceImpl(InvertedIndex index) throws RemoteException {
        super();
        this.index = index;
    }

    @Override
    public List<String> search(String termo) throws RemoteException {
        System.out.println("Searching term: " + termo);
        return index.search(termo);
    }

    @Override
    public void indexPage(String url, String content) throws RemoteException {
        System.out.println("Indexing page: " + url);
        index.add(url, content);
    }

    @Override
    public void addBacklink(String fromUrl, String toUrl) throws RemoteException {
        System.out.println("Save backlink of " + fromUrl + " to " + toUrl);
        index.addBacklink(fromUrl, toUrl);
    }

    @Override
    public Set<String> getBacklinks(String url) throws RemoteException {
        return index.getBacklinks(url);
    }

    @Override
    public Map<String, Integer> getTopSearches() throws RemoteException {
        return index.getTopSearches();
    }

    @Override
    public double getAverageSearchTime() throws RemoteException {
        return index.getAverageSearchTime();
    }
}
