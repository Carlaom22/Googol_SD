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
                System.out.println("\n=======================");
                System.out.println("        MENU");
                System.out.println("=======================");
                System.out.println("1. Search term");
                System.out.println("2. View backlinks for a URL");
                System.out.println("3. View system statistics");
                System.out.println("4. Add URL to index");
                System.out.println("5. View active barrels");
                System.out.println("6. View barrel status report");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");

                String option = scanner.nextLine().trim();

                switch (option) {
                    case "0" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    case "1" -> searchTerm(gateway, scanner);
                    case "2" -> viewBacklinks(gateway, scanner);
                    case "3" -> viewStats(gateway);
                    case "4" -> openLinkAdder();
                    case "5" -> viewActiveBarrels(gateway);
                    case "6" -> viewSystemStatus(gateway);
                    default -> System.out.println("Invalid option. Try again.");
                }
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to connect to SearchGateway.");
            e.printStackTrace();
        }
    }

    private static void searchTerm(SearchGateway gateway, Scanner scanner) {
        try {
            System.out.print("Enter search term: ");
            String term = scanner.nextLine().trim();
            List<String> results = gateway.search(term);

            if (results.isEmpty()) {
                System.out.println("No results found.");
                return;
            }

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
                String cmd = scanner.nextLine().trim();
                if (!cmd.equalsIgnoreCase("n")) break;
                page++;
            }

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to search term.");
            e.printStackTrace();
        }
    }

    private static void viewBacklinks(SearchGateway gateway, Scanner scanner) {
        try {
            System.out.print("Enter URL: ");
            String url = scanner.nextLine().trim();
            Set<String> backlinks = gateway.getBacklinks(url);

            if (backlinks.isEmpty()) {
                System.out.println("No backlinks found.");
            } else {
                System.out.println("Backlinks:");
                backlinks.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to retrieve backlinks.");
            e.printStackTrace();
        }
    }

    private static void viewStats(SearchGateway gateway) {
        try {
            System.out.println("\nSystem Statistics:");
            Map<String, Integer> stats = gateway.getTopSearches();
            if (stats.isEmpty()) {
                System.out.println("No search data available.");
            } else {
                stats.forEach((term, count) -> System.out.println("- " + term + ": " + count + " times"));
            }

            double avgTime = gateway.getAverageSearchTime();
            System.out.printf("Average search time: %.2f ms%n", avgTime);

        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch statistics.");
            e.printStackTrace();
        }
    }

    private static void viewSystemStatus(SearchGateway gateway) {
        try {
            System.out.println("\n==== Barrel Status Report ====");
            List<String> barrels = gateway.getActiveBarrels();
            if (barrels.isEmpty()) {
                System.out.println("No barrels available.");
                return;
            }
            for (String address : barrels) {
                try {
                    server.SearchService service = (server.SearchService) Naming.lookup(address);
                    System.out.println("[" + address + "] " + service.status());
                } catch (Exception e) {
                    System.out.println("[ERROR] Failed to contact barrel at: " + address);
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to check barrel status.");
            e.printStackTrace();
        }
    }

    private static void openLinkAdder() {
        try {
            System.out.println("[Client] Opening LinkAdder...");
            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "cd .. && java -cp bin client.LinkAdder").start();
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to start LinkAdder.");
            e.printStackTrace();
        }
    }

    private static void viewActiveBarrels(SearchGateway gateway) {
        try {
            List<String> barrels = gateway.getActiveBarrels();
            if (barrels.isEmpty()) {
                System.out.println("No barrels online.");
            } else {
                System.out.println("Online Barrels:");
                barrels.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to fetch barrel list.");
            e.printStackTrace();
        }
    }
}
