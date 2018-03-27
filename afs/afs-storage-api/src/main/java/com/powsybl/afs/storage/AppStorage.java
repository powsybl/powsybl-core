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
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface AppStorage extends AutoCloseable {

    String getFileSystemName();

    boolean isRemote();

    NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass);

    NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata);

    boolean isWritable(String nodeId);

    NodeInfo getNodeInfo(String nodeId);

    void setDescription(String nodeId, String description);

    void updateModificationTime(String nodeId);

    List<NodeInfo> getChildNodes(String nodeId);

    Optional<NodeInfo> getChildNode(String nodeId, String name);

    Optional<NodeInfo> getParentNode(String nodeId);

    void setParentNode(String nodeId, String newParentNodeId);

    String deleteNode(String nodeId);

    Optional<InputStream> readBinaryData(String nodeId, String name);

    OutputStream writeBinaryData(String nodeId, String name);

    boolean dataExists(String nodeId, String name);

    Set<String> getDataNames(String nodeId);

    boolean removeData(String nodeId, String name);

    void createTimeSeries(String nodeId, TimeSeriesMetadata metadata);

    Set<String> getTimeSeriesNames(String nodeId);

    boolean timeSeriesExists(String nodeId, String timeSeriesName);

    List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames);

    Set<Integer> getTimeSeriesDataVersions(String nodeId);

    Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName);

    List<DoubleTimeSeries> getDoubleTimeSeries(String nodeId, Set<String> timeSeriesNames, int version);

    void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks);

    List<StringTimeSeries> getStringTimeSeries(String nodeId, Set<String> timeSeriesNames, int version);

    void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks);

    void clearTimeSeries(String nodeId);

    void addDependency(String nodeId, String name, String toNodeId);

    Set<NodeInfo> getDependencies(String nodeId, String name);

    Set<NodeDependency> getDependencies(String nodeId);

    Set<NodeInfo> getBackwardDependencies(String nodeId);

    void removeDependency(String nodeId, String name, String toNodeId);

    void flush();

    @Override
    void close();
}
