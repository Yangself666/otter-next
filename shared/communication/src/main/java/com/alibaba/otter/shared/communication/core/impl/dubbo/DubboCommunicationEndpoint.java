/*
 * Copyright (C) 2010-2101 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.otter.shared.communication.core.impl.dubbo;

import java.util.Map;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.remoting.Constants;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.impl.AbstractCommunicationEndpoint;

/**
 * 基于dubbo的endpoint实现,仅仅使用了dubb的rpc工具
 *
 * @author jianghang 2011-11-29 上午11:08:29
 * @version 4.0.0
 */
public class DubboCommunicationEndpoint extends AbstractCommunicationEndpoint {

    private final org.apache.dubbo.rpc.model.ApplicationModel applicationModel =
        org.apache.dubbo.rpc.model.ApplicationModel.defaultModel();
    private ServiceConfig<CommunicationEndpoint> service;
    private int port = 2088;
    private int payload = Constants.DEFAULT_PAYLOAD;

    public DubboCommunicationEndpoint(){
    }

    public DubboCommunicationEndpoint(int port){
        this.port = port;
    }

    static ApplicationConfig application() {
        ApplicationConfig application = new ApplicationConfig(System.getProperty("appName", "otter"));
        application.setQosEnable(false);
        return application;
    }

    public synchronized void initial() {
        if (service != null) return;
        ProtocolConfig protocol = new ProtocolConfig("dubbo", port);
        protocol.setSerialization("hessian2");
        protocol.setServer("netty4");
        protocol.setThreads(50);
        protocol.setPayload(payload);
        ServiceConfig<CommunicationEndpoint> configured = new ServiceConfig<>();
        configured.setApplication(application());
        configured.setRegistry(new RegistryConfig(RegistryConfig.NO_AVAILABLE));
        configured.setProtocol(protocol);
        configured.setInterface(CommunicationEndpoint.class);
        configured.setRef(this);
        configured.setProxy("jdk");
        configured.setParameters(Map.of("heartbeat", "5000", "iothreads", "4"));
        configured.export();
        service = configured;
    }

    public synchronized void destory() {
        if (service != null) {
            service.unexport();
            service = null;
        }
        // 关闭应用级 RPC 资源，释放服务端端口及通信线程
        if (!applicationModel.isDestroyed()) applicationModel.destroy();
    }

    // =============== setter / gettter ==================

    public void setPort(int port) {
        this.port = port;
    }

    public void setPayload(int payload) {
        this.payload = payload;
    }

}
