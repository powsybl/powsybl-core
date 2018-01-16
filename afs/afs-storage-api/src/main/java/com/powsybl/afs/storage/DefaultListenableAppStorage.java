/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.afs.storage.events.*;
import com.powsybl.math.timeseries.DoubleArrayChunk;
import com.powsybl.math.timeseries.StringArrayChunk;
import com.powsybl.math.timeseries.TimeSeriesMetadata;

import java.io.OutputStream;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorage extends ForwardingAppStorage implements ListenableAppStorage {

    private final AppStorageListenerList listeners = new AppStorageListenerList();

    public DefaultListenableAppStorage(AppStorage storage) {
        super(storage);
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        NodeInfo nodeInfo = super.createRootNodeIfNotExists(name, nodePseudoClass);
        listeners.notify(new NodeCreated(nodeInfo.getId()));
        return nodeInfo;
    }

    @Override
    public NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata) {
        NodeInfo nodeInfo = super.createNode(parentNodeId, name, nodePseudoClass, description, version, genericMetadata);
        listeners.notify(new NodeCreated(nodeInfo.getId()));
        return nodeInfo;
    }

    @Override
    public void setDescription(String nodeId, String description) {
        super.setDescription(nodeId, description);
        listeners.notify(new NodeDescriptionUpdated(nodeId, description));
    }

    @Override
    public void setParentNode(String nodeId, String newParentNodeId) {
        super.setParentNode(nodeId, newParentNodeId);
        listeners.notify(new ParentChanged(nodeId));
    }

    @Override
    public void deleteNode(String nodeId) {
        super.deleteNode(nodeId);
        listeners.notify(new NodeRemoved(nodeId));
    }

    @Override
    public OutputStream writeBinaryData(String nodeId, String name) {
        OutputStream os = super.writeBinaryData(nodeId, name);
        listeners.notify(new NodeDataUpdated(nodeId, name));
        return os;
    }

    @Override
    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        super.createTimeSeries(nodeId, metadata);
        listeners.notify(new TimeSeriesCreated(nodeId, metadata.getName()));
    }

    @Override
    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        super.addDoubleTimeSeriesData(nodeId, version, timeSeriesName, chunks);
        listeners.notify(new TimeSeriesDataUpdated(nodeId, timeSeriesName));
    }

    @Override
    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        super.addStringTimeSeriesData(nodeId, version, timeSeriesName, chunks);
        listeners.notify(new TimeSeriesDataUpdated(nodeId, timeSeriesName));
    }

    @Override
    public void clearTimeSeries(String nodeId) {
        super.clearTimeSeries(nodeId);
        listeners.notify(new TimeSeriesCleared(nodeId));
    }

    @Override
    public void addDependency(String nodeId, String name, String toNodeId) {
        super.addDependency(nodeId, name, toNodeId);
        listeners.notify(new DependencyAdded(nodeId, name));
    }

    @Override
    public void removeDependency(String nodeId, String name, String toNodeId) {
        super.removeDependency(nodeId, name, toNodeId);
        listeners.notify(new DependencyRemoved(nodeId, name));
    }

    @Override
    public void addListener(Object target, AppStorageListener l) {
        listeners.add(target, l);
    }

    @Override
    public void removeListeners(Object target) {
        listeners.removeAll(target);
    }
}
