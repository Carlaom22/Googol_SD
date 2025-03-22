@echo off
setlocal

:: Diretórios
set SRC_DIR=src
set BIN_DIR=bin

if not exist %BIN_DIR% mkdir %BIN_DIR%

echo Compilando todos os ficheiros Java...
javac -d %BIN_DIR% -cp lib\jsoup-1.18.3.jar -sourcepath %SRC_DIR% ^
    %SRC_DIR%\client\SearchClient.java ^
    %SRC_DIR%\client\LinkAdder.java ^
    %SRC_DIR%\crawler\WebCrawler.java ^
    %SRC_DIR%\index\InvertedIndex.java ^
    %SRC_DIR%\index\IndexStorageBarrel1.java ^
    %SRC_DIR%\index\IndexStorageBarrel2.java ^
    %SRC_DIR%\server\SearchGateway.java ^
    %SRC_DIR%\server\SearchGatewayImpl.java ^
    %SRC_DIR%\server\SearchGatewayServer.java ^
    %SRC_DIR%\server\SearchService.java ^
    %SRC_DIR%\server\SearchServiceImpl.java ^
    %SRC_DIR%\server\BarrelRegistry.java ^
    %SRC_DIR%\server\CentralURLQueue.java ^
    %SRC_DIR%\server\CentralURLQueueImpl.java ^
    %SRC_DIR%\server\CentralURLQueueServer.java

if %ERRORLEVEL% NEQ 0 (
    echo Erro na compilação.
    exit /b %ERRORLEVEL%
)

echo Compilado com sucesso.

:: Iniciar CentralURLQueue
echo Iniciando servidor da fila central...
start cmd /k "cd %BIN_DIR% && java server.CentralURLQueueServer"

timeout /t 2

:: Iniciar SearchGatewayServer
echo Iniciando Gateway...
start cmd /k "cd %BIN_DIR% && java server.SearchGatewayServer"

timeout /t 2

:: Iniciar Barrels
echo Iniciando Barrel 1...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel1"
timeout /t 1

echo Iniciando Barrel 2...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel2"
timeout /t 1

:: Iniciar cliente de pesquisa
echo Iniciando SearchClient...
start cmd /k "cd %BIN_DIR% && java client.SearchClient"

echo Tudo pronto!
echo Pode iniciar crawlers adicionais com:
echo "cd bin && java crawler.WebCrawler"

endlocal

:: .\run.bat to compile