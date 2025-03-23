# SD_2 - Sistema de Pesquisa Distribuído com Java RMI

Este projeto implementa um motor de busca distribuído utilizando Java RMI, com as seguintes funcionalidades:
- Indexação de páginas web
- Pesquisa de termos
- Consulta de backlinks
- Estatísticas de uso
- Tolerância a falhas com múltiplos barrels
- Gateway de pesquisa
- Fila centralizada de URLs
- Interface de cliente em terminal

---

## 📁 Estrutura do Projeto

```
SD_2/
├── bin/                # Arquivos compilados (.class)
├── lib/                # Bibliotecas externas (ex: jsoup)
├── src/                # Código fonte
├── run.bat             # Script de compilação e execução
└── README.md           # Este ficheiro
```

---

## ✅ Pré-requisitos

- Java JDK 11 ou superior
- Biblioteca `jsoup-1.18.3.jar` colocada em `lib/`
- Ambiente Windows (ou adaptações para Linux/Mac)

---

## 🛠️ Compilar e Executar

### 1. Compilar e iniciar o sistema

```bash
.
run.bat
```

Este script:
- Compila todos os ficheiros `.java`
- Inicia:
  - CentralURLQueueServer (fila de links)
  - SearchGatewayServer (gateway de pesquisa)
  - Barrel1 e Barrel2 (armazenamento/indexação)
  - SearchClient (cliente com menu)
  - WebCrawler (para indexar as URLs da fila)

---

## 🔎 Como usar

### No menu do cliente:

```
1. Pesquisar termo
2. Ver backlinks de uma página
3. Ver estatísticas
4. Adicionar URL para indexar
0. Sair
```

### Para adicionar uma URL:

1. Escolha a opção **4**
2. Um novo terminal abrirá com o `LinkAdder`
3. Digite uma URL como:

```
https://pt.wikipedia.org/wiki/Sistemas_Distribuidos
```

4. A fila receberá esse link
5. O WebCrawler buscará e enviará para um barrel

---

## 🧪 Verificação de Funcionamento

### Ao adicionar um link:

- Terminal da Fila:
  ```
  [CentralURLQueue] URL adicionada: ...
  [CentralURLQueue] Enviando URL para crawler: ...
  ```

- Terminal do WebCrawler:
  ```
  [DEBUG] Recebido da fila: ...
  [DEBUG] Indexando: ...
  ```

- Terminal do Barrel:
  ```
  [Barrel] Indexando página: ...
  [Barrel] Backlink registado: ...
  ```

---

## 💬 Observações

- O sistema usa Java RMI para comunicação entre componentes
- O Gateway escolhe um barrel disponível e tolera falhas
- A fila central evita reindexação de links duplicados
- Os barrels armazenam backlinks e estatísticas

---

## 👨‍💻 Autor

EU. Projeto para a cadeira de Sistemas Distribuídos.
