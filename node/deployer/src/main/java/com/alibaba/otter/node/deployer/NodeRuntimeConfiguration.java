package com.alibaba.otter.node.deployer;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.alibaba.otter.node.common.config.ConfigClientService;
import com.alibaba.otter.node.etl.OtterController;

@Configuration(proxyBeanMethods = false)
public class NodeRuntimeConfiguration {

    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> downloadPort(ConfigClientService config,
                                                                                       Environment environment) {
        return factory -> {
            if (!environment.containsProperty("server.port")) {
                Integer port = config.currentNode().getParameters().getDownloadPort();
                factory.setPort(port == null ? 8081 : port);
            }
        };
    }

    @Bean
    public SmartLifecycle nodeLifecycle(OtterController controller) {
        return new SmartLifecycle() {
            private volatile boolean running;

            @Override
            public void start() {
                try {
                    controller.start();
                    running = true;
                } catch (Throwable e) {
                    throw new IllegalStateException("Node initialization failed", e);
                }
            }

            @Override
            public void stop() {
                if (running) {
                    try {
                        controller.stop();
                    } catch (Throwable e) {
                        throw new IllegalStateException("Node shutdown failed", e);
                    } finally {
                        running = false;
                    }
                }
            }

            @Override
            public boolean isRunning() {
                return running;
            }

            @Override
            public int getPhase() {
                // HTTP 服务就绪后再注册节点，关闭时先停止接收同步任务
                return Integer.MAX_VALUE;
            }
        };
    }
}
