package com.alibaba.otter.node.deployer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = "com.alibaba.otter.node.deployer")
@ImportResource("classpath:otter-node-context.xml")
public class OtterLauncher {

    public static void main(String[] args) {
        // 由 Boot 顺序停止任务与通信资源，避免多个 JVM 关闭钩子并发释放连接
        System.setProperty("dubbo.shutdownHook.listenIgnore", "true");
        SpringApplication application = new SpringApplication(OtterLauncher.class);
        application.addInitializers(com.alibaba.otter.node.etl.OtterContextLocator::initialize);
        try {
            application.run(args);
        } catch (RuntimeException | Error error) {
            org.apache.dubbo.rpc.model.ApplicationModel.defaultModel().destroy();
            throw error;
        }
    }
}
