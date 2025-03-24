package client;

import java.io.File;
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
                System.out.println("5. View active barrels");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String opcao = scanner.nextLine();

                switch (opcao) {
                    case "0" -> {
                        System.out.println("Goodbye!");
                        return;
                    }

                    case "1" -> {
                        System.out.print("Term: ");
                        String term = scanner.nextLine();
                        List<String> results = gateway.search(term);
                        if (results.isEmpty()) {
                            System.out.println("No results found.");
                        } else {
                            int page = 0;
                            int pageSize = 10;
                            while (true) {
                                int start = page * pageSize;
                                int end = Math.min(start + pageSize, results.size());
                                System.out.println("\nResults [" + (start + 1) + "–" + end + "] of " + results.size() + ":");
                                for (int i = start; i < end; i++) {
                                    System.out.println((i + 1) + ". " + results.get(i));
                                }
                                if (end == results.size()) break;

                                System.out.print("Type 'n' for next page or 'q' to quit: ");
                                String cmd = scanner.nextLine();
                                if (cmd.equalsIgnoreCase("n")) {
                                    page++;
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    case "2" -> {
                        System.out.print("Enter the URL to see backlinks: ");
                        String url = scanner.nextLine();
                        Set<String> backlinks = gateway.getBacklinks(url);
                        if (backlinks.isEmpty()) {
                            System.out.println("No backlinks found.");
                        } else {
                            backlinks.forEach(System.out::println);
                        }
                    }

                    case "3" -> {
                        System.out.println("\nSystem statistics:");
                        Map<String, Integer> stats = gateway.getTopSearches();
                        if (stats.isEmpty()) {
                            System.out.println("No search data.");
                        } else {
                            stats.forEach((term, count) ->
                                System.out.println("- " + term + ": " + count + " times"));
                        }
                        double avg = gateway.getAverageSearchTime();
                        System.out.println("Average search time: " + avg + " ms");
                    }

                    case "4" -> {
                        System.out.println("Opening LinkAdder...");
                        try {
                            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "cd .. && java -cp bin client.LinkAdder").start();
                        } catch (Exception e) {
                            System.out.println("[ERROR] Failed to open LinkAdder:");
                            e.printStackTrace();
                        }
                    }

                    case "5" -> {
                        System.out.println("\nActive barrels:");
                        List<String> barrels = gateway.getActiveBarrels();
                        if (barrels.isEmpty()) {
                            System.out.println("No barrels online.");
                        } else {
                            barrels.forEach(System.out::println);
                        }
                    }

                    default -> System.out.println("Invalid option.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
