package index;

import java.io.*;
import java.util.*;

public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, HashSet<String>> index = new HashMap<>();
    private HashMap<String, Integer> backlinks = new HashMap<>();
    private HashMap<String, Set<String>> backlinkMap = new HashMap<>();

    private HashMap<String, Integer> searchCount = new HashMap<>();
    private long totalSearchTime = 0;
    private long totalSearches = 0;

    public void add(String url, String content) {
        String[] words = content.toLowerCase().split("\\W+");
        for (String word : words) {
            index.putIfAbsent(word, new HashSet<>());
            index.get(word).add(url);
        }
    }

    public void addBacklink(String fromUrl, String toUrl) {
        if (!fromUrl.equals(toUrl)) {
            backlinks.put(toUrl, backlinks.getOrDefault(toUrl, 0) + 1);
            backlinkMap.putIfAbsent(toUrl, new HashSet<>());
            backlinkMap.get(toUrl).add(fromUrl);
        }
    }

    public List<String> search(String word) {
        long startTime = System.nanoTime();
        Set<String> results = index.getOrDefault(word, new HashSet<>());
        List<String> sortedResults = new ArrayList<>(results);
        sortedResults.sort((a, b) -> backlinks.getOrDefault(b, 0) - backlinks.getOrDefault(a, 0));
        searchCount.put(word, searchCount.getOrDefault(word, 0) + 1);
        long endTime = System.nanoTime();
        totalSearchTime += (endTime - startTime);
        totalSearches++;
        return sortedResults;
    }

    public Set<String> getBacklinks(String url) {
        return backlinkMap.getOrDefault(url, new HashSet<>());
    }

    public Map<String, Integer> getTopSearches() {
        return searchCount.entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);
    }

    public double getAverageSearchTime() {
        return totalSearches > 0 ? (totalSearchTime / totalSearches) / 1_000_000.0 : 0;
    }

    public void saveToDisk(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("[INFO] Index successfully saved to disk.");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save index to disk.");
            e.printStackTrace();
        }

        saveToText(filename.replace(".ser", ".txt"));
    }

    private void saveToText(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("==== INDEX ====");
            for (String word : index.keySet()) {
                writer.println("Word: " + word);
                for (String url : index.get(word)) {
                    writer.println("  - " + url);
                }
            }

            writer.println("\n==== BACKLINKS ====");
            for (String url : backlinkMap.keySet()) {
                writer.println("Page: " + url + " (Total: " + backlinks.getOrDefault(url, 0) + ")");
                for (String ref : backlinkMap.get(url)) {
                    writer.println("  <- " + ref);
                }
            }

            writer.println("\n==== TOP SEARCHES ====");
            for (Map.Entry<String, Integer> entry : getTopSearches().entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue() + " times");
            }

            writer.println("\nAverage response time: " + getAverageSearchTime() + " ms");
            System.out.println("[INFO] Index also saved in .txt format.");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save index as .txt.");
            e.printStackTrace();
        }
    }

    public static InvertedIndex loadFromDisk(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            System.out.println("[INFO] Index successfully loaded from disk.");
            return (InvertedIndex) in.readObject();
        } catch (Exception e) {
            System.out.println("[INFO] No previous index found. Creating a new one.");
            return new InvertedIndex();
        }
    }
}