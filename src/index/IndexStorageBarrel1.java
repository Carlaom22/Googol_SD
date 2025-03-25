package index;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import server.SearchService;
import server.SearchServiceImpl;

public class IndexStorageBarrel1 {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Barrel1][RMI] RMI Registry created.");
            } catch (Exception e) {
                System.out.println("[Barrel1][RMI] RMI Registry already running.");
            }

            System.out.println("[Barrel1][Startup] Loading index from disk...");
            InvertedIndex index = InvertedIndex.loadFromDisk("barrel1_index.ser");

            try {
                System.out.println("[Barrel1][Sync] Attempting immediate sync from Barrel2...");
                SearchService other = (SearchService) Naming.lookup("rmi://localhost/Barrel2");
                InvertedIndex.IndexData data = other.exportIndexData();
                index.importIndexData(data);
                System.out.println("[Barrel1][Sync] Sync successful from Barrel2.");
            } catch (Exception e) {
                System.out.println("[Barrel1][Sync] Immediate sync failed. Will retry in background.");
                new Thread(() -> {
                    for (int i = 1; i <= 5; i++) {
                        try {
                            System.out.println("[Barrel1][Sync][Thread=" + Thread.currentThread().getName() + "] Retry attempt " + i);
                            Thread.sleep(3000);
                            SearchService other = (SearchService) Naming.lookup("rmi://localhost/Barrel2");
                            InvertedIndex.IndexData data = other.exportIndexData();
                            index.importIndexData(data);
                            System.out.println("[Barrel1][Sync] Late sync successful from Barrel2.");
                            break;
                        } catch (Exception retry) {
                            System.out.println("[Barrel1][Sync] Retry " + i + " failed.");
                        }
                    }
                }, "Barrel1-SyncThread").start();
            }

            System.out.println("[Barrel1][Bind] Binding SearchService to RMI registry...");
            SearchService service = new SearchServiceImpl(index);
            Naming.rebind("rmi://localhost/Barrel1", service);
            System.out.println("[Barrel1][Status] Ready and online.");

            InvertedIndex finalIndex = index;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[Barrel1][Shutdown] Saving index to disk...");
                finalIndex.saveToDisk("barrel1_index.ser");
            }, "Barrel1-ShutdownHook"));

        } catch (Exception e) {
            System.err.println("[Barrel1][ERROR] Exception during startup:");
            e.printStackTrace();
        }
    }
}
