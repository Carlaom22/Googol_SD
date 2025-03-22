package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CentralURLQueueServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[INFO] RMI Registry iniciado.");
            } catch (Exception e) {
                System.out.println("[INFO] RMI Registry já estava em execução.");
            }

            CentralURLQueue queue = new CentralURLQueueImpl();
            Naming.rebind("rmi://localhost/URLQueue", queue);
            System.out.println("CentralURLQueue ativa.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
