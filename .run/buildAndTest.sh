#!/bin/bash

PLUGIN_NAME="MyMaid4"
JAR_FILE="MyMaid4.jar"

if [ -f "server/" ]; then
    mkdir server
fi

if [ -f "server/eula.txt" ]; then
    echo "eula=true" > server/eula.txt
fi

if [ -f "server/plugins/" ]; then
    mkdir server/plugins/
fi

if [ -f "server/paper.jar" ]; then
    curl -o server/paper.jar -L "https://papermc.io/api/v1/paper/1.16.5/latest/download"
fi

if [ -f "server/mcrconapi-1.1.1.jar" ]; then
    curl -o server/mcrconapi-1.1.1.jar -L "https://github.com/fnetworks/mcrconapi/releases/download/v1.1.1/mcrconapi-1.1.1.jar"
fi

if [ -f "server/server.properties" ]; then
    echo "server.propertiesが存在しないため作成します。"
    cat <<EOF > server/server.properties
gamemode=creative
level-name=Jao_Afa
enforce-whitelist=true
difficulty=peaceful
level-type=flat
enable-command-block=true
server-port=25565
enable-rcon=true
rcon.password=rconpassword
rcon.port=25575
white-list=true
motd=$PLUGIN_NAME Test Server
EOF
fi

echo "jarファイルをコピーします。"
cp target/$JAR_FILE server/plugins/
if [ $? -ne 0 ]; then
    echo $JAR_FILE NOT FOUND

    echo 5秒後にクローズします。
    sleep 5
    exit 1
fi

echo "Minecraftサーバに対してリロードコマンドを実行します。"
java -jar server/mcrconapi-1.1.1.jar -a localhost -l rconpassword -n -c "rl confirm"

if [ $? -ne 0 ]; then
    echo "Minecraftサーバが起動していないため、起動します。"

    cd server
    java -jar paper.jar -nogui
    if [ $? -eq 0 ]; then exit fi
fi

echo 5秒後にクローズします。
sleep 5
exit
