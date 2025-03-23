package server;

import index.InvertedIndex;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchServiceImpl extends UnicastRemoteObject implements SearchService {
    private final InvertedIndex index;

    public SearchServiceImpl(InvertedIndex index) throws RemoteException {
        super();
        this.index = index;
    }

    @Override
    public void indexPage(String url, String content) throws RemoteException {
        System.out.println("[Barrel] Indexing (no links): " + url);
        index.add(url, content);
    }

    @Override
    public void indexPage(String url, String content, List<String> links) throws RemoteException {
        System.out.println("[Barrel] Indexing page: " + url);
        index.add(url, content);

        if (links != null && !links.isEmpty()) {
            for (String link : links) {
                index.addBacklink(link, url);
                System.out.println("[Barrel] Backlink registered: " + link + " -> " + url);
            }
        }
    }

    @Override
    public void addBacklink(String fromUrl, String toUrl) throws RemoteException {
        index.addBacklink(fromUrl, toUrl);
        System.out.println("[Barrel] Direct backlink added: " + fromUrl + " -> " + toUrl);
    }

    @Override
    public List<String> search(String term) throws RemoteException {
        List<String> results = index.search(term);
        System.out.println("[Barrel] Search: '" + term + "' - " + results.size() + " result(s)");
        return results;
    }

    @Override
    public Set<String> getBacklinks(String url) throws RemoteException {
        System.out.println("[Barrel] Backlink request for: " + url);
        return index.getBacklinks(url);
    }

    @Override
    public Map<String, Integer> getTopSearches() throws RemoteException {
        System.out.println("[Barrel] Top searches request");
        return index.getTopSearches();
    }

    @Override
    public double getAverageSearchTime() throws RemoteException {
        System.out.println("[Barrel] Average response time request");
        return index.getAverageSearchTime();
    }
}