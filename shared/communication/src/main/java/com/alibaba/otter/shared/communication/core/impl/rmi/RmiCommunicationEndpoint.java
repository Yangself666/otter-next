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

package com.alibaba.otter.shared.communication.core.impl.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.alibaba.otter.shared.communication.core.exception.CommunicationException;
import com.alibaba.otter.shared.communication.core.impl.AbstractCommunicationEndpoint;

/**
 * 基于rmi的endpoint的实现，包装了一个rmi remote对象
 *
 * @author jianghang 2011-9-9 下午07:06:25
 */
public class RmiCommunicationEndpoint extends AbstractCommunicationEndpoint {

    private String             host;
    private int                port                 = 1099;
    private Registry           registry;
    private RemoteCommunicationEndpoint remote;
    private boolean            ownsRegistry;
    private boolean            bound;
    private boolean            alwaysCreateRegistry = false;

    public RmiCommunicationEndpoint(){
    }

    public RmiCommunicationEndpoint(int port){
        this.port = port;
        initial();
    }

    public synchronized void initial() {
        if (bound) return;
        try {
            if (alwaysCreateRegistry) {
                registry = LocateRegistry.createRegistry(port);
                ownsRegistry = true;
            } else {
                registry = LocateRegistry.getRegistry(host, port);
                try {
                    registry.list();
                } catch (RemoteException e) {
                    if (host != null && !host.isBlank() && !"localhost".equals(host) && !"127.0.0.1".equals(host)) {
                        throw e;
                    }
                    registry = LocateRegistry.createRegistry(port);
                    ownsRegistry = true;
                }
            }
            remote = this::acceptEvent;
            registry.bind("endpoint", UnicastRemoteObject.exportObject(remote, 0));
            bound = true;
        } catch (RemoteException | java.rmi.AlreadyBoundException e) {
            destory();
            throw new CommunicationException("Rmi_Create_Error", e);
        }
    }

    public synchronized void destory() {
        try {
            if (registry != null && bound) {
                registry.unbind("endpoint");
            }
        } catch (Exception e) {
            // 注册中心可能已先于服务端关闭，继续释放本地导出对象
        } finally {
            if (remote != null) {
                try {
                    UnicastRemoteObject.unexportObject(remote, true);
                } catch (java.rmi.NoSuchObjectException e) {
                    // 尚未完成导出的对象无需再次释放
                }
                remote = null;
            }
            if (ownsRegistry && registry != null) {
                try {
                    UnicastRemoteObject.unexportObject(registry, true);
                } catch (java.rmi.NoSuchObjectException e) {
                    // 注册中心已经关闭
                }
            }
            registry = null;
            ownsRegistry = false;
            bound = false;
        }
    }

    // =============== setter / gettter ==================
    public void setAlwaysCreateRegistry(boolean alwaysCreateRegistry) {
        this.alwaysCreateRegistry = alwaysCreateRegistry;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

}
