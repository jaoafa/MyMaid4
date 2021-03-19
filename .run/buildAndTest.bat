@echo off

echo jarファイルをコピーします。
copy target\MyMaid4.jar server\plugins\MyMaid4.jar
if not %errorlevel% == 0 (
    echo MyMaid4.jar NOT FOUND

    echo 5秒後にクローズします。
    timeout 5 /NOBREAK
    exit 1
)

echo Minecraftサーバに対してリロードコマンドを実行します。
java -jar server\mcrconapi-1.1.1.jar -a localhost -l rconpassword -n -c "rl confirm"

if not %errorlevel% == 0 (
    echo Minecraftサーバが起動していないため、起動します。

    cd server
    java -jar paper-1.16.5.jar -nogui
    if %errorlevel% == 0 exit
)

echo 5秒後にクローズします。
timeout 5 /NOBREAK
exit
