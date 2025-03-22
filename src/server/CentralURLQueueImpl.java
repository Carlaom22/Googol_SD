package server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashSet;
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
            System.out.println("URL adicionada: " + url);
        }
    }

    @Override
    public synchronized String getNextUrl() throws RemoteException {
        return queue.poll();
    }
}
