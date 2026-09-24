# 应用启动与配置

Manager 和 Node 分别以 Spring Boot 可执行 JAR 运行，依赖随 JAR 分发。Manager 内置管理页面，运行环境需要完整 JDK 25。

本地构建产物：

- `manager/deployer/target/otter-manager.jar`
- `node/deployer/target/otter-node.jar`

两端的 `target/` 目录还包含 `start.sh`、`stop.sh`、`restart.sh`、`status.sh`、公共脚本 `otter.sh` 和 `config/runtime.env.example`。部署时将 JAR、这五个脚本及所需配置放入同一个应用目录。脚本支持 Linux 和 macOS 的 Bash。

## Manager

准备可访问的 MySQL 管理库和 ZooKeeper，执行 `manager/deployer/src/main/resources/sql/otter-manager-schema.sql` 初始化管理库。已有管理库的迁移要求见 [运行基础](runtime-foundation.md)。

将 `otter-manager.jar` 放入独立运行目录，在该目录创建 `config/application.properties`：

```properties
server.port=8080
otter.database.driver.url=jdbc:mysql://127.0.0.1:3306/otter
otter.database.driver.username=otter
otter.database.driver.password=${OTTER_DB_PASSWORD}
otter.zookeeper.cluster.default=127.0.0.1:2181
otter.communication.manager.port=1099
```

通过运行环境设置 `OTTER_DB_PASSWORD`，然后在运行目录启动：

```bash
bash start.sh
```

访问 `http://localhost:8080` 打开管理页面。初始化账号及权限说明见 [运行基础](runtime-foundation.md)。

示例中的数据库与 ZooKeeper 地址用于同机部署，分机部署时填写相应的可达地址。

## Node

在管理页面配置协调集群和节点，记录节点 ID。节点地址应匹配运行机器，通信端口与下载端口应允许相关节点访问。

将 `otter-node.jar` 放入独立运行目录，执行：

```bash
bash start.sh --otter.node.id=1 --otter.manager.address=127.0.0.1:1099
```

将示例 ID 替换为管理页面中的节点 ID，Manager 地址使用其 RPC 端口。Node 从 Manager 获取节点配置、ZooKeeper 集群和下载端口，启动时需要能够连接 Manager 与对应的 ZooKeeper。

也可以使用环境变量：

```bash
export OTTER_NODE_ID=1
export OTTER_MANAGER_ADDRESS=127.0.0.1:1099
bash start.sh
```

## 外部配置与运行目录

两端均自动读取当前工作目录的 `application.properties`、`application.yml` 以及 `config/` 下的配置文件。JAR 内提供应用默认值，外部文件、环境变量及命令行参数可以覆盖相应属性。

通过参数指定额外配置文件：

```bash
java -jar otter-manager.jar --spring.config.additional-location=file:./manager.properties
```

| 配置 | 默认值或用途 |
| --- | --- |
| `server.port` | Manager 默认 8080；Node 默认使用管理页面中的下载端口 |
| `otter.node.id` / `OTTER_NODE_ID` | Node 身份，必须填写有效的节点 ID |
| `otter.manager.address` / `OTTER_MANAGER_ADDRESS` | Node 连接的 Manager RPC 地址，默认 `127.0.0.1:1099` |
| `otter.node.home` / `OTTER_NODE_HOME` | Node 数据根目录，默认当前工作目录的 `data/node` |
| `logging.file.path` / `LOGGING_FILE_PATH` | 两端的日志目录，默认当前工作目录的 `logs` |
| `logging.config` | 自定义 Logback 配置文件位置 |

Node 数据目录包含 `htdocs`、`download` 和 `extend` 等子目录。运行目录需要写权限，多实例应使用独立的数据目录与日志目录。

JVM 参数放在 `-jar` 前，应用参数放在 JAR 文件名后，例如：

```bash
java -Xms512m -Xmx2g -jar otter-node.jar --otter.node.id=1 --otter.node.home=./data/node-1
```

## 停止与进程管理

在 Manager 或 Node 对应的应用目录执行：

```bash
bash start.sh
bash status.sh
bash restart.sh
bash stop.sh
```

`start` 在后台启动应用，等待 Boot 启动完成后返回。重复启动会报告已有进程；`status` 在进程运行时返回 0，未运行时返回 3。控制台输出追加到 `logs/otter-manager-console.log` 或 `logs/otter-node-console.log`，业务日志继续使用 `logging.file.path` 指定的目录。

`restart` 先等待进程停止，再重新启动；默认沿用上次 `start` 的命令行参数，也可在 `restart.sh` 后提供一组替换参数。停止超时会返回错误并保留进程与 PID 记录，重启流程随即结束。启动等待超时也会保留进程，可用 `status.sh` 和日志继续检查。

脚本默认以所在目录为工作目录，从其他目录调用也能读取同一份配置。`OTTER_HOME` 可指定独立运行目录，`OTTER_JAR` 可指定应用 JAR；相对 JAR 路径以运行目录为基准。多个实例应使用各自的运行目录。

将 `config/runtime.env.example` 复制为 `config/runtime.env` 可以集中维护启动配置：

```bash
JAVA_OPTS=(-Xms512m -Xmx2g -XX:+HeapDumpOnOutOfMemoryError)
APP_ARGS=(--otter.node.id=1 --otter.manager.address=127.0.0.1:1099)
START_TIMEOUT=60
STOP_TIMEOUT=60
```

上述 `APP_ARGS` 示例用于 Node；Manager 可设置端口等对应参数。`JAVA_OPTS` 和 `APP_ARGS` 使用 Bash 数组，含空格的单个参数应加引号。配置文件每次执行脚本时重新读取，命令行中的应用参数优先于 `APP_ARGS`。需要通过环境传入 JVM 参数时可使用 `JAVA_TOOL_OPTIONS`。Java 路径按 `JAVA`、`JAVA_HOME`、PATH 的顺序选择。

默认启动就绪判断使用本次启动产生的 Boot 日志。如果自定义日志配置关闭了启动消息，可在 `runtime.env` 设置 `HEALTH_URL`，脚本会改用该地址的成功 HTTP 响应确认就绪，此时运行环境需要 curl。

PID、启动标识和上次命令行参数保存在 `run/`，文件权限限制为当前用户读写。脚本在发送停止信号前校验进程身份，避免 PID 复用造成误操作；并发启停通过 `run/` 下的操作锁互斥。若脚本被强制结束导致锁遗留，确认锁目录 `owner` 记录的进程已结束后，再移除对应的锁目录。

也可直接使用 `java -jar otter-manager.jar` 或 `java -jar otter-node.jar --otter.node.id=1` 前台运行。前台进程由终端的 `Ctrl+C` 或服务管理器的 `SIGTERM` 停止，生命周期脚本管理的是通过 `start.sh` 启动的进程。

两端均提供 `GET /actuator/health`。HTTP 健康状态表示应用可用，同步任务的状态和数据正确性需要单独验收。

## 已验证范围

已验证可执行 JAR 的启动、外部配置加载、节点 ID 环境变量、Manager 与 Node 通信、管理页面、文件下载、动态 Java 扩展编译和退出释放资源。管理库集成验证使用 H2 MySQL 模式；真实数据库同步的验收范围见 [运行基础](runtime-foundation.md)。

启动脚本在 macOS Bash 环境验证了启动、状态查询、重启参数保留、正常停止、并发启动、失效 PID、进程身份校验和超时行为；Linux 环境需要进一步执行部署验收。

Java 扩展编译直接读取可执行 JAR 内的依赖字节码，运行环境需要包含 `jdk.compiler` 模块。
