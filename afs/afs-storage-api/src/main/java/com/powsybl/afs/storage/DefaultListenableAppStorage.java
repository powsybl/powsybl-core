/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.commons.datasource.DataSource;
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
    public NodeId fromString(String str) {
        return storage.fromString(str);
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        return storage.createRootNodeIfNotExists(name, nodePseudoClass);
    }

    @Override
    public NodeId createNode(NodeId parentNodeId, String name, String nodePseudoClass) {
        NodeId nodeId = storage.createNode(parentNodeId, name, nodePseudoClass);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.nodeCreated(nodeId));
        return nodeId;
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        return storage.getNodeName(nodeId);
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        return storage.getNodePseudoClass(nodeId);
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
    public List<NodeId> getChildNodes(NodeId nodeId) {
        return storage.getChildNodes(nodeId);
    }

    @Override
    public List<NodeInfo> getChildNodesInfo(NodeId nodeId) {
        return storage.getChildNodesInfo(nodeId);
    }

    @Override
    public NodeId getChildNode(NodeId nodeId, String name) {
        return storage.getChildNode(nodeId, name);
    }

    @Override
    public NodeInfo getChildNodeInfo(NodeId nodeId, String name) {
        return storage.getChildNodeInfo(nodeId, name);
    }

    @Override
    public NodeId getParentNode(NodeId nodeId) {
        return storage.getParentNode(nodeId);
    }

    @Override
    public NodeInfo getParentNodeInfo(NodeId nodeId) {
        return storage.getParentNodeInfo(nodeId);
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
    public String getStringAttribute(NodeId nodeId, String name) {
        return storage.getStringAttribute(nodeId, name);
    }

    @Override
    public void setStringAttribute(NodeId nodeId, String name, String value) {
        storage.setStringAttribute(nodeId, name, value);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.attributeUpdated(nodeId, name));
    }

    @Override
    public Reader readStringAttribute(NodeId nodeId, String name) {
        return storage.readStringAttribute(nodeId, name);
    }

    @Override
    public Writer writeStringAttribute(NodeId nodeId, String name) {
        return storage.writeStringAttribute(nodeId, name);
    }

    @Override
    public OptionalInt getIntAttribute(NodeId nodeId, String name) {
        return storage.getIntAttribute(nodeId, name);
    }

    @Override
    public void setIntAttribute(NodeId nodeId, String name, int value) {
        storage.setIntAttribute(nodeId, name, value);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.attributeUpdated(nodeId, name));
    }

    @Override
    public OptionalDouble getDoubleAttribute(NodeId nodeId, String name) {
        return storage.getDoubleAttribute(nodeId, name);
    }

    @Override
    public void setDoubleAttribute(NodeId nodeId, String name, double value) {
        storage.setDoubleAttribute(nodeId, name, value);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.attributeUpdated(nodeId, name));
    }

    @Override
    public Optional<Boolean> getBooleanAttribute(NodeId nodeId, String name) {
        return storage.getBooleanAttribute(nodeId, name);
    }

    @Override
    public void setBooleanAttribute(NodeId nodeId, String name, boolean value) {
        storage.setBooleanAttribute(nodeId, name, value);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.attributeUpdated(nodeId, name));
    }

    @Override
    public DataSource getDataSourceAttribute(NodeId nodeId, String name) {
        return storage.getDataSourceAttribute(nodeId, name);
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
    public NodeId getDependency(NodeId nodeId, String name) {
        return storage.getDependency(nodeId, name);
    }

    @Override
    public NodeInfo getDependencyInfo(NodeId nodeId, String name) {
        return storage.getDependencyInfo(nodeId, name);
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        storage.addDependency(nodeId, name, toNodeId);
        listeners.values().stream().flatMap(List::stream).forEach(l -> l.dependencyAdded(nodeId, name));
    }

    @Override
    public List<NodeId> getDependencies(NodeId nodeId) {
        return storage.getDependencies(nodeId);
    }

    @Override
    public List<NodeInfo> getDependenciesInfo(NodeId nodeId) {
        return storage.getDependenciesInfo(nodeId);
    }

    @Override
    public List<NodeId> getBackwardDependencies(NodeId nodeId) {
        return storage.getBackwardDependencies(nodeId);
    }

    @Override
    public List<NodeInfo> getBackwardDependenciesInfo(NodeId nodeId) {
        return storage.getBackwardDependenciesInfo(nodeId);
    }

    @Override
    public InputStream readFromCache(NodeId nodeId, String key) {
        return storage.readFromCache(nodeId, key);
    }

    @Override
    public OutputStream writeToCache(NodeId nodeId, String key) {
        return storage.writeToCache(nodeId, key);
    }

    @Override
    public void invalidateCache(NodeId nodeId, String key) {
        storage.invalidateCache(nodeId, key);
    }

    @Override
    public void invalidateCache() {
        storage.invalidateCache();
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
        listeners.computeIfAbsent(target, k -> new ArrayList<>()).add(l);
    }

    @Override
    public void removeListeners(Object target) {
        listeners.remove(target);
    }
}
