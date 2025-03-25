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
                System.out.println("[Barrel2][RMI] RMI Registry created.");
            } catch (Exception e) {
                System.out.println("[Barrel2][RMI] RMI Registry already running.");
            }

            System.out.println("[Barrel2][Startup] Loading index from disk...");
            InvertedIndex index = InvertedIndex.loadFromDisk("barrel2_index.ser");

            // Tentativa inicial de sincronização com Barrel1
            try {
                System.out.println("[Barrel2][Sync] Attempting immediate sync from Barrel1...");
                SearchService other = (SearchService) Naming.lookup("rmi://localhost/Barrel1");
                InvertedIndex.IndexData data = other.exportIndexData();
                index.importIndexData(data);
                System.out.println("[Barrel2][Sync] Sync successful from Barrel1.");
            } catch (Exception e) {
                System.out.println("[Barrel2][Sync] Immediate sync failed. Will retry in background.");
                // Retry assíncrono
                new Thread(() -> {
                    for (int i = 1; i <= 5; i++) {
                        try {
                            System.out.println("[Barrel2][Sync][Thread=" + Thread.currentThread().getName() + "] Retry attempt " + i);
                            Thread.sleep(3000);
                            SearchService other = (SearchService) Naming.lookup("rmi://localhost/Barrel1");
                            InvertedIndex.IndexData data = other.exportIndexData();
                            index.importIndexData(data);
                            System.out.println("[Barrel2][Sync] Late sync successful from Barrel1.");
                            break;
                        } catch (Exception retry) {
                            System.out.println("[Barrel2][Sync] Retry " + i + " failed.");
                        }
                    }
                }, "Barrel2-SyncThread").start();
            }

            System.out.println("[Barrel2][Bind] Binding SearchService to RMI registry...");
            SearchService service = new SearchServiceImpl(index);
            Naming.rebind("rmi://localhost/Barrel2", service);
            System.out.println("[Barrel2][Status] Ready and online.");

            InvertedIndex finalIndex = index;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Barrel2][Shutdown] Saving index to disk...");
                finalIndex.saveToDisk("barrel2_index.ser");
            }, "Barrel2-ShutdownHook"));

        } catch (Exception e) {
            System.err.println("[Barrel2][ERROR] Exception during startup:");
            e.printStackTrace();
        }
    }
}
