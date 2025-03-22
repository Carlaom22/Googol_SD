package server;

import java.rmi.Naming;
import java.util.*;

public class SearchServer {

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Search term");
                System.out.println("2. View backlinks for a page");
                System.out.println("3. View system statistics");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("0")) break;

                if (opcao.equals("1")) {
                    System.out.print("Enter the term: ");
                    String termo = scanner.nextLine();

                    BarrelConnection connection = tryGetBarrel();
                    if (connection == null) {
                        System.out.println("[ERROR] No barrel available.");
                        continue;
                    }

                    List<String> resultados = connection.service.search(termo);
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

                            System.out.println("Responded by: " + connection.address);
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

                    BarrelConnection connection = tryGetBarrel();
                    if (connection == null) {
                        System.out.println("[ERROR] No barrel available.");
                        continue;
                    }

                    Set<String> backlinks = connection.service.getBacklinks(url);
                    if (backlinks.isEmpty()) {
                        System.out.println("No backlinks found.");
                    } else {
                        backlinks.forEach(System.out::println);
                    }

                } else if (opcao.equals("3")) {
                    BarrelConnection connection = tryGetBarrel();
                    if (connection == null) {
                        System.out.println("[ERROR] No barrel available.");
                        continue;
                    }

                    System.out.println("\nSystem statistics:");
                    Map<String, Integer> topSearches = connection.service.getTopSearches();
                    System.out.println("Top 10 most searched terms:");
                    topSearches.forEach((key, value) -> System.out.println("- " + key + ": " + value + " times"));
                    double avgSearchTime = connection.service.getAverageSearchTime();
                    System.out.println("Average response time: " + avgSearchTime + " ms");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BarrelConnection tryGetBarrel() {
        List<String> barrels = BarrelRegistry.getBarrelAddresses();
        Collections.shuffle(barrels); // Random load balancing

        for (String address : barrels) {
            try {
                System.out.println("Trying to connect to barrel: " + address);
                SearchService service = (SearchService) Naming.lookup(address);
                System.out.println("Connected to barrel: " + address);
                return new BarrelConnection(service, address);
            } catch (Exception e) {
                System.out.println("Failed to contact " + address + ", trying next...");
            }
        }
        return null;
    }

    private static class BarrelConnection {
        public SearchService service;
        public String address;

        public BarrelConnection(SearchService service, String address) {
            this.service = service;
            this.address = address;
        }
    }
}
