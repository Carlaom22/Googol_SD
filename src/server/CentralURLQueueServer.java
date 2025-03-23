package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CentralURLQueueServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Fila] RMI Registry iniciado.");
            } catch (Exception e) {
                System.out.println("[Fila] RMI Registry já estava em execução.");
            }

            CentralURLQueue fila = new CentralURLQueueImpl();
            Naming.rebind("rmi://localhost/URLQueue", fila);
            System.out.println("[Fila] CentralURLQueue disponível em rmi://localhost/URLQueue");
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar o servidor da fila central.");
            e.printStackTrace();
        }
    }
}
