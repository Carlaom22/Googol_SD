package client;

import server.CentralURLQueue;
import java.rmi.Naming;
import java.util.Scanner;

public class LinkAdder {
    public static void main(String[] args) {
        try {
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            Scanner scanner = new Scanner(System.in);
            System.out.println("[LinkAdder] Ready. Type a URL or 'exit' to quit.");

            while (true) {
                System.out.print("Enter URL: ");
                String url = scanner.nextLine();
                if (url.equalsIgnoreCase("exit")) break;

                queue.addUrl(url);
                System.out.println("[LinkAdder] URL added: " + url);

                // Thread que lança um novo processo WebCrawler
                Thread thread = new Thread(() -> {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(
                            "cmd", "/c", "start", "cmd", "/k",
                            "cd bin && java -cp .;../lib/jsoup-1.18.3.jar crawler.WebCrawler"
                        );
                        pb.inheritIO();
                        pb.start();
                    } catch (Exception e) {
                        System.out.println("[LinkAdder] Failed to start crawler thread.");
                        e.printStackTrace();
                    }
                });
                thread.start();
            }

            scanner.close();
        } catch (Exception e) {
            System.out.println("[LinkAdder] ERROR:");
            e.printStackTrace();
        }
    }
}