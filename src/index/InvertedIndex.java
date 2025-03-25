package index;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Set<String>> index = new HashMap<>();
    private final Map<String, Integer> backlinks = new HashMap<>();
    private final Map<String, Set<String>> backlinkMap = new HashMap<>();
    private final Map<String, Integer> searchCount = new HashMap<>();

    private long totalSearchTime = 0;
    private long totalSearches = 0;

    public synchronized void add(String url, String content) {
        System.out.println("[Index][Thread=" + Thread.currentThread().getName() + "] Adding content for: " + url);
        String[] words = content.toLowerCase().split("\\W+");
        for (String word : words) {
            index.computeIfAbsent(word, k -> new HashSet<>()).add(url);
        }
        System.out.println("[Index] Added " + words.length + " words for URL: " + url);
    }

    public synchronized void addBacklink(String fromUrl, String toUrl) {
        if (!fromUrl.equals(toUrl)) {
            backlinks.put(toUrl, backlinks.getOrDefault(toUrl, 0) + 1);
            backlinkMap.computeIfAbsent(toUrl, k -> new HashSet<>()).add(fromUrl);
            System.out.println("[Index][Backlink] " + fromUrl + " -> " + toUrl);
        }
    }

    public synchronized List<String> search(String word) {
        System.out.println("[Index][Search] Searching for: " + word);
        long startTime = System.nanoTime();
        Set<String> results = index.getOrDefault(word, new HashSet<>());
        List<String> sortedResults = new ArrayList<>(results);
        sortedResults.sort((a, b) -> backlinks.getOrDefault(b, 0) - backlinks.getOrDefault(a, 0));
        searchCount.put(word, searchCount.getOrDefault(word, 0) + 1);
        totalSearchTime += System.nanoTime() - startTime;
        totalSearches++;
        System.out.println("[Index][SearchResults] Found " + sortedResults.size() + " results for: " + word);
        return sortedResults;
    }

    public synchronized Set<String> getBacklinks(String url) {
        System.out.println("[Index][Backlinks] Getting backlinks for: " + url);
        return backlinkMap.getOrDefault(url, new HashSet<>());
    }

    public synchronized Map<String, Integer> getTopSearches() {
        System.out.println("[Index][Stats] Fetching top searches...");
        return searchCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    public synchronized double getAverageSearchTime() {
        double avg = totalSearches > 0 ? (totalSearchTime / totalSearches) / 1_000_000.0 : 0;
        System.out.println("[Index][Stats] Average search time: " + avg + " ms");
        return avg;
    }

    public synchronized int getTotalSearches() {
        return (int) totalSearches;
    }

    public synchronized int getTotalPages() {
        return index.values().stream().flatMap(Set::stream).collect(Collectors.toSet()).size();
    }

    public synchronized void saveToDisk(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("[Index][Disk] Index saved to .ser file: " + filename);
        } catch (IOException e) {
            System.out.println("[Index][Disk][ERROR] Failed to save .ser file");
            e.printStackTrace();
        }
        saveToText(filename.replace(".ser", ".txt"));
    }

    private synchronized void saveToText(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("==== INDEX ====");
            for (var entry : index.entrySet()) {
                writer.println("Word: " + entry.getKey());
                for (String url : entry.getValue()) {
                    writer.println("  - " + url);
                }
            }

            writer.println("\n==== BACKLINKS ====");
            for (var entry : backlinkMap.entrySet()) {
                writer.println("Page: " + entry.getKey() + " (Total: " + backlinks.getOrDefault(entry.getKey(), 0) + ")");
                for (String ref : entry.getValue()) {
                    writer.println("  <- " + ref);
                }
            }

            writer.println("\n==== TOP SEARCHES ====");
            getTopSearches().forEach((term, count) -> writer.println(term + ": " + count + " times"));
            writer.println("\nAverage response time: " + getAverageSearchTime() + " ms");

            System.out.println("[Index][Disk] Index saved to .txt file: " + filename);
        } catch (IOException e) {
            System.out.println("[Index][Disk][ERROR] Failed to save .txt file");
            e.printStackTrace();
        }
    }

    public static InvertedIndex loadFromDisk(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            System.out.println("[Index][Disk] Index loaded from file: " + filename);
            return (InvertedIndex) in.readObject();
        } catch (Exception e) {
            System.out.println("[Index][Disk] No previous index found. Creating new one.");
            return new InvertedIndex();
        }
    }

    public synchronized IndexData exportIndexData() {
        System.out.println("[Index][Replication] Exporting data...");
        return new IndexData(index, backlinks, backlinkMap, searchCount, totalSearchTime, totalSearches);
    }

    public synchronized void importIndexData(IndexData data) {
        System.out.println("[Index][Replication] Importing index data...");
        data.index.forEach((word, urls) -> {
            index.computeIfAbsent(word, k -> new HashSet<>()).addAll(urls);
        });
        data.backlinks.forEach((url, count) -> {
            backlinks.put(url, backlinks.getOrDefault(url, 0) + count);
        });
        data.backlinkMap.forEach((url, froms) -> {
            backlinkMap.computeIfAbsent(url, k -> new HashSet<>()).addAll(froms);
        });
        data.searchCount.forEach((term, count) -> {
            searchCount.put(term, searchCount.getOrDefault(term, 0) + count);
        });
        totalSearchTime += data.totalSearchTime;
        totalSearches += data.totalSearches;
        System.out.println("[Index][Replication] Import completed.");
    }

    public static class IndexData implements Serializable {
        private static final long serialVersionUID = 2L;
        public final Map<String, Set<String>> index;
        public final Map<String, Integer> backlinks;
        public final Map<String, Set<String>> backlinkMap;
        public final Map<String, Integer> searchCount;
        public final long totalSearchTime;
        public final long totalSearches;

        public IndexData(Map<String, Set<String>> index,
                         Map<String, Integer> backlinks,
                         Map<String, Set<String>> backlinkMap,
                         Map<String, Integer> searchCount,
                         long totalSearchTime,
                         long totalSearches) {
            this.index = deepCopy(index);
            this.backlinks = new HashMap<>(backlinks);
            this.backlinkMap = deepCopy(backlinkMap);
            this.searchCount = new HashMap<>(searchCount);
            this.totalSearchTime = totalSearchTime;
            this.totalSearches = totalSearches;
        }

        private <T> Map<String, Set<T>> deepCopy(Map<String, Set<T>> original) {
            Map<String, Set<T>> copy = new HashMap<>();
            for (var entry : original.entrySet()) {
                copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return copy;
        }
    }
}
