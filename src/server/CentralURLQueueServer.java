package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CentralURLQueueServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Queue] RMI Registry started.");
            } catch (Exception e) {
                System.out.println("[Queue] RMI Registry was already running.");
            }

            CentralURLQueue queue = new CentralURLQueueImpl();
            Naming.rebind("rmi://localhost/URLQueue", queue);
            System.out.println("[Queue] CentralURLQueue available at rmi://localhost/URLQueue");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to start central queue server.");
            e.printStackTrace();
        }
    }
}
