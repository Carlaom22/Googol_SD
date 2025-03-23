package client;

import java.rmi.Naming;
import java.util.Scanner;
import server.CentralURLQueue;

public class LinkAdder {
    public static void main(String[] args) {
        try {
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Adicionar links à fila central");
            while (true) {
                System.out.print("Digite a URL (ou 'sair'): ");
                String url = scanner.nextLine();
                if (url.equalsIgnoreCase("sair")) break;
                queue.addUrl(url);
                System.out.println("URL adicionada com sucesso.");
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("[ERRO] Não foi possível ligar à fila central.");
            e.printStackTrace();
        }
    }
}
