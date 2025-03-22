@echo off
setlocal

set SRC_DIR=src
set BIN_DIR=bin

if not exist %BIN_DIR% mkdir %BIN_DIR%

echo Compiling JAVA files...
javac -d %BIN_DIR% -cp lib\jsoup-1.18.3.jar -sourcepath %SRC_DIR% ^
    %SRC_DIR%\client\SearchClient.java ^
    %SRC_DIR%\server\SearchService.java ^
    %SRC_DIR%\server\SearchServiceImpl.java ^
    %SRC_DIR%\server\SearchServer.java ^
    %SRC_DIR%\server\BarrelRegistry.java ^
    %SRC_DIR%\crawler\WebCrawler.java ^
    %SRC_DIR%\index\InvertedIndex.java ^
    %SRC_DIR%\index\IndexStorageBarrel1.java ^
    %SRC_DIR%\index\IndexStorageBarrel2.java ^
    %SRC_DIR%\server\URLQueue.java

if %ERRORLEVEL% NEQ 0 (
    echo Error compiling.
    exit /b %ERRORLEVEL%
)

echo Starting Barrel 1...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel1"

timeout /t 2

echo Starting Barrel 2...
start cmd /k "cd %BIN_DIR% && java index.IndexStorageBarrel2"

timeout /t 2

echo Starting search server...
start cmd /k "cd %BIN_DIR% && java server.SearchServer"

timeout /t 2

echo Starting WebCrawler...
start cmd /k "cd %BIN_DIR% && java -cp .;..\\lib\\jsoup-1.18.3.jar crawler.WebCrawler"

echo Starting client search...
start cmd /k "cd %BIN_DIR% && java client.SearchClient"

echo Ready!

endlocal
