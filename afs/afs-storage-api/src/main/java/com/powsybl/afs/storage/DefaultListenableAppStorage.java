/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.math.timeseries.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultListenableAppStorage implements ListenableAppStorage {

    private final AppStorage storage;

    private final WeakHashMap<Object, List<AppStorageListener>> listeners = new WeakHashMap<>();

    public DefaultListenableAppStorage(AppStorage storage) {
        this.storage = Objects.requireNonNull(storage);
    }

    @Override
    public String getFileSystemName() {
        return storage.getFileSystemName();
    }

    @Override
    public boolean isRemote() {
        return storage.isRemote();
    }

    @Override
    public NodeId fromString(String str) {
        return storage.fromString(str);
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        return storage.createRootNodeIfNotExists(name, nodePseudoClass);
    }

    @Override
    public NodeInfo createNode(NodeId parentNodeId, String name, String nodePseudoClass, String description, int version,
                               Map<String, String> stringMetadata, Map<String, Double> doubleMetadata,
                               Map<String, Integer> intMetadata, Map<String, Boolean> booleanMetadata) {
        NodeInfo nodeInfo = storage.createNode(parentNodeId, name, nodePseudoClass, description, version, stringMetadata, doubleMetadata, intMetadata, booleanMetadata);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.nodeCreated(nodeInfo.getId()));
        return nodeInfo;
    }

    @Override
    public boolean isWritable(NodeId nodeId) {
        return storage.isWritable(nodeId);
    }

    @Override
    public NodeInfo getNodeInfo(NodeId nodeId) {
        return storage.getNodeInfo(nodeId);
    }

    @Override
    public void setDescription(NodeId nodeId, String description) {
        storage.setDescription(nodeId, description);
    }

    @Override
    public List<NodeInfo> getChildNodes(NodeId nodeId) {
        return storage.getChildNodes(nodeId);
    }

    @Override
    public NodeInfo getChildNode(NodeId nodeId, String name) {
        return storage.getChildNode(nodeId, name);
    }

    @Override
    public NodeInfo getParentNode(NodeId nodeId) {
        return storage.getParentNode(nodeId);
    }

    @Override
    public void setParentNode(NodeId nodeId, NodeId newParentNodeId) {
        storage.setParentNode(nodeId, newParentNodeId);
    }

    @Override
    public void deleteNode(NodeId nodeId) {
        storage.deleteNode(nodeId);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.nodeRemoved(nodeId));
    }

    @Override
    public Reader readStringData(NodeId nodeId, String name) {
        return storage.readStringData(nodeId, name);
    }

    @Override
    public Writer writeStringData(NodeId nodeId, String name) {
        Writer writer = storage.writeStringData(nodeId, name);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.nodeDataUpdated(nodeId, name));
        return writer;
    }

    @Override
    public InputStream readBinaryData(NodeId nodeId, String name) {
        return storage.readBinaryData(nodeId, name);
    }

    @Override
    public OutputStream writeBinaryData(NodeId nodeId, String name) {
        OutputStream os = storage.writeBinaryData(nodeId, name);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.nodeDataUpdated(nodeId, name));
        return os;
    }

    @Override
    public boolean dataExists(NodeId nodeId, String name) {
        return storage.dataExists(nodeId, name);
    }

    @Override
    public void createTimeSeries(NodeId nodeId, TimeSeriesMetadata metadata) {
        storage.createTimeSeries(nodeId, metadata);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.timeSeriesCreated(nodeId, metadata.getName()));
    }

    @Override
    public Set<String> getTimeSeriesNames(NodeId nodeId) {
        return storage.getTimeSeriesNames(nodeId);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(NodeId nodeId, Set<String> timeSeriesNames) {
        return storage.getTimeSeriesMetadata(nodeId, timeSeriesNames);
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return storage.getDoubleTimeSeries(nodeId, timeSeriesNames, version);
    }

    @Override
    public void addDoubleTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        storage.addDoubleTimeSeriesData(nodeId, version, timeSeriesName, chunks);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.timeSeriesDataUpdated(nodeId, timeSeriesName));
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return storage.getStringTimeSeries(nodeId, timeSeriesNames, version);
    }

    @Override
    public void addStringTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        storage.addStringTimeSeriesData(nodeId, version, timeSeriesName, chunks);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.timeSeriesDataUpdated(nodeId, timeSeriesName));
    }

    @Override
    public void removeAllTimeSeries(NodeId nodeId) {
        storage.removeAllTimeSeries(nodeId);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.timeSeriesRemoved(nodeId));
    }

    @Override
    public NodeInfo getDependency(NodeId nodeId, String name) {
        return storage.getDependency(nodeId, name);
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        storage.addDependency(nodeId, name, toNodeId);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.dependencyAdded(nodeId, name));
    }

    @Override
    public List<NodeInfo> getDependencies(NodeId nodeId) {
        return storage.getDependencies(nodeId);
    }

    @Override
    public List<NodeInfo> getBackwardDependencies(NodeId nodeId) {
        return storage.getBackwardDependencies(nodeId);
    }

    @Override
    public void flush() {
        storage.flush();
    }

    @Override
    public void close() {
        storage.close();
    }

    @Override
    public void addListener(Object target, AppStorageListener l) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(l);
        listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(l);
    }

    @Override
    public void removeListeners(Object target) {
        Objects.requireNonNull(target);
        listeners.remove(target);
    }
}
