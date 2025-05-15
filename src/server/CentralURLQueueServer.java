package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class CentralURLQueueServer {
    public static void main(String[] args) {
        try {
            CentralURLQueueImpl queue = new CentralURLQueueImpl();

            // (opcional, se ainda não houver um Registry)
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[RMI] Registry criado na porta 1099");
            } catch (Exception e) {
                System.out.println("[RMI] Registry já existente.");
            }

            Naming.rebind("CentralURLQueue", queue);
            System.out.println("[CentralURLQueueServer] Serviço registado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
