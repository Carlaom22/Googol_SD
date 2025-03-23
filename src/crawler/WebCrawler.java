package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import server.SearchService;
import server.BarrelRegistry;
import server.CentralURLQueue;

import java.io.IOException;
import java.rmi.Naming;
import java.util.*;

public class WebCrawler {
    private static final int MAX_DEPTH = 2;
    private static final int MAX_PAGES = 50;

    public static void main(String[] args) {
        try {
            System.out.println("[DEBUG] Trying to connect to the queue...");
            CentralURLQueue queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            System.out.println("[DEBUG] Connected to the queue successfully.");

            while (true) {
                System.out.println("[DEBUG] Requesting next link from the queue...");
                String startUrl = queue.getNextUrl();
                if (startUrl == null) {
                    System.out.println("[DEBUG] Queue is empty. Waiting...");
                    Thread.sleep(2000);
                    continue;
                }

                System.out.println("[DEBUG] Received from queue: " + startUrl);

                Queue<URLDepthPair> crawlQueue = new LinkedList<>();
                Set<String> visited = new HashSet<>();
                crawlQueue.add(new URLDepthPair(startUrl, 0));
                visited.add(startUrl);

                int pagesIndexed = 0;

                while (!crawlQueue.isEmpty() && pagesIndexed < MAX_PAGES) {
                    URLDepthPair current = crawlQueue.poll();
                    String url = current.url;
                    int depth = current.depth;

                    System.out.println("[DEBUG] Indexing: " + url + " | Depth: " + depth);
                    try {
                        Document doc = Jsoup.connect(url).get();
                        String text = doc.body().text();

                        List<String> foundLinks = new ArrayList<>();
                        if (depth < MAX_DEPTH) {
                            Elements links = doc.select("a[href]");
                            for (Element link : links) {
                                String found = link.attr("abs:href");
                                if (found.startsWith("http") && !visited.contains(found)) {
                                    foundLinks.add(found);
                                    crawlQueue.add(new URLDepthPair(found, depth + 1));
                                    visited.add(found);
                                    System.out.println("[DEBUG] Found link: " + found);
                                }
                            }
                        }

                        // Enviar para todos os barrels (replicação)
                        boolean atLeastOneSent = false;
                        for (String address : BarrelRegistry.getBarrelAddresses()) {
                            try {
                                System.out.println("[DEBUG] Trying to send to barrel: " + address);
                                SearchService service = (SearchService) Naming.lookup(address);
                                service.indexPage(url, text, foundLinks);
                                System.out.println("[DEBUG] Sent to: " + address);
                                atLeastOneSent = true;
                            } catch (Exception e) {
                                System.out.println("[DEBUG] Failed to contact: " + address);
                                e.printStackTrace();
                            }
                        }

                        if (!atLeastOneSent) {
                            System.out.println("[DEBUG] No barrel available for: " + url);
                        }

                        pagesIndexed++;

                    } catch (IOException e) {
                        System.out.println("[DEBUG] ERROR accessing: " + url);
                        e.printStackTrace();
                    }
                }

                System.out.println("[DEBUG] Indexing complete for: " + startUrl);
            }

        } catch (Exception e) {
            System.out.println("[DEBUG] ERROR connecting to queue:");
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