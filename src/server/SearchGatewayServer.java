package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class SearchGatewayServer {
    public static void main(String[] args) {
        try {
            try {
                LocateRegistry.createRegistry(1099);
                System.out.println("[INFO] RMI Registry criado.");
            } catch (Exception e) {
                System.out.println("[INFO] RMI Registry já ativo.");
            }

            SearchGateway gateway = new SearchGatewayImpl();
            Naming.rebind("rmi://localhost/SearchGateway", gateway);
            System.out.println("SearchGateway ativo.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
