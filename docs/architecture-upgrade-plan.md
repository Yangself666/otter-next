# Otter Next 升级路线

当前基础采用 JDK 25、Spring Boot 4.1.1、Spring 7、Tomcat 11、MyBatis 3 和 Vue 3 管理控制台。Manager 与 Node 的职责及 Select → Extract → Transform → Load 模型沿用现有设计，协调状态由 ZooKeeper 管理。运行方式及验收范围见 [Spring Boot 运行基础](runtime-foundation.md)。

## 已落地的框架基础

| 领域 | 当前实现 |
| --- | --- |
| Java 与构建 | JDK 25、Maven 3.9.12、固定插件版本 |
| 应用框架 | Manager / Node 的 Boot 启动入口与生命周期 |
| HTTP | Spring MVC、内嵌 Tomcat 11、健康检查与优雅关闭 |
| 管理库访问 | MyBatis 3.5.19、MyBatis-Spring 4.1.0、HikariCP |
| 管理安全 | Spring Security、会话认证、角色权限、CSRF、BCrypt |
| 管理界面 | Vue 3、TypeScript、Vite |
| 节点通信 | Dubbo 3.3.6 直连、Hessian2、模型白名单 |
| 协调客户端 | ZooKeeper 3.9.6 |
| 文件传输 | Node HTTP 下载、HEAD、Range、路径约束 |

管理页面提供核心资源的新增、编辑、查询和删除，支持通道启停、流水线统计、日志和全局设置。复杂 Canal 参数、扩展处理器和系统参数使用 JSON 编辑入口，后续按业务使用频率继续细化表单。

## 下一阶段：真实同步闭环

首先选择 MySQL → MySQL 单向同步作为验收链路：

1. 固定管理库、源库、目标库与 ZooKeeper 版本，建立可重复的联调环境
2. 升级并验证 Canal 采集适配与业务 JDBC 驱动，覆盖日期、时区、无符号数、JSON、二进制、大字段与 DDL
3. 通过管理页面配置任务，核对增删改、表映射、列映射和批次确认
4. 验证通道启停、节点重启、网络中断、位点恢复与重放，核对重复写入和遗漏
5. 在真实 MySQL 上验收全部 MyBatis 映射和事务回滚，将管理库初始化整理为版本化迁移

完成标准是可重复运行、数据可核对、恢复语义明确。基础框架的启动与管理功能验证不能替代数据一致性验收。

## 后续架构演进

架构调整在同步闭环稳定后实施，优先解决职责、状态和恢复问题。

| 方向 | 实施内容 | 验收重点 |
| --- | --- | --- |
| 控制面 | 明确 Manager 的任务分配、配置版本、指令及审计职责 | 指令幂等、过期任务拒绝、状态可追踪 |
| 执行面 | 将 Node 生命周期、任务执行和资源释放收敛到清晰边界 | 节点重连、任务停止、故障恢复 |
| 协调机制 | 评估 Master + Worker 的租约、任期与主备策略 | 脑裂防护、旧主隔离、任务所有权 |
| 控制协议 | 设计类型明确的配置与指令 DTO、认证及协议版本 | 双端升级、请求身份、兼容性 |
| 数据传输 | 明确本地内存和远程批次传输的契约 | 背压、校验、重试、幂等与确认 |
| 可观测性 | 补充吞吐、延迟、错误、位点和资源指标 | 可定位故障、可解释恢复进度 |
| 扩展能力 | Oracle、双向同步、回环控制、补救、文件与自定义处理器 | 按场景建立支持矩阵 |
| 发布运维 | 容器构建、部署配置、持续集成和发布演练 | 全新环境部署及持续运行 |

协调组件的最终选择由租约与调度设计决定。Nacos 可提供配置和服务发现；任务所有权、主备任期和批次确认需要独立定义。

## 官方参考

- [Spring Boot 系统要求](https://docs.spring.io/spring-boot/system-requirements.html)
- [MyBatis-Spring](https://mybatis.org/spring/)
- [Vue 工程指南](https://vuejs.org/guide/quick-start.html)
- [Dubbo 发布信息](https://dubbo.apache.org/en/download/)
- [Dubbo 序列化类型检查](https://dubbo.apache.org/en/overview/mannual/java-sdk/tasks/security/class-check/)
- [ZooKeeper 发布策略](https://zookeeper.apache.org/releases/)
- [Canal 发布记录](https://github.com/alibaba/canal/releases)
