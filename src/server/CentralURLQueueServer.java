package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Starts the CentralURLQueue RMI server.
 * Makes the queue available at rmi://localhost/URLQueue.
 */
public class CentralURLQueueServer {
    public static void main(String[] args) {
        try {
            // Tenta criar o RMI Registry na porta 1099
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Queue] RMI Registry started.");
            } catch (Exception e) {
                System.out.println("[Queue] RMI Registry already running.");
            }

            // Cria e publica a instância do serviço
            CentralURLQueue queue = new CentralURLQueueImpl();
            Naming.rebind("rmi://localhost/URLQueue", queue);
            System.out.println("[Queue] CentralURLQueue available at rmi://localhost/URLQueue");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start CentralURLQueueServer.");
            e.printStackTrace();
        }
    }
}