package client;

import java.rmi.Naming;
import java.util.*;
import server.SearchGateway;

public class SearchClient {
    public static void main(String[] args) {
        try {
            SearchGateway gateway = (SearchGateway) Naming.lookup("rmi://localhost/SearchGateway");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n1. Pesquisar termo");
                System.out.println("2. Ver backlinks de uma página");
                System.out.println("3. Ver estatísticas");
                System.out.println("4. Adicionar URL para indexar");
                System.out.println("0. Sair");
                System.out.print("Escolha uma opção: ");
                String opcao = scanner.nextLine();

                if (opcao.equals("0")) break;

                switch (opcao) {
                    case "1" -> {
                        System.out.print("Termo: ");
                        String termo = scanner.nextLine();
                        List<String> resultados = gateway.search(termo);
                        if (resultados.isEmpty()) {
                            System.out.println("Nenhum resultado encontrado.");
                        } else {
                            int pageSize = 10;
                            int page = 0;
                            while (true) {
                                int start = page * pageSize;
                                int end = Math.min(start + pageSize, resultados.size());
                                System.out.println("\nResultados [" + (start + 1) + "–" + end + "] de " + resultados.size() + ":");
                                for (int i = start; i < end; i++) {
                                    System.out.println((i + 1) + ". " + resultados.get(i));
                                }
                                if (end == resultados.size()) break;
                                System.out.print("Digite 'n' para próxima página ou 'q' para sair: ");
                                String cmd = scanner.nextLine();
                                if (cmd.equalsIgnoreCase("n")) page++;
                                else break;
                            }
                        }
                    }
                    case "2" -> {
                        System.out.print("URL: ");
                        String url = scanner.nextLine();
                        Set<String> backlinks = gateway.getBacklinks(url);
                        if (backlinks.isEmpty()) System.out.println("Nenhum backlink encontrado.");
                        else backlinks.forEach(System.out::println);
                    }
                    case "3" -> {
                        System.out.println("\nEstatísticas do sistema:");
                        Map<String, Integer> top = gateway.getTopSearches();
                        top.forEach((k, v) -> System.out.println("- " + k + ": " + v + " vezes"));
                        System.out.println("Tempo médio de resposta: " + gateway.getAverageSearchTime() + " ms");
                    }
                    case "4" -> {
                        System.out.println("Abrindo LinkAdder em novo terminal...");
                        try {
                            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", "cd .. && java -cp bin client.LinkAdder").start();
                        } catch (Exception e) {
                            System.out.println("[ERRO] Falha ao abrir LinkAdder.");
                            e.printStackTrace();
                        }
                    }
                    default -> System.out.println("Opção inválida.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}