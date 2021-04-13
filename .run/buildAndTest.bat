@echo off

set PLUGIN_NAME=MyMaid4
set JAR_FILE=MyMaid4.jar

if not exist server (
    mkdir server
)

if not exist server\eula.txt (
    echo eula=true> server\eula.txt
)

if not exist server\plugins (
    mkdir server\plugins
)

if not exist server\paper.jar (
    curl -o server\paper.jar -L "https://papermc.io/api/v1/paper/1.16.5/latest/download"
)

if not exist server\mcrconapi-1.1.1.jar (
    curl -o server\mcrconapi-1.1.1.jar -L "https://github.com/fnetworks/mcrconapi/releases/download/v1.1.1/mcrconapi-1.1.1.jar"
)

if not exist server/server.properties (
    echo server.propertiesが存在しないため作成します。
    echo gamemode=creative> server/server.properties
    echo level-name=Jao_Afa> server/server.properties
    echo enforce-whitelist=true>> server/server.properties
    echo difficulty=peaceful>> server/server.properties
    echo level-type=flat>> server/server.properties
    echo enable-command-block=true>> server/server.properties
    echo server-port=25565>> server/server.properties
    echo enable-rcon=true>> server/server.properties
    echo rcon.password=rconpassword>> server/server.properties
    echo rcon.port=25575>> server/server.properties
    echo white-list=true>> server/server.properties
    echo motd=%PLUGIN_NAME% Test Server>> server/server.properties
)

echo jarファイルをコピーします。
copy target\%JAR_FILE% server\plugins\%JAR_FILE%
if not %errorlevel% == 0 (
    echo %JAR_FILE% NOT FOUND

    echo 5秒後にクローズします。
    timeout 5 /NOBREAK
    exit 1
)

echo Minecraftサーバに対してリロードコマンドを実行します。
java -jar server\mcrconapi-1.1.1.jar -a localhost -l rconpassword -n -c "rl confirm"

if not %errorlevel% == 0 (
    echo Minecraftサーバが起動していないため、起動します。

    cd server
    java -jar paper.jar -nogui
    if %errorlevel% == 0 exit
)

echo 5秒後にクローズします。
timeout 5 /NOBREAK
exit