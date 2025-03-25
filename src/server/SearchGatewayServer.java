package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Inicializa o servidor RMI do SearchGateway.
 * Responsável por disponibilizar o serviço que encaminha pedidos para os barrels.
 */
public class SearchGatewayServer {
    public static void main(String[] args) {
        try {
            // Iniciar RMI registry, se ainda não estiver ativo
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Gateway] RMI Registry started.");
            } catch (Exception e) {
                System.out.println("[Gateway] RMI Registry already running.");
            }

            // Criar e expor a instância do Gateway
            SearchGateway gateway = new SearchGatewayImpl();
            Naming.rebind("rmi://localhost/SearchGateway", gateway);
            System.out.println("[Gateway] SearchGateway bound at rmi://localhost/SearchGateway");

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start SearchGatewayServer:");
            e.printStackTrace();
        }
    }
}