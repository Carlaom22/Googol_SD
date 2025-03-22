package client;

import java.rmi.Naming;
import java.util.*;
import server.SearchGateway;

public class SearchClient {
    public static void main(String[] args) {
        try {
            SearchGateway gateway = (SearchGateway) Naming.lookup("rmi://localhost/SearchGateway");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Search term");
                System.out.println("2. View backlinks for a page");
                System.out.println("3. View system statistics");
                System.out.println("4. Add URL to index");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("0")) break;

                if (opcao.equals("1")) {
                    System.out.print("Enter the term: ");
                    String termo = scanner.nextLine();
                    List<String> resultados = gateway.search(termo);
                    if (resultados.isEmpty()) {
                        System.out.println("No results found.");
                    } else {
                        int pageSize = 10;
                        int page = 0;
                        while (true) {
                            int start = page * pageSize;
                            int end = Math.min(start + pageSize, resultados.size());
                            System.out.println("\nResults [" + (start + 1) + "–" + end + "] of " + resultados.size() + ":");
                            for (int i = start; i < end; i++) {
                                System.out.println((i + 1) + ". " + resultados.get(i));
                            }
                            if (end == resultados.size()) {
                                System.out.println("End of results.");
                                break;
                            }
                            System.out.print("Type 'n' for next page or 'q' to quit: ");
                            String cmd = scanner.nextLine();
                            if (cmd.equalsIgnoreCase("n")) {
                                page++;
                            } else {
                                break;
                            }
                        }
                    }
                } else if (opcao.equals("2")) {
                    System.out.print("Enter the URL to view backlinks: ");
                    String url = scanner.nextLine();
                    Set<String> backlinks = gateway.getBacklinks(url);
                    if (backlinks.isEmpty()) {
                        System.out.println("No backlinks found.");
                    } else {
                        backlinks.forEach(System.out::println);
                    }
                } else if (opcao.equals("3")) {
                    System.out.println("\nSystem statistics:");
                    Map<String, Integer> topSearches = gateway.getTopSearches();
                    System.out.println("Top 10 most searched terms:");
                    topSearches.forEach((key, value) -> System.out.println("- " + key + ": " + value + " times"));
                    double avgSearchTime = gateway.getAverageSearchTime();
                    System.out.println("Average response time: " + avgSearchTime + " ms");
                } else if (opcao.equals("4")) {
                    System.out.println("Opening LinkAdder in a new terminal...");
                    try {
                        new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "cd .. && java -cp bin client.LinkAdder").start();
                    } catch (Exception e) {
                        System.out.println("[ERROR] Failed to open LinkAdder.");
                        e.printStackTrace();
                    }
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
