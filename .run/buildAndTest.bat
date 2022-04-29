@echo off

set PLUGIN_NAME=MyMaid4
set JAR_FILE=MyMaid4.jar
set PAPER_VERSION=1.18.2

if not exist server (
    mkdir server
)

if not exist server\eula.txt (
    echo eula=true> server\eula.txt
)

if not exist server\plugins (
    mkdir server\plugins
)

if not exist server\paper-%PAPER_VERSION%.jar (
    curl -o server\paper-%PAPER_VERSION%.jar -L "https://api.tomacheese.com/papermc/%PAPER_VERSION%/latest"
)

if not exist server\mcrconapi-1.1.1.jar (
    curl -o server\mcrconapi-1.1.1.jar -L "https://github.com/fnetworks/mcrconapi/releases/download/v1.1.1/mcrconapi-1.1.1.jar"
)

if not exist server/server.properties (
    echo server.properties�����݂��Ȃ����ߍ쐬���܂��B
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

echo jar�t�@�C�����R�s�[���܂��B
copy target\%JAR_FILE% server\plugins\%JAR_FILE%
if not %errorlevel% == 0 (
    echo %JAR_FILE% ��������܂���ł����B

    echo 5�b��ɃN���[�Y���܂��B
    timeout 5 /NOBREAK
    exit 1
)

set SELECTED_JAVA=notfound

where java 2>nul
if %errorlevel% == 0 (
   set SELECTED_JAVA=java
)

where java17 2>nul
if %errorlevel% == 0 (
   set SELECTED_JAVA=java17
)

if %SELECTED_JAVA% == "notfound" (
    echo Java��������܂���ł����B�C���X�g�[�����ĉ������B

    echo 5�b��ɃN���[�Y���܂��B
    timeout 5 /NOBREAK
    exit 1
)

for /f tokens^=2-5^ delims^=-_^" %%j in ('%SELECTED_JAVA% -fullversion 2^>^&1') do set "JAVA_VERSION=%%j%%k%%l%%m"

echo Java Version: %JAVA_VERSION% (%JAVA_VERSION:~0,3%)

if /i not "%JAVA_VERSION:~0,3%" == "17." (
    echo Paper�T�[�o�̋N���ɂ�Java 17���K�v�ł��B

    echo 5�b��ɃN���[�Y���܂��B
    timeout 5 /NOBREAK
    exit 1
)

echo Minecraft�T�[�o�ɑ΂��ă����[�h�R�}���h�����s���܂��B
%SELECTED_JAVA% -jar server\mcrconapi-1.1.1.jar -a localhost -l rconpassword -n -c "rl confirm"

if not %errorlevel% == 0 (
    echo Minecraft�T�[�o���N�����Ă��Ȃ����߁A�N�����܂��B

    cd server
    %SELECTED_JAVA% -jar paper-%PAPER_VERSION%.jar -nogui
    if %errorlevel% == 0 exit
)

echo 5�b��ɃN���[�Y���܂��B
timeout 5 /NOBREAK
exit