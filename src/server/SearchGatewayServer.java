package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SearchGatewayServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Gateway] RMI Registry iniciado.");
            } catch (Exception e) {
                System.out.println("[Gateway] RMI Registry já estava ativo.");
            }

            SearchGateway gateway = new SearchGatewayImpl();
            Naming.rebind("rmi://localhost/SearchGateway", gateway);
            System.out.println("[Gateway] SearchGateway disponível em rmi://localhost/SearchGateway");
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar o servidor da gateway.");
            e.printStackTrace();
        }
    }
}
