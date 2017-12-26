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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface AppStorage extends AutoCloseable {

    String getFileSystemName();

    default boolean isRemote() {
        throw new UnsupportedOperationException("Not implemented");
    }

    NodeId fromString(String str);

    NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass);

    NodeInfo createNode(NodeId parentNodeId, String name, String nodePseudoClass, String description, int version, Map<String, String> stringMetadata,
                        Map<String, Double> doubleMetadata, Map<String, Integer> intMetadata, Map<String, Boolean> booleanMetadata);

    boolean isWritable(NodeId nodeId);

    NodeInfo getNodeInfo(NodeId nodeId);

    void setDescription(NodeId nodeId, String description);

    List<NodeInfo> getChildNodes(NodeId nodeId);

    NodeInfo getChildNode(NodeId nodeId, String name);

    NodeInfo getParentNode(NodeId nodeId);

    void setParentNode(NodeId nodeId, NodeId newParentNodeId);

    void deleteNode(NodeId nodeId);

    Reader readStringData(NodeId nodeId, String name);

    Writer writeStringData(NodeId nodeId, String name);

    InputStream readBinaryData(NodeId nodeId, String name);

    OutputStream writeBinaryData(NodeId nodeId, String name);

    boolean dataExists(NodeId nodeId, String name);

    void createTimeSeries(NodeId nodeId, TimeSeriesMetadata metadata);

    Set<String> getTimeSeriesNames(NodeId nodeId);

    List<TimeSeriesMetadata> getTimeSeriesMetadata(NodeId nodeId, Set<String> timeSeriesNames);

    List<DoubleTimeSeries> getDoubleTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version);

    void addDoubleTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks);

    List<StringTimeSeries> getStringTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version);

    void addStringTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks);

    void removeAllTimeSeries(NodeId nodeId);

    NodeInfo getDependency(NodeId nodeId, String name);

    void addDependency(NodeId nodeId, String name, NodeId toNodeId);

    List<NodeInfo> getDependencies(NodeId nodeId);

    List<NodeInfo> getBackwardDependencies(NodeId nodeId);

    void flush();

    @Override
    void close();
}
