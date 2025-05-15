package crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import server.BarrelRegistry;
import server.CentralURLQueue;
import server.SearchService;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler {

    private static final int MAX_DEPTH = 2;
    private static final int MAX_PAGES = 100;
    private static final int NUM_THREADS = 4;
    private static final long INITIAL_BACKOFF = 10;
    private static final long MAX_BACKOFF = 1000;
    private static final AtomicInteger pagesIndexed = new AtomicInteger(0);
    private static CentralURLQueue queue;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try {
            System.out.println("[DEBUG] Connecting to RMI queue...");
            queue = (CentralURLQueue) Naming.lookup("rmi://localhost/CentralURLQueue");
            System.out.println("[DEBUG] Connected to URLQueue.");

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(new CrawlerWorker());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to connect or start crawling:");
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    static class CrawlerWorker implements Runnable {

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("[DEBUG] [" + threadName + "] Worker started.");

            long backoff = INITIAL_BACKOFF;

            while (true) {
                String startUrl;
                try {
                    startUrl = queue.getNextUrl();
                } catch (RemoteException e) {
                    System.err.println("[DEBUG] [" + threadName + "] RMI error getting URL: " + e.getMessage());
                    break; 
                }

                if (startUrl == null) {
                    System.out.println("[DEBUG] [" + threadName + "] Queue is empty. Backing off for " + backoff + " ms...");
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException e) {
                        System.out.println("[DEBUG] [" + threadName + "] Interrupted during backoff.");
                        Thread.currentThread().interrupt();
                        break;
                    }
                    backoff = Math.min(backoff * 2, MAX_BACKOFF);
                    continue;
                }

                backoff = INITIAL_BACKOFF;

                bfsCrawl(startUrl, threadName);
            }
            System.out.println("[DEBUG] [" + threadName + "] Worker finished.");
        }

        private void bfsCrawl(String startUrl, String threadName) {
            Queue<URLDepthPair> crawlQueue = new LinkedList<>();
            Set<String> visited = new HashSet<>();
            crawlQueue.add(new URLDepthPair(startUrl, 0));
            visited.add(startUrl);

            System.out.println("[DEBUG] [" + threadName + "] Crawling: " + startUrl);

            while (!crawlQueue.isEmpty() && pagesIndexed.get() < MAX_PAGES) {
                URLDepthPair current = crawlQueue.poll();
                String url = current.url;
                int depth = current.depth;

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
                                System.out.println("[DEBUG] [" + threadName + "] Found: " + found);
                            }
                        }
                    }

                    for (String address : BarrelRegistry.getBarrelAddresses()) {
                        try {
                            SearchService service = (SearchService) Naming.lookup(address);
                            service.indexPage(url, text, foundLinks);
                            System.out.println("[DEBUG] Sent to barrel: " + address);
                        } catch (Exception e) {
                            System.err.println("[DEBUG] Failed to send to barrel: " + address);
                        }
                    }

                    pagesIndexed.incrementAndGet();
                    System.out.println("[DEBUG] Indexed (total): " + pagesIndexed.get());

                } catch (IOException e) {
                    System.err.println("[DEBUG] ERROR accessing URL: " + url);
                }
            }

            System.out.println("[DEBUG] [" + threadName + "] Finished crawling: " + startUrl);
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