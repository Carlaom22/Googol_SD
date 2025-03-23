package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SearchGatewayServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[Gateway] RMI Registry started.");
            } catch (Exception e) {
                System.out.println("[Gateway] RMI Registry was already active.");
            }

            SearchGateway gateway = new SearchGatewayImpl();
            Naming.rebind("rmi://localhost/SearchGateway", gateway);
            System.out.println("[Gateway] SearchGateway available at rmi://localhost/SearchGateway");
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to start the gateway server.");
            e.printStackTrace();
        }
    }
}