
     Projeto de Sistemas Distribuídos - Meta 1 - 2025


Autores:
- Carlos Soares     | 2020230124 | uc2020230124@student.uc.pt
- Miguel Machado    | 2020222874 | uc2020222874@student.uc.pt



  📁 Estrutura do Projeto


SD_2/
│
├── bin/                         # Diretório onde ficam os .class compilados
│
├── lib/
│   └── jsoup-1.18.3.jar         # Biblioteca usada pelo WebCrawler
│
├── run.bat                      # Script para compilar e iniciar o sistema
├── README.txt                   # Instruções de uso e explicações
│
└── src/
    ├── client/
    │   ├── SearchClient.java        # Interface textual para o utilizador
    │   └── LinkAdder.java           # Permite adicionar URLs à fila central
    │
    ├── crawler/
    │   └── WebCrawler.java          # Consome URLs da fila e envia para os barrels
    │
    ├── index/
    │   ├── InvertedIndex.java           # Estrutura de dados e persistência (.ser + .txt)
    │   ├── IndexStorageBarrel1.java     # Servidor de armazenamento 1
    │   └── IndexStorageBarrel2.java     # Servidor de armazenamento 2
    │
    └── server/
        ├── BarrelRegistry.java             # (opcional) Lista estática de barrels
        ├── SearchService.java              # Interface RMI dos barrels
        ├── SearchServiceImpl.java          # Lógica dos barrels (indexação, pesquisa, stats)
        ├── SearchGateway.java              # Interface RMI da Gateway
        ├── SearchGatewayImpl.java          # Encaminha pesquisas para barrels
        ├── SearchGatewayServer.java        # Publica a Gateway no RMI
        ├── CentralURLQueue.java            # Interface RMI da fila central
        ├── CentralURLQueueImpl.java        # Implementação da fila central
        └── CentralURLQueueServer.java      # Publica a fila de URLs no RMI


  📌 Funcionamento Geral (Resumo)

Utilizador (SearchClient)
   ⇩
Gateway (SearchGateway)
   ⇩
Barrels (Barrel1 e Barrel2)
   ⇩
InvertedIndex (armazenamento local .ser/.txt)

Separadamente:
LinkAdder → CentralURLQueue ← WebCrawler(s)



  ▶️ Como executar

1. Abre um terminal na raiz do projeto (`SD_2/`)
2. Executa:

   > .\run.bat

   O script irá:
   - Compilar os ficheiros
   - Iniciar: CentralURLQueue, Gateway, Barrel1, Barrel2, Cliente e Crawler

3. O cliente (SearchClient) irá apresentar o seguinte menu:

   [1] Pesquisar termo  
   [2] Ver backlinks de uma página  
   [3] Ver estatísticas do sistema  
   [4] Adicionar URL para indexação  
   [5] Ver barrels ativos  
   [0] Sair  

4. Cada novo link inserido gera um WebCrawler autónomo numa nova thread.


  💡 Observações

- A comunicação entre os módulos é feita via Java RMI.
- Toda a indexação é replicada de forma atómica para todos os barrels.
- Os barrels são tolerantes a falhas e sincronizam dados ao reiniciar.
- A fila central impede duplicação de URLs entre crawlers.
- O sistema suporta múltiplos crawlers e é completamente concorrente.


