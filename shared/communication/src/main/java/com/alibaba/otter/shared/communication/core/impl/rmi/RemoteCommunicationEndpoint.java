package com.alibaba.otter.shared.communication.core.impl.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.alibaba.otter.shared.communication.core.model.Event;

/** RMI 传输边界使用 JDK Remote 接口 */
public interface RemoteCommunicationEndpoint extends Remote {

    Object acceptEvent(Event event) throws RemoteException;
}
