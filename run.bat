@echo off
setlocal

set SRC_DIR=src
set BIN_DIR=bin

if not exist %BIN_DIR% mkdir %BIN_DIR%

echo Compiling ...
javac -d %BIN_DIR% -cp lib\jsoup-1.18.3.jar -sourcepath %SRC_DIR% ^
    %SRC_DIR%\client\SearchClient.java ^
    %SRC_DIR%\client\LinkAdder.java ^
    %SRC_DIR%\crawler\WebCrawler.java ^
    %SRC_DIR%\index\InvertedIndex.java ^
    %SRC_DIR%\index\IndexStorageBarrel1.java ^
    %SRC_DIR%\index\IndexStorageBarrel2.java ^
    %SRC_DIR%\server\SearchService.java ^
    %SRC_DIR%\server\SearchServiceImpl.java ^
    %SRC_DIR%\server\CentralURLQueue.java ^
    %SRC_DIR%\server\CentralURLQueueImpl.java ^
    %SRC_DIR%\server\CentralURLQueueServer.java ^
    %SRC_DIR%\server\SearchGateway.java ^
    %SRC_DIR%\server\SearchGatewayImpl.java ^
    %SRC_DIR%\server\SearchGatewayServer.java

if %ERRORLEVEL% NEQ 0 (
    echo Error while compiling.
    exit /b %ERRORLEVEL%
)

echo Compiled with success.

echo Starting CentralURLQueueServer...
start cmd /k "cd %BIN_DIR% && java server.CentralURLQueueServer"
timeout /t 2

echo Starting SearchGatewayServer...
start cmd /k "cd %BIN_DIR% && java server.SearchGatewayServer"
timeout /t 2

echo Starting Barrel 1...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel1"
timeout /t 1

echo Starting Barrel 2...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel2"
timeout /t 1

echo Starting SearchClient...
start cmd /k "cd %BIN_DIR% && java client.SearchClient"
timeout /t 2

echo Starting WebCrawler...
start cmd /k "cd %BIN_DIR% && java -cp .;../lib/jsoup-1.18.3.jar crawler.WebCrawler"

:: Optional: Start LinkAdder manually like this - equal for other files
:: start cmd /k "cd %BIN_DIR% && java -cp .;../lib/jsoup-1.18.3.jar client.LinkAdder"

echo Ready!
endlocal
