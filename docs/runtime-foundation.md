# Spring Boot 运行基础

Manager 与 Node 使用 JDK 25、Spring Boot 4.1.1、Spring Framework 7.0.9 和内嵌 Tomcat 11.0.24。版本由根 POM 的 Boot BOM 统一管理。管理页面使用 Vue 3.5.43、TypeScript 5.9.3、Vite 8.3.0，并与 Manager 同域发布。

官方兼容依据：[Spring Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html)、[MyBatis-Spring 兼容矩阵](https://mybatis.org/spring/)、[Vue 工程指南](https://vuejs.org/guide/quick-start.html)。

## 应用结构

- `manager/deployer`：Boot 启动入口，加载业务配置和管理 API
- `manager/web`：Spring MVC、Spring Security、管理前端与静态资源
- `manager/biz`：业务服务、MyBatis 3.5.19 / MyBatis-Spring 4.1.0、HikariCP 管理库连接池
- `node/deployer`：Boot 启动入口、节点生命周期、文件下载 Controller
- `shared/communication`：Dubbo 3.3.6 直连通信，Hessian2 序列化、JDK 动态代理、业务模型反序列化白名单
- `shared/arbitrate`：ZooKeeper 3.9.6 客户端与现有阶段仲裁

Manager 与 Node 通过同版本发布包部署。RPC 的序列化协议和服务标识随本次升级调整，两端需要一起升级。下载仍由 Node 直接提供，ZooKeeper 保存协调状态。

应用以 `otter-manager.jar` 与 `otter-node.jar` 分发，使用 `java -jar` 启动。完整 JDK 提供动态 Java 扩展编译能力，启动步骤见 [应用启动与配置](deployment.md)。

## 管理页面

访问 Manager 根路径即可打开控制台。页面覆盖运行总览、通道、流水线、节点、数据源、数据表、表映射与列分组、Canal 采集配置、告警规则、协调集群、数据矩阵、用户、运行日志和系统设置。复杂同步参数通过 JSON 编辑器维护，基础属性与关联对象使用中文表单。

建议配置顺序：协调集群 → 节点 → 数据源与表 → 采集配置 → 通道与流水线 → 表映射 → 启动通道。节点配置保存后，通过 `--otter.node.id` 或 `OTTER_NODE_ID` 指定 Node 对应的 ID。

管理员维护配置与用户；操作员查看配置、监控和日志并启停通道。认证采用服务端会话，写请求校验 CSRF Token。数据库与采集密码不返回浏览器，编辑时留空保留已有值。账号删除及角色调整对后续 API 请求立即生效。

初始化 SQL 提供 `admin/admin` 和 `guest/guest` 账号。部署时应在用户页面设置自己的密码或替换这些账号。新增密码至少 8 个字符，UTF-8 长度不超过 72 字节；已有密码验证成功后自动升级为 BCrypt。

已有管理数据库使用本版本前，需要执行：

```sql
ALTER TABLE `USER` MODIFY `PASSWORD` varchar(128) NOT NULL;
```

全新部署使用 `manager/deployer/src/main/resources/sql/otter-manager-schema.sql`。该脚本手动执行，应用启动不自动建库或覆盖已有配置。

## 配置与启动

两端包含默认应用配置，并按 Spring Boot 的规则读取工作目录和 `config/` 下的外部配置，支持命令行和环境变量覆盖。

| 配置 | 作用 |
| --- | --- |
| Manager `otter.port` / `server.port` | 管理 HTTP 端口，默认 8080，`server.port` 优先 |
| Manager `otter.address` / `server.address` | HTTP 监听地址，默认 0.0.0.0 |
| `otter.database.driver.*` | Manager 管理数据库连接参数 |
| `otter.communication.manager.port` | Manager RPC 端口，默认 1099 |
| `otter.manager.address` | Node 连接的 Manager RPC 地址 |
| `otter.zookeeper.cluster.default` | Manager 默认协调集群地址 |
| Node 配置中的 `parameters.downloadPort` | Node Tomcat 下载端口，可用 `server.port` 覆盖 |
| Node `otter.htdocs.dir` | 文件下载根目录 |
| `spring.lifecycle.timeout-per-shutdown-phase` | 每个关闭阶段等待时间，默认 30 秒 |

两端提供 `start.sh`、`stop.sh`、`restart.sh`、`status.sh` 管理后台进程；也可通过 `java -jar` 前台运行。启动参数与脚本配置见 [应用启动与配置](deployment.md)。业务日志目录默认为工作目录下的 `logs`，可通过 `logging.file.path` 配置。

`GET /actuator/health` 提供应用健康状态。节点通过 Boot 生命周期在 HTTP 服务就绪后注册，关闭时停止任务并释放通信资源。健康状态不代表某条同步任务已完成业务验收。

下载路径为 `/download/<相对路径>`，支持 GET、HEAD、Range。缺失文件返回 404，目录与下载根目录外的路径返回 403。

## 验证范围

本地使用 macOS、Temurin 25.0.4.1、Maven 3.9.12、Node.js 24.20.0，完成全模块构建、前端类型检查和生产构建，并使用临时 H2 MySQL 模式管理库及独立 ZooKeeper 进行集成验证：

- Manager 与 Node 完整启动、跨进程配置获取、节点在线登记
- 管理账号登录、配置列表、关联配置创建与修改、列映射、系统参数读写
- 浏览器登录和管理界面渲染
- Node 下载、HEAD、Range、缺失路径及目录越界校验
- 管理员与操作员权限、CSRF 拒绝、删除账号后的会话失效、重复账号校验
- 列映射与分组保存失败时事务回滚，保留原配置
- 可执行 JAR 启动与退出释放端口、外部配置与节点身份参数、动态 Java 扩展编译、MySQL 建表与改表语句映射

H2 验证使用单独适配的临时建库脚本，生产初始化脚本仍面向 MySQL。实际 MySQL binlog → 目标库同步、Oracle 写入、双向同步、跨节点故障恢复和持续运行性能，需要在下一阶段用真实源库与目标库验收。Canal 1.1.5、业务 JDBC 驱动及其配套组件属于后续采集链路升级范围。

现有 jtester/jmockit 测试默认跳过，本轮未新增测试文件。运行验证在 macOS 完成，其他操作系统需要在对应环境进一步验收。
