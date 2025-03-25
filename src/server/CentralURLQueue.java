package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * RMI interface for the Central URL Queue.
 * Provides methods for crawlers to retrieve URLs and clients to add them.
 */
public interface CentralURLQueue extends Remote {

    /**
     * Adds a URL to the central queue.
     *
     * @param url the URL to be added
     * @throws RemoteException if the RMI call fails
     */
    void addUrl(String url) throws RemoteException;

    /**
     * Retrieves and removes the next URL from the queue.
     *
     * @return the next URL to crawl, or null if the queue is empty
     * @throws RemoteException if the RMI call fails
     */
    String getNextUrl() throws RemoteException;
}