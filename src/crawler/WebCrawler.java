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

    // For exponential backoff
    private static final long INITIAL_BACKOFF = 10;
    private static final long MAX_BACKOFF = 1000;

    // Keep track of total pages indexed across all threads
    private static final AtomicInteger pagesIndexed = new AtomicInteger(0);

    // We'll store the RMI queue reference here so each worker can access it
    private static CentralURLQueue queue;

    public static void main(String[] args) {
        // Create a fixed thread pool, just like in Downloader
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        try {
            System.out.println("[DEBUG] Connecting to RMI queue...");
            queue = (CentralURLQueue) Naming.lookup("rmi://localhost/URLQueue");
            System.out.println("[DEBUG] Connected to URLQueue.");

            // Submit NUM_THREADS workers, each repeatedly pulling from queue
            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(new CrawlerWorker());
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to connect or start crawling:");
            e.printStackTrace();
        } finally {
            // We don't forcibly shut down here, leaving threads to run
            executor.shutdown();
        }
    }

    /**
     * A worker that repeatedly dequeues a startUrl from the RMI queue (with exponential backoff),
     * then does BFS (the same logic you had) on that single URL. When BFS finishes, it loops back
     * to pull the next URL, until the queue is empty for too long or we hit the max pages.
     */
    static class CrawlerWorker implements Runnable {

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("[DEBUG] [" + threadName + "] Worker started.");

            long backoff = INITIAL_BACKOFF;

            while (true) {
                String startUrl;
                try {
                    // Try to get a new URL from the RMI queue
                    startUrl = queue.getNextUrl();
                } catch (RemoteException e) {
                    System.err.println("[DEBUG] [" + threadName + "] RMI error getting URL: " + e.getMessage());
                    break; // end thread if RMI fails
                }

                if (startUrl == null) {
                    // If the queue is empty, do exponential backoff
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

                // Reset backoff on success
                backoff = INITIAL_BACKOFF;

                // Perform the BFS crawl logic on this single startUrl
                bfsCrawl(startUrl, threadName);
            }

            System.out.println("[DEBUG] [" + threadName + "] Worker finished.");
        }

        /**
         * The BFS logic you originally had in run(). We now put it in a separate method so each
         * newly dequeued URL can do the BFS process. Everything else is unchanged.
         */
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

                    // The same barrels logic as before
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

    // Unchanged
    static class URLDepthPair {
        String url;
        int depth;

        URLDepthPair(String url, int depth) {
            this.url = url;
            this.depth = depth;
        }
    }
}