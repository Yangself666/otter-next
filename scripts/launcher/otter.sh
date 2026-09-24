#!/usr/bin/env bash

set -o pipefail
umask 077

app_name='@@project.build.finalName@@'
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd) || exit 1
app_home=$(cd "${OTTER_HOME:-$script_dir}" && pwd) || exit 1
JAVA_OPTS=()
APP_ARGS=()

# 本地运行配置使用 Bash 数组，保留含空格参数的边界
if [ -f "$app_home/config/runtime.env" ]; then
    source "$app_home/config/runtime.env" || exit 1
fi

start_timeout=${START_TIMEOUT:-60}
stop_timeout=${STOP_TIMEOUT:-60}
health_url=${HEALTH_URL:-}
jar_file=${OTTER_JAR:-$script_dir/$app_name.jar}
case "$jar_file" in
    /*) ;;
    *) jar_file=$app_home/$jar_file ;;
esac
run_dir=$app_home/run
pid_file=$run_dir/$app_name.pid
args_file=$run_dir/$app_name.args
lock_dir=$run_dir/$app_name.lock
console_file=$app_home/logs/$app_name-console.log
pid=
token=

usage() {
    echo "用法：bash otter.sh {start|stop|restart|status} [应用参数]"
    echo "start 和 restart 支持 --otter.node.id=1 等 Spring Boot 参数"
}

fail() {
    echo "${app_name}：$*" >&2
    return 1
}

read_pid() {
    pid=
    token=
    [ -f "$pid_file" ] || return 1
    { IFS= read -r pid; IFS= read -r token; } < "$pid_file"
    [[ "$pid" =~ ^[0-9]+$ ]] && [ "$pid" -gt 1 ] || return 1
    [[ "$token" =~ ^[0-9]+-[0-9]+-[0-9]+-[0-9]+$ ]] || return 1
}

process_matches() {
    local command_line state
    command_line=$(LC_ALL=C ps -ww -p "$pid" -o args= 2>/dev/null) || return 1
    state=$(LC_ALL=C ps -p "$pid" -o stat= 2>/dev/null) || return 1
    [[ "$state" != *Z* ]] || return 1
    # 唯一启动标识与应用名称共同验证身份，避免 PID 被复用后误操作其他进程
    [[ " $command_line " == *" -Dotter.launch.token=$token "* ]] &&
        [[ " $command_line " == *" -Dotter.application=$app_name "* ]]
}

release_lock() {
    rm -f "$lock_dir/owner"
    rmdir "$lock_dir" 2>/dev/null || true
}

acquire_lock() {
    mkdir -p "$run_dir" || return 1
    if ! mkdir "$lock_dir" 2>/dev/null; then
        fail "操作锁已存在：${lock_dir}；请确认其他启动或停止操作已经结束"
        return 1
    fi
    trap release_lock EXIT
    trap 'exit 130' INT
    trap 'exit 143' TERM
    printf '%s\n' "$$" > "$lock_dir/owner" || return 1
}

prepare_java() {
    if [ -n "${JAVA:-}" ]; then
        java_bin=$JAVA
    elif [ -n "${JAVA_HOME:-}" ]; then
        java_bin=$JAVA_HOME/bin/java
    else
        java_bin=$(command -v java) || { fail "请配置完整 JDK 25"; return 1; }
    fi
    [ -x "$java_bin" ] || { fail "Java 可执行文件不可用：$java_bin"; return 1; }
    local version
    version=$("$java_bin" -version 2>&1 | awk -F '"' '/version/ {print $2; exit}')
    [ "${version%%.*}" = 25 ] || { fail "需要 JDK 25，当前版本：$version"; return 1; }
    [ -r "$jar_file" ] || { fail "找不到应用 JAR：$jar_file"; return 1; }
    if [ -n "$health_url" ]; then
        command -v curl >/dev/null || { fail "配置 HEALTH_URL 时需要 curl"; return 1; }
    fi
}

save_args() {
    local argument temporary=$args_file.$$
    : > "$temporary" || return 1
    for argument in "$@"; do
        printf '%s\0' "$argument" >> "$temporary" || return 1
    done
    mv -f "$temporary" "$args_file"
}

start_app() {
    if read_pid && process_matches; then
        echo "$app_name 已在运行，PID=$pid"
        return 0
    fi
    rm -f "$pid_file" || return 1
    prepare_java || return 1
    mkdir -p "$app_home/logs" || return 1
    touch "$console_file" || return 1
    save_args "$@" || return 1
    local offset deadline launcher child_pid
    offset=$(wc -c < "$console_file")
    offset=$((offset + 1))
    case "$app_name" in
        otter-manager) launcher=OtterManagerLauncher ;;
        otter-node) launcher=OtterLauncher ;;
        *) fail "请使用构建产物目录中的启动脚本"; return 1 ;;
    esac
    token=$(date +%s)-$$-$RANDOM-$RANDOM
    cd "$app_home" || return 1
    nohup "$java_bin" "${JAVA_OPTS[@]}" \
        "-Dotter.application=$app_name" "-Dotter.launch.token=$token" \
        -jar "$jar_file" "${APP_ARGS[@]}" "$@" \
        < /dev/null >> "$console_file" 2>&1 &
    child_pid=$!
    pid=$child_pid
    if ! printf '%s\n%s\n' "$pid" "$token" > "$pid_file.$$" || ! mv -f "$pid_file.$$" "$pid_file"; then
        kill -TERM "$child_pid" 2>/dev/null || true
        fail "无法保存 PID 文件，已通知新进程停止"
        return 1
    fi
    echo "正在启动 ${app_name}，PID=${pid}，控制台日志：$console_file"
    deadline=$((SECONDS + start_timeout))
    while [ "$SECONDS" -lt "$deadline" ]; do
        sleep 1
        if ! process_matches; then
            wait "$child_pid" 2>/dev/null
            rm -f "$pid_file"
            fail "启动失败，详情见 $console_file"
            return 1
        fi
        if [ -n "$health_url" ]; then
            if curl --silent --fail --max-time 2 "$health_url" >/dev/null; then
                echo "$app_name 已就绪，PID=$pid"
                return 0
            fi
        elif tail -c "+$offset" "$console_file" | grep -F "Started $launcher in " >/dev/null; then
            echo "$app_name 已就绪，PID=$pid"
            return 0
        fi
    done
    fail "等待就绪超过 ${start_timeout} 秒，进程仍在运行（PID=${pid}）；请检查 $console_file"
    return 1
}

stop_app() {
    if ! read_pid || ! process_matches; then
        rm -f "$pid_file" || return 1
        echo "$app_name 未运行"
        return 0
    fi
    local deadline=$((SECONDS + stop_timeout))
    echo "正在停止 ${app_name}，PID=$pid"
    kill -TERM "$pid" || { fail "发送停止信号失败，PID=$pid"; return 1; }
    while process_matches; do
        if [ "$SECONDS" -ge "$deadline" ]; then
            fail "等待停止超过 ${stop_timeout} 秒，进程仍在运行（PID=${pid}）"
            return 1
        fi
        sleep 1
    done
    rm -f "$pid_file" || return 1
    echo "$app_name 已停止"
}

restart_app() {
    local argument
    local previous_args=()
    # 重启沿用上次命令行参数，同时重新读取 runtime.env 中的配置
    if [ "$#" -eq 0 ] && [ -f "$args_file" ]; then
        while IFS= read -r -d '' argument; do
            previous_args+=("$argument")
        done < "$args_file"
        set -- "${previous_args[@]}"
    fi
    stop_app || return 1
    start_app "$@"
}

action=${1:-help}
[ "$#" -eq 0 ] || shift
case "$action" in
    help|-h|--help) usage; exit 0 ;;
    start|restart|stop|status) ;;
    *) usage >&2; exit 2 ;;
esac
if [[ ! "$start_timeout" =~ ^[1-9][0-9]*$ ]] || [[ ! "$stop_timeout" =~ ^[1-9][0-9]*$ ]]; then
    fail "START_TIMEOUT 和 STOP_TIMEOUT 必须为正整数"
    exit 2
fi
if [ "$action" = stop ] || [ "$action" = status ]; then
    [ "$#" -eq 0 ] || { usage >&2; exit 2; }
fi
if [ "$action" = status ]; then
    if read_pid && process_matches; then
        echo "$app_name 正在运行，PID=$pid"
        exit 0
    fi
    echo "$app_name 未运行"
    exit 3
fi
acquire_lock || exit 1
case "$action" in
    start) start_app "$@" ;;
    stop) stop_app ;;
    restart) restart_app "$@" ;;
esac
