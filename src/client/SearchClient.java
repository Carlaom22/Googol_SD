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
                System.out.println("2. See backlinks of a certain page");
                System.out.println("3. See stats");
                System.out.println("4. Add URL to index");
                System.out.println("0. Exit");
                System.out.print("Escolha uma opção: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("0")) break;

                switch (opcao) {
                    case "1" -> {
                        System.out.print("Term: ");
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
                                System.out.println("\nResults [" + (start + 1) + "to" + end + "] de " + resultados.size() + ":");
                                for (int i = start; i < end; i++) {
                                    System.out.println((i + 1) + ". " + resultados.get(i));
                                }
                                if (end == resultados.size()) break;
                                System.out.print("Enter 'n' to see next page or 'q' to exit: ");
                                String cmd = scanner.nextLine();
                                if (cmd.equalsIgnoreCase("n")) page++;
                                else break;
                            }
                        }
                    }
                    case "2" -> {
                        System.out.print("URL: ");
                        String url = scanner.nextLine();
                        Set<String> backlinks = gateway.getBacklinks(url);
                        if (backlinks.isEmpty()) System.out.println("No backlink found.");
                        else backlinks.forEach(System.out::println);
                    }
                    case "3" -> {
                        System.out.println("\nSystem stats:");
                        Map<String, Integer> top = gateway.getTopSearches();
                        top.forEach((k, v) -> System.out.println("- " + k + ": " + v + " times"));
                        System.out.println("Time of response (average): " + gateway.getAverageSearchTime() + " ms");
                    }
                    case "4" -> {
                        System.out.println("Opening LinkAdder in another terminal...");
                        try {
                            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "cd .. && java -cp bin client.LinkAdder").start();
                        } catch (Exception e) {
                            System.out.println("[ERROR] Fail to open LinkAdder.");
                            e.printStackTrace();
                        }
                    }
                    default -> System.out.println("Invalid option.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}