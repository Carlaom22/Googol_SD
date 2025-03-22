package client;

import java.rmi.Naming;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import server.SearchService;

public class SearchClient {
    public static void main(String[] args) {
        try {
            SearchService searchService = (SearchService) Naming.lookup("rmi://localhost/SearchService");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Search term");
                System.out.println("2. See backlinks of a page");
                System.out.println("3. Stats");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("0")) break;

                if (opcao.equals("1")) {
                    System.out.print("Insert the term: ");
                    String termo = scanner.nextLine();
                    List<String> resultados = searchService.search(termo);
                    
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

                            System.out.print("'n' to next page or 'q' to exit: ");
                            String cmd = scanner.nextLine();
                            if (cmd.equalsIgnoreCase("n")) {
                                page++;
                            } else {
                                break;
                            }
                        }
                    }
                } else if (opcao.equals("2")) {
                    System.out.print("Insert URL to see their backlinks: ");
                    String url = scanner.nextLine();
                    Set<String> backlinks = searchService.getBacklinks(url);
                    if (backlinks.isEmpty()) {
                        System.out.println("No backlink found.");
                    } else {
                        backlinks.forEach(System.out::println);
                    }
                } else if (opcao.equals("3")) {
                    System.out.println("\nSystem stats:");
                    Map<String, Integer> topSearches = searchService.getTopSearches();
                    System.out.println("Top 10 searches:");
                    topSearches.forEach((key, value) -> System.out.println("- " + key + ": " + value + " times"));
                    double avgSearchTime = searchService.getAverageSearchTime();
                    System.out.println("Time per response: " + avgSearchTime + " ms");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
