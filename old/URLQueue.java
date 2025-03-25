package server;

import java.util.*;

public class URLQueue {
    private Queue<String> queue = new LinkedList<>();
    private Set<String> visited = new HashSet<>();

    public void addURL(String url) {
        if (!visited.contains(url)) {
            queue.add(url);
            visited.add(url);
        }
    }

    public String getNextURL() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}