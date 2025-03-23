package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class CentralURLQueueImpl extends UnicastRemoteObject implements CentralURLQueue {
    private final Queue<String> queue = new LinkedList<>();
    private final Set<String> visited = new HashSet<>();

    public CentralURLQueueImpl() throws RemoteException {
        super();
    }

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

    @Override
    public synchronized String getNextUrl() throws RemoteException {
        String next = queue.poll();
        if (next != null) {
            System.out.println("[CentralURLQueue] Sending URL to crawler: " + next);
        }
        return next;
    }
}