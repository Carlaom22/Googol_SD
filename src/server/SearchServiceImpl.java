package server;

import index.InvertedIndex;
import index.InvertedIndex.IndexData;

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
        System.out.println("[Barrel][Init] SearchServiceImpl initialized on thread: " + Thread.currentThread().getName());
    }

    @Override
    public synchronized void indexPage(String url, String content) throws RemoteException {
        System.out.println("[Barrel][IndexNoLinks][Thread=" + Thread.currentThread().getName() + "] Indexing URL: " + url);
        index.add(url, content);
    }

    @Override
    public synchronized void indexPage(String url, String content, List<String> links) throws RemoteException {
        System.out.println("[Barrel][IndexWithLinks][Thread=" + Thread.currentThread().getName() + "] Indexing page: " + url);
        index.add(url, content);

        if (links != null && !links.isEmpty()) {
            for (String link : links) {
                index.addBacklink(link, url);
                System.out.println("[Barrel][Backlink][Thread=" + Thread.currentThread().getName() + "] Registered: " + link + " -> " + url);
            }
        } else {
            System.out.println("[Barrel][Backlink] No backlinks found for this page.");
        }
    }

    @Override
    public synchronized void addBacklink(String fromUrl, String toUrl) throws RemoteException {
        index.addBacklink(fromUrl, toUrl);
        System.out.println("[Barrel][ManualBacklink][Thread=" + Thread.currentThread().getName() + "] Added: " + fromUrl + " -> " + toUrl);
    }

    @Override
    public List<String> search(String term) throws RemoteException {
        System.out.println("[Barrel][Search][Thread=" + Thread.currentThread().getName() + "] Searching for: '" + term + "'");
        List<String> results = index.search(term);
        System.out.println("[Barrel][SearchResults] Found " + results.size() + " result(s) for '" + term + "'");
        return results;
    }

    @Override
    public Set<String> getBacklinks(String url) throws RemoteException {
        System.out.println("[Barrel][GetBacklinks][Thread=" + Thread.currentThread().getName() + "] for URL: " + url);
        return index.getBacklinks(url);
    }

    @Override
    public Map<String, Integer> getTopSearches() throws RemoteException {
        System.out.println("[Barrel][Stats] Top searches requested.");
        return index.getTopSearches();
    }

    @Override
    public double getAverageSearchTime() throws RemoteException {
        System.out.println("[Barrel][Stats] Average search time requested.");
        return index.getAverageSearchTime();
    }

    @Override
    public IndexData exportIndexData() throws RemoteException {
        System.out.println("[Barrel][Replication] Exporting index data for replication.");
        return index.exportIndexData();
    }

    @Override
    public String status() throws RemoteException {
        int totalPages = index.getTotalPages();
        int totalSearches = index.getTotalSearches();
        double avgTime = index.getAverageSearchTime();
        return String.format("[Status] Pages Indexed: %d | Total Searches: %d | Avg Search Time: %.2f ms",
                totalPages, totalSearches, avgTime);
    }

    @Override
    public void ping() throws RemoteException {
        System.out.println("[Barrel][Ping] Received ping on thread: " + Thread.currentThread().getName());
    }

}