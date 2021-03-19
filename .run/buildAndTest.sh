#!/bin/bash

if [ -f "server/server.properties" ]; then
    echo "server.propertiesが存在しないため作成します。"
    cat <<EOF > server/server.properties
gamemode=creative
enforce-whitelist=true
difficulty=peaceful
level-type=flat
enable-command-block=true
server-port=25565
enable-rcon=true
rcon.password=rconpassword
rcon.port=25575
white-list=true
motd=MyMaid4 Test Server
EOF
fi

echo "jarファイルをコピーします。"
cp target/MyMaid4.jar server/plugins/
if [ $? -ne 0 ]; then
    echo MyMaid4.jar NOT FOUND

    echo 5秒後にクローズします。
    sleep 5
    exit 1
fi

echo "Minecraftサーバに対してリロードコマンドを実行します。"
java -jar server/mcrconapi-1.1.1.jar -a localhost -l rconpassword -n -c "rl confirm"

if [ $? -ne 0 ]; then
    echo "Minecraftサーバが起動していないため、起動します。"

    cd server
    java -jar paper-1.16.5.jar -nogui
    if [ $? -eq 0 ]; then exit fi
fi

echo 5秒後にクローズします。
sleep 5
exit
