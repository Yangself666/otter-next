#!/bin/bash

current_path=`pwd`
bin_abs_path=$(cd "$(dirname "$0")" && pwd)
base=${bin_abs_path}/..
otterNodeIdFile=$base/conf/nid
logback_configurationFile=$base/conf/logback.xml
export LANG=en_US.UTF-8

if [ -f "$base/bin/otter.pid" ] ; then
    echo "found otter.pid , Please run stop.sh first ,then startup.sh" 2>&2
    exit 1
fi

if [ ! -d "$base/logs/node" ] ; then
    mkdir -p "$base/logs/node"
fi

if [ -z "${ARIA2C:-}" ]; then
    ARIA2C=$(command -v aria2c || true)
fi

# 优先使用显式指定的 Java 或 JAVA_HOME
if [ -z "${JAVA:-}" ]; then
    if [ -n "${JAVA_HOME:-}" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA=$(command -v java) || { echo "请配置 JDK 25 的 JAVA_HOME" >&2; exit 1; }
    fi
fi
if [ ! -x "$JAVA" ]; then
    echo "Java 可执行文件不可用：$JAVA" >&2
    exit 1
fi

java_version=$("$JAVA" -version 2>&1 | awk -F '"' '/version/ {print $2; exit}')
if [ "${java_version%%.*}" != "25" ]; then
    echo "当前版本要求 JDK 25，检测到：$java_version" >&2
    exit 1
fi

case "$#"
in
0 )
    ;;
1 )
    var=$1
    if [ -d "$var" ]
    then
        otterNodeIdFile=$var/nid
        logback_configurationFile=$base/conf/logback.xml
    elif [ -f "$var" ] ; then
        otterNodeIdFile=$base/conf/nid
        logback_configurationFile=$var
    else
        echo "THE PARAMETER IS NOT CORRECT.PLEASE CHECK AGAIN."
        exit 1
    fi;;
2 )
    var1=$1
    var2=$2
    if [ -d "$var1" ] && [ -f "$var2" ] ; then
        otterNodeIdFile=$var1/nid
        logback_configurationFile=$var2
    elif [ -d "$var2" ] && [ -f "$var1" ] ; then
        otterNodeIdFile=$var2/nid
        logback_configurationFile=$var1
    else
        if [ "$1" = "debug" ]; then
            DEBUG_PORT=$2
            DEBUG_SUSPEND="n"
            JAVA_DEBUG_OPT="-agentlib:jdwp=transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"
        fi
     fi;;
* )
    echo "THE PARAMETERS MUST BE TWO OR LESS.PLEASE CHECK AGAIN."
    exit 1;;
esac

# 在切换工作目录前固定外部配置路径
if [ -f "$logback_configurationFile" ]; then
    logback_configurationFile=$(cd "$(dirname "$logback_configurationFile")" && pwd)/$(basename "$logback_configurationFile")
fi

# 可通过 JAVA_OPTS 调整堆大小和垃圾回收参数
JAVA_OPTS=${JAVA_OPTS:--server -Xms512m -Xmx2048m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError}

JAVA_OPTS=" $JAVA_OPTS -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8"


if [ -f "$otterNodeIdFile" ] && [ -f "$logback_configurationFile" ]
then
    OTTER_OPTS=(-DappName=otter-node -Ddubbo.application.logger=slf4j "-Dlogging.config=$logback_configurationFile" "-Dnid=$(cat "$otterNodeIdFile")")
    for i in "$base"/lib/*.jar;
    do CLASSPATH=$i:"$CLASSPATH";
    done
    CLASSPATH="$base/conf:$CLASSPATH";

    echo LOG CONFIGURATION : $logback_configurationFile
    echo Otter nodeId file : $otterNodeIdFile
    echo CLASSPATH :$CLASSPATH

  echo "cd to $bin_abs_path for workaround relative path"
  cd "$bin_abs_path"

    "$JAVA" $JAVA_OPTS $JAVA_DEBUG_OPT "${OTTER_OPTS[@]}" -classpath ".:$CLASSPATH" com.alibaba.otter.node.deployer.OtterLauncher 1>>"$base/logs/node/node.log" 2>&1 &
    echo $! > "$base/bin/otter.pid"

  echo "cd to $current_path for continue"
  cd "$current_path"
else
    echo "otterNodeIdFile file("$otterNodeIdFile") OR log configration file($logback_configurationFile) is not exist,please create then first!"
    exit 1
fi
