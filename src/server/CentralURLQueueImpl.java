package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of the CentralURLQueue interface.
 * Handles unique URL registration and queue access for crawlers.
 */
public class CentralURLQueueImpl extends UnicastRemoteObject implements CentralURLQueue {

    private final Queue<String> queue = new LinkedList<>();
    private final Set<String> visited = new HashSet<>();

    public CentralURLQueueImpl() throws RemoteException {
        super();
    }

    /**
     * Adds a new URL to the queue if it hasn't been visited before.
     *
     * @param url the URL to be added
     */
    @Override
    public synchronized void addUrl(String url) throws RemoteException {
        if (!visited.contains(url)) {
            queue.add(url);
            visited.add(url);
            System.out.println("[CentralURLQueue] URL added: " + url);
        } else {
            System.out.println("[CentralURLQueue] Ignored (already visited): " + url);
        }
    }

    /**
     * Retrieves the next available URL from the queue.
     *
     * @return the next URL, or null if the queue is empty
     */
    @Override
    public synchronized String getNextUrl() throws RemoteException {
        String next = queue.poll();
        if (next != null) {
            System.out.println("[CentralURLQueue] Sending URL to crawler: " + next);
        } else {
            System.out.println("[CentralURLQueue] Queue is empty.");
        }
        return next;
    }
}