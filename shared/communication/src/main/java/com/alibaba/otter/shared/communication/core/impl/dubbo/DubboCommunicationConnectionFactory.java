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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.remoting.Constants;
import org.springframework.beans.factory.DisposableBean;

import com.alibaba.otter.shared.communication.core.CommunicationEndpoint;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnection;
import com.alibaba.otter.shared.communication.core.impl.connection.CommunicationConnectionFactory;
import com.alibaba.otter.shared.communication.core.model.CommunicationParam;

/**
 * dubbo rpc服务链接的factory
 *
 * @author jianghang 2011-11-29 上午11:13:31
 * @version 4.0.0
 */
public class DubboCommunicationConnectionFactory implements CommunicationConnectionFactory, DisposableBean {

    private final Map<String, ReferenceConfig<CommunicationEndpoint>> references = new ConcurrentHashMap<>();
    private int payload = Constants.DEFAULT_PAYLOAD;

    public CommunicationConnection createConnection(CommunicationParam params) {
        if (params == null) throw new IllegalArgumentException("param is null!");
        String serviceUrl = "dubbo://" + params.getIp() + ":" + params.getPort() + "/"
            + CommunicationEndpoint.class.getName();
        ReferenceConfig<CommunicationEndpoint> reference = references.computeIfAbsent(serviceUrl, address -> {
            ReferenceConfig<CommunicationEndpoint> configured = new ReferenceConfig<>();
            configured.setApplication(DubboCommunicationEndpoint.application());
            configured.setInterface(CommunicationEndpoint.class);
            configured.setUrl(address);
            configured.setProxy("jdk");
            configured.setCheck(false);
            configured.setTimeout(50000);
            configured.setParameters(Map.of("serialization", "hessian2", "client", "netty4",
                "payload", String.valueOf(payload), "iothreads", "4"));
            configured.get();
            return configured;
        });
        return new DubboCommunicationConnection(params, reference.get());
    }

    @Override
    public void destroy() {
        references.values().forEach(reference -> {
            if (!reference.getScopeModel().isDestroyed()) reference.destroy();
        });
        references.clear();
    }

    public void releaseConnection(CommunicationConnection connection) {
        // do nothing
    }

    // =============== setter / gettter ==================

    public void setPayload(int payload) {
        this.payload = payload;
    }

}
