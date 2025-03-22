package client;

import java.rmi.Naming;
import java.util.Scanner;
import server.CentralURLQueue;

public class LinkAdder {
    public static void main(String[] args) {
        try {
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            Scanner scanner = new Scanner(System.in);

            System.out.println("LinkAdder - Adicionar links à fila central de indexação");
            while (true) {
                System.out.print("Digite uma URL (ou 'sair'): ");
                String url = scanner.nextLine();
                if (url.equalsIgnoreCase("sair")) break;

                queue.addUrl(url);
                System.out.println("URL adicionada à fila.");
            }

            scanner.close();
        } catch (Exception e) {
            System.out.println("[ERRO] Não foi possível ligar à fila central.");
            e.printStackTrace();
        }
    }
}
