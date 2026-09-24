# JDK 25 构建与发布

构建需要完整 JDK 25、Maven 3.9.12 及以上、Node.js 24 及以上和 npm。Maven Enforcer 校验 JDK 主版本为 25，Java 编译目标为 `release=25`。Node.js 用于构建管理页面，部署后的 Manager 直接提供已打包的静态资源。

## 构建

设置 `JAVA_HOME` 并确认 `java`、`node`、`npm` 位于 PATH。在项目根目录执行：

```bash
export MVN=mvn
"$MVN" -version
node --version
bash lib/install.sh
"$MVN" -B -ntp clean package -Dmaven.test.skip=true -Denv=release
```

其他环境将 `MVN` 替换为对应的 Maven 路径。`lib/install.sh` 安装仓库随附的 Oracle JDBC 和现有测试框架依赖。

Maven 在 `manager.web` 的 `generate-resources` 阶段执行 `npm ci`、`vue-tsc --noEmit` 和 `vite build`。前端构建到 `manager/web/target/frontend/static`，随后打入 Manager 的类路径。`package-lock.json` 固定依赖树。

发布包输出到根目录 `target`：

- `manager.deployer-4.2.19-SNAPSHOT.tar.gz`
- `node.deployer-4.2.19-SNAPSHOT.tar.gz`

默认开发构建省略 `-Denv=release`，分别在 `manager/deployer/target/manager` 与 `node/deployer/target/node` 生成发布目录。

## 前端开发

```bash
cd manager/web/frontend
npm ci
npm run dev
```

Vite 仅监听本机，`/api` 代理到 `http://127.0.0.1:8080`。后端需要按运行说明配置数据库和 ZooKeeper。单独构建前端使用 `npm run build`。

## 运行

Manager 和 Node 均使用完整 JDK 25。Unix 启动脚本优先使用 `JAVA`，其次使用 `JAVA_HOME`，最后查找 PATH，并检查 Java 主版本。默认堆大小为 512 MiB 至 2 GiB，使用 G1；Unix 使用 `JAVA_OPTS` 覆盖 JVM 参数，Windows 使用 `JAVA_MEM_OPTS` 覆盖内存参数。

应用以普通类路径启动，动态扩展编译使用 `jdk.compiler` 模块。框架、配置、账号、数据库初始化与验证边界见 [Spring Boot 运行基础](runtime-foundation.md)。

现有测试框架尚待迁移，本轮构建跳过其编译和执行；前端类型检查属于正常构建流程。未在 Windows 上执行启动脚本。
