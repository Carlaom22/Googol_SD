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

    // Indexação simples (cliente usa esta versão)
    @Override
    public void indexPage(String url, String content) throws RemoteException {
        System.out.println("[Barrel] Indexando página (sem backlinks): " + url);
        index.add(url, content);
    }

    // Indexação completa com backlinks (WebCrawler usa esta)
    public void indexPage(String url, String content, List<String> links) throws RemoteException {
        System.out.println("[Barrel] Indexando página: " + url);
        index.add(url, content);

        if (links != null && !links.isEmpty()) {
            for (String link : links) {
                index.addBacklink(link, url);
                System.out.println("[Barrel] Backlink registado: " + link + " - " + url);
            }
        } else {
            System.out.println("[Barrel] Nenhum backlink fornecido.");
        }
    }

    @Override
    public void addBacklink(String fromUrl, String toUrl) throws RemoteException {
        index.addBacklink(fromUrl, toUrl);
        System.out.println("[Barrel] Backlink direto adicionado: " + fromUrl + " → " + toUrl);
    }

    @Override
    public List<String> search(String termo) throws RemoteException {
        List<String> resultados = index.search(termo);
        System.out.println("[Barrel] Pesquisa: '" + termo + "' - " + resultados.size() + " resultado(s)");
        return resultados;
    }

    @Override
    public Set<String> getBacklinks(String url) throws RemoteException {
        System.out.println("[Barrel] Pedido de backlinks para: " + url);
        return index.getBacklinks(url);
    }

    @Override
    public Map<String, Integer> getTopSearches() throws RemoteException {
        System.out.println("[Barrel] Pedido de top pesquisas");
        return index.getTopSearches();
    }

    @Override
    public double getAverageSearchTime() throws RemoteException {
        System.out.println("[Barrel] Pedido de tempo médio de resposta");
        return index.getAverageSearchTime();
    }
}
