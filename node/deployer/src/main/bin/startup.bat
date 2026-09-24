@echo off
@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

if not defined JAVA (
    if defined JAVA_HOME (set "JAVA=%JAVA_HOME%\bin\java.exe") else (set "JAVA=java")
)

set ENV_PATH=.\
if "%OS%" == "Windows_NT" set ENV_PATH=%~dp0%

cd /d "%ENV_PATH%"

set conf_dir=%ENV_PATH%\..\conf
set nid_file=%conf_dir%\nid
set /p nid=<"%nid_file%"
set otter_conf=%conf_dir%\otter.properties
set logback_configurationFile=%conf_dir%\logback.xml

set CLASSPATH=%conf_dir%
set CLASSPATH=%conf_dir%\..\lib\*;%CLASSPATH%

if not defined JAVA_MEM_OPTS set JAVA_MEM_OPTS=-Xms512m -Xmx2048m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError
set JAVA_OPTS_EXT= -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dapplication.codeset=UTF-8 -Dfile.encoding=UTF-8
rem JAVA_DEBUG_OPT 可由调用方设置以启用调试
set OTTER_OPTS= -DappName=otter-node -Ddubbo.application.logger=slf4j -Dlogging.config="%logback_configurationFile%" -Dnid=%nid%

set JAVA_OPTS= %JAVA_MEM_OPTS% %JAVA_OPTS_EXT% %JAVA_DEBUG_OPT% %OTTER_OPTS%

set CMD_STR= "%JAVA%" %JAVA_OPTS% -classpath "%CLASSPATH%" com.alibaba.otter.node.deployer.OtterLauncher
echo start cmd : %CMD_STR%

"%JAVA%" %JAVA_OPTS% -classpath "%CLASSPATH%" com.alibaba.otter.node.deployer.OtterLauncher
