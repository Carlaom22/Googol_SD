package index;

import java.rmi.Naming;
import server.SearchService;
import java.rmi.registry.LocateRegistry;

public class IndexStorageBarrel {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            InvertedIndex index = new InvertedIndex();
            SearchService searchService = new server.SearchServiceImpl(index);
            Naming.rebind("rmi://localhost/SearchService", searchService);

            System.out.println("Indexation server ready...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}