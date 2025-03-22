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
        String[] palavras = content.toLowerCase().split("\\W+");
        for (String palavra : palavras) {
            index.putIfAbsent(palavra, new HashSet<>());
            index.get(palavra).add(url);
        }
    }

    public void addBacklink(String fromUrl, String toUrl) {
        if (!fromUrl.equals(toUrl)) {
            backlinks.put(toUrl, backlinks.getOrDefault(toUrl, 0) + 1);
            backlinkMap.putIfAbsent(toUrl, new HashSet<>());
            backlinkMap.get(toUrl).add(fromUrl);
        }
    }

    public List<String> search(String palavra) {
        long startTime = System.nanoTime();
        Set<String> results = index.getOrDefault(palavra, new HashSet<>());
        List<String> sortedResults = new ArrayList<>(results);
        sortedResults.sort((a, b) -> backlinks.getOrDefault(b, 0) - backlinks.getOrDefault(a, 0));
        searchCount.put(palavra, searchCount.getOrDefault(palavra, 0) + 1);
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
            System.out.println("[INFO] Indice salvo em disco com sucesso.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar índice em disco.");
            e.printStackTrace();
        }

        saveToText(filename.replace(".ser", ".txt"));
    }

    private void saveToText(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("==== INDEX ====");
            for (String palavra : index.keySet()) {
                writer.println("Palavra: " + palavra);
                for (String url : index.get(palavra)) {
                    writer.println("  - " + url);
                }
            }

            writer.println("\n==== BACKLINKS ====");
            for (String url : backlinkMap.keySet()) {
                writer.println("Página: " + url + " (Total: " + backlinks.getOrDefault(url, 0) + ")");
                for (String ref : backlinkMap.get(url)) {
                    writer.println("  <- " + ref);
                }
            }

            writer.println("\n==== TOP BUSCAS ====");
            for (Map.Entry<String, Integer> entry : getTopSearches().entrySet()) {
                writer.println(entry.getKey() + ": " + entry.getValue() + " vezes");
            }

            writer.println("\nMédia de tempo de resposta: " + getAverageSearchTime() + " ms");
            System.out.println("[INFO] Indice salvo também em formato .txt.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar índice como .txt.");
            e.printStackTrace();
        }
    }

    public static InvertedIndex loadFromDisk(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            System.out.println("[INFO] Indice carregado do disco com sucesso.");
            return (InvertedIndex) in.readObject();
        } catch (Exception e) {
            System.out.println("[INFO] Nenhum índice anterior encontrado. Criando novo.");
            return new InvertedIndex();
        }
    }
}
