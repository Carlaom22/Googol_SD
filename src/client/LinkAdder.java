package client;

import java.rmi.Naming;
import java.util.Scanner;
import server.CentralURLQueue;

public class LinkAdder {
    public static void main(String[] args) {
        try {
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            Scanner scanner = new Scanner(System.in);
            System.out.println("Adding links to central queue");
            while (true) {
                System.out.print("Enter URL (or 'exit'): ");
                String url = scanner.nextLine();
                if (url.equalsIgnoreCase("exit")) break;
                queue.addUrl(url);
                System.out.println("URL added successfully.");
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("[ERROR] Unable to connect to central queue.");
            e.printStackTrace();
        }
    }
}
