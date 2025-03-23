package index;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import server.SearchService;
import server.SearchServiceImpl;

public class IndexStorageBarrel2 {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[INFO] RMI Registry successfully created.");
            } catch (Exception e) {
                System.out.println("[INFO] RMI Registry already running.");
            }

            InvertedIndex index = InvertedIndex.loadFromDisk("barrel2_index.ser");
            SearchService searchService = new SearchServiceImpl(index);
            Naming.rebind("rmi://localhost/Barrel2", searchService);
            System.out.println("Barrel2 is ready.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                index.saveToDisk("barrel2_index.ser");
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
