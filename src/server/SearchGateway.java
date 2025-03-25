package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface remota para o gateway de pesquisa.
 * Encaminha pedidos dos clientes para os barrels disponíveis.
 */
public interface SearchGateway extends Remote {

    /**
     * Pesquisa por um termo nos barrels disponíveis.
     * @param termo Termo a ser pesquisado.
     * @return Lista de URLs onde o termo foi encontrado.
     * @throws RemoteException Em caso de erro de comunicação.
     */
    List<String> search(String termo) throws RemoteException;

    /**
     * Obtém os backlinks de uma determinada URL.
     * @param url Página alvo dos backlinks.
     * @return Conjunto de URLs que apontam para a URL dada.
     * @throws RemoteException Em caso de erro de comunicação.
     */
    Set<String> getBacklinks(String url) throws RemoteException;

    /**
     * Retorna os 10 termos mais pesquisados no sistema.
     * @return Mapa de termos e número de vezes pesquisados.
     * @throws RemoteException Em caso de erro de comunicação.
     */
    Map<String, Integer> getTopSearches() throws RemoteException;

    /**
     * Retorna o tempo médio de resposta às pesquisas.
     * @return Tempo médio em milissegundos.
     * @throws RemoteException Em caso de erro de comunicação.
     */
    double getAverageSearchTime() throws RemoteException;

    /**
     * Lista todos os barrels atualmente ativos.
     * @return Lista de endereços RMI dos barrels disponíveis.
     * @throws RemoteException Em caso de erro de comunicação.
     */
    List<String> getActiveBarrels() throws RemoteException;
}