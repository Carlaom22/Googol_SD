package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.rmi.Naming;
import java.util.*;

import server.SearchService;
import server.BarrelRegistry;

public class WebCrawler {
    private static final int MAX_DEPTH = 2;
    private static final int MAX_PAGES = 50;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter a URL to index (or type 'exit' to quit): ");
                String startUrl = scanner.nextLine();
                if (startUrl.equalsIgnoreCase("exit")) break;

                Queue<URLDepthPair> queue = new LinkedList<>();
                Set<String> visited = new HashSet<>();
                queue.add(new URLDepthPair(startUrl, 0));
                visited.add(startUrl);

                int pagesIndexed = 0;

                while (!queue.isEmpty() && pagesIndexed < MAX_PAGES) {
                    URLDepthPair current = queue.poll();
                    String url = current.url;
                    int depth = current.depth;

                    System.out.println("\nIndexing: " + url + " (Depth: " + depth + ")");
                    try {
                        Document doc = Jsoup.connect(url).get();
                        String text = doc.body().text();

                        for (String address : BarrelRegistry.getBarrelAddresses()) {
                            try {
                                SearchService searchService = (SearchService) Naming.lookup(address);
                                searchService.indexPage(url, text);
                            } catch (Exception e) {
                                System.out.println("[ERROR] Failed to contact barrel at " + address);
                            }
                        }

                        pagesIndexed++;

                        if (depth < MAX_DEPTH) {
                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                String found = link.attr("abs:href");
                                if (found.startsWith("http") && !visited.contains(found)) {
                                    queue.add(new URLDepthPair(found, depth + 1));
                                    visited.add(found);
                                    System.out.println("Added to queue: " + found);

                                    for (String address : BarrelRegistry.getBarrelAddresses()) {
                                        try {
                                            SearchService searchService = (SearchService) Naming.lookup(address);
                                            searchService.addBacklink(url, found);
                                        } catch (Exception e) {
                                            System.out.println("[ERROR] Failed to add backlink at " + address);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error accessing: " + url);
                    }
                }

                System.out.println("Indexing complete! Total pages indexed: " + pagesIndexed);
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class URLDepthPair {
        String url;
        int depth;

        URLDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}