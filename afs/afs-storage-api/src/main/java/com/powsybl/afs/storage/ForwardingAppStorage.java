/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * A storage implementation which simply delegates calls to another underlying AppStorage implementation.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ForwardingAppStorage implements AppStorage {

    private final AppStorage storage;

    public ForwardingAppStorage(AppStorage storage) {
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
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        return storage.createRootNodeIfNotExists(name, nodePseudoClass);
    }

    @Override
    public NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata) {
        return storage.createNode(parentNodeId, name, nodePseudoClass, description, version, genericMetadata);
    }

    @Override
    public void setMetadata(String nodeId, NodeGenericMetadata genericMetadata) {
        storage.setMetadata(nodeId, genericMetadata);
    }

    @Override
    public boolean isWritable(String nodeId) {
        return storage.isWritable(nodeId);
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) {
        return storage.getNodeInfo(nodeId);
    }

    @Override
    public void setDescription(String nodeId, String description) {
        storage.setDescription(nodeId, description);
    }

    @Override
    public void setConsistent(String nodeId) {
        storage.setConsistent(nodeId);
    }

    @Override
    public boolean isConsistent(String nodeId) {
        return storage.isConsistent(nodeId);
    }

    @Override
    public void updateModificationTime(String nodeId) {
        storage.updateModificationTime(nodeId);
    }

    @Override
    public List<NodeInfo> getChildNodes(String nodeId) {
        return storage.getChildNodes(nodeId);
    }

    @Override
    public List<NodeInfo> getInconsistentNodes() {
        return storage.getInconsistentNodes();
    }

    @Override
    public Optional<NodeInfo> getChildNode(String nodeId, String name) {
        return storage.getChildNode(nodeId, name);
    }

    @Override
    public Optional<NodeInfo> getParentNode(String nodeId) {
        return storage.getParentNode(nodeId);
    }

    @Override
    public void setParentNode(String nodeId, String newParentNodeId) {
        storage.setParentNode(nodeId, newParentNodeId);
    }

    @Override
    public String deleteNode(String nodeId) {
        return storage.deleteNode(nodeId);
    }

    @Override
    public void renameNode(String nodeId, String name) {
        storage.renameNode(nodeId, name);
    }

    @Override
    public Optional<InputStream> readBinaryData(String nodeId, String name) {
        return storage.readBinaryData(nodeId, name);
    }

    @Override
    public OutputStream writeBinaryData(String nodeId, String name) {
        return storage.writeBinaryData(nodeId, name);
    }

    @Override
    public boolean dataExists(String nodeId, String name) {
        return storage.dataExists(nodeId, name);
    }

    @Override
    public Set<String> getDataNames(String nodeId) {
        return storage.getDataNames(nodeId);
    }

    @Override
    public boolean removeData(String nodeId, String name) {
        return storage.removeData(nodeId, name);
    }

    @Override
    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        storage.createTimeSeries(nodeId, metadata);
    }

    @Override
    public Set<String> getTimeSeriesNames(String nodeId) {
        return storage.getTimeSeriesNames(nodeId);
    }

    @Override
    public boolean timeSeriesExists(String nodeId, String timeSeriesName) {
        return storage.timeSeriesExists(nodeId, timeSeriesName);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames) {
        return storage.getTimeSeriesMetadata(nodeId, timeSeriesNames);
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId) {
        return storage.getTimeSeriesDataVersions(nodeId);
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName) {
        return storage.getTimeSeriesDataVersions(nodeId, timeSeriesName);
    }

    @Override
    public Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        return storage.getDoubleTimeSeriesData(nodeId, timeSeriesNames, version);
    }

    @Override
    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleDataChunk> chunks) {
        storage.addDoubleTimeSeriesData(nodeId, version, timeSeriesName, chunks);
    }

    @Override
    public Map<String, List<StringDataChunk>> getStringTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        return storage.getStringTimeSeriesData(nodeId, timeSeriesNames, version);
    }

    @Override
    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringDataChunk> chunks) {
        storage.addStringTimeSeriesData(nodeId, version, timeSeriesName, chunks);
    }

    @Override
    public void clearTimeSeries(String nodeId) {
        storage.clearTimeSeries(nodeId);
    }

    @Override
    public void addDependency(String nodeId, String name, String toNodeId) {
        storage.addDependency(nodeId, name, toNodeId);
    }

    @Override
    public Set<NodeInfo> getDependencies(String nodeId, String name) {
        return storage.getDependencies(nodeId, name);
    }

    @Override
    public Set<NodeDependency> getDependencies(String nodeId) {
        return storage.getDependencies(nodeId);
    }

    @Override
    public Set<NodeInfo> getBackwardDependencies(String nodeId) {
        return storage.getBackwardDependencies(nodeId);
    }

    @Override
    public void removeDependency(String nodeId, String name, String toNodeId) {
        storage.removeDependency(nodeId, name, toNodeId);
    }

    @Override
    public void flush() {
        storage.flush();
    }

    @Override
    public boolean isClosed() {
        return storage.isClosed();
    }

    @Override
    public void close() {
        storage.close();
    }

    public EventsBus getEventsBus() {
        return storage.getEventsBus();
    }
}
