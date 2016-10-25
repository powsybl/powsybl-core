/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.online;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class OnlineWorkflowStartParameters implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_THREADS = 1;

    private int threads;
    private String jmxHost;
    private int jmxPort;
    private Class<? extends OnlineApplicationListenerFactory> listenerFactoryClasses = null;
    private Class<? extends OnlineWorkflowFactory> onlineWorkflowFactory;

    public static OnlineWorkflowStartParameters loadDefault() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("online-start-parameters");
        int threads = config.getIntProperty("threads", DEFAULT_THREADS);
        String jmxHost = config.getStringProperty("jmxHost");
        int jmxPort = config.getIntProperty("jmxPort");
        Class<? extends OnlineWorkflowFactory> onlineWorkflowFactory = config.getClassProperty("onlineWorkflowFactoryClass", OnlineWorkflowFactory.class, OnlineWorkflowFactoryImpl.class);
        Class<? extends OnlineApplicationListenerFactory> listenerFactoryClass = config.getClassProperty("listenerFactoryClasses", OnlineApplicationListenerFactory.class, null);

        return new OnlineWorkflowStartParameters(threads, jmxHost, jmxPort, listenerFactoryClass, onlineWorkflowFactory);
    }

    public OnlineWorkflowStartParameters(int threads, String jmxHost, int jmxPort, Class<? extends OnlineApplicationListenerFactory> listenerFactoryClass, Class<? extends OnlineWorkflowFactory> onlineWorkflowFactory) {
        Objects.requireNonNull(onlineWorkflowFactory);
        this.onlineWorkflowFactory = onlineWorkflowFactory;
        this.listenerFactoryClasses = listenerFactoryClass;
        setJmxHost(jmxHost);
        setJmxPort(jmxPort);
        setThreads(threads);
    }

    public int getThreads() {
        return threads;
    }

    public String getJmxHost() {
        return jmxHost;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    @Override
    public String toString() {
        return "OnlineWorkflowStartParameters [threads=" + threads + ", jmxHost=" + jmxHost + ", jmxPort=" + jmxPort
                + ", listenerFactoryClasses=" + listenerFactoryClasses + ", onlineWorkflowFactory="
                + onlineWorkflowFactory + "]";
    }

    public void setThreads(int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads must be greater than zero: " + threads);
        }
        this.threads = threads;
    }

    public void setJmxHost(String jmxHost) {
        Objects.requireNonNull(jmxHost);
        this.jmxHost = jmxHost;
    }

    public void setJmxPort(int jmxPort) {
        if (jmxPort <= 0) {
            throw new IllegalArgumentException("jmxPort must be greater than zero: " + jmxPort);
        }
        this.jmxPort = jmxPort;
    }

    public Class<? extends OnlineApplicationListenerFactory> getOnlineApplicationListenerFactoryClass() {
        return listenerFactoryClasses;
    }

    public Class<? extends OnlineWorkflowFactory> getOnlineWorkflowFactoryClass() {
        return onlineWorkflowFactory;
    }

}
