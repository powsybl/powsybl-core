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
public interface AppFileSystemStorage extends AutoCloseable {

    NodeId fromString(String str);

    String getNodeName(NodeId nodeId);

    boolean isWritable(NodeId nodeId);

    NodeId createNode(NodeId parentNodeId, String name, String nodePseudoClass);

    String getNodePseudoClass(NodeId nodeId);

    List<NodeId> getChildNodes(NodeId nodeId);

    NodeId getChildNode(NodeId nodeId, String name);

    NodeId getParentNode(NodeId nodeId);

    void setParentNode(NodeId nodeId, NodeId newParentNodeId);

    void deleteNode(NodeId nodeId);

    String getStringAttribute(NodeId nodeId, String name);

    void setStringAttribute(NodeId nodeId, String name, String value);

    Reader readStringAttribute(NodeId nodeId, String name);

    Writer writeStringAttribute(NodeId nodeId, String name);

    OptionalInt getIntAttribute(NodeId nodeId, String name);

    void setIntAttribute(NodeId nodeId, String name, int value);

    OptionalDouble getDoubleAttribute(NodeId nodeId, String name);

    void setDoubleAttribute(NodeId nodeId, String name, double value);

    Optional<Boolean> getBooleanAttribute(NodeId nodeId, String name);

    void setBooleanAttribute(NodeId nodeId, String name, boolean value);

    DataSource getDataSourceAttribute(NodeId nodeId, String name);

    static UnsupportedOperationException createNotImplementedException() {
        return new UnsupportedOperationException("Not implemented");
    }

    default void createTimeSeries(NodeId nodeId, TimeSeriesMetadata metadata) {
        throw createNotImplementedException();
    }

    default Set<String> getTimeSeriesNames(NodeId nodeId) {
        throw createNotImplementedException();
    }

    default List<TimeSeriesMetadata> getTimeSeriesMetadata(NodeId nodeId, Set<String> timeSeriesNames) {
        throw createNotImplementedException();
    }

    default List<DoubleTimeSeries> getDoubleTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        throw createNotImplementedException();
    }

    default void addDoubleTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        throw createNotImplementedException();
    }

    default List<StringTimeSeries> getStringTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        throw createNotImplementedException();
    }

    default void addStringTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        throw createNotImplementedException();
    }

    default void removeAllTimeSeries(NodeId nodeId) {
        throw createNotImplementedException();
    }

    NodeId getDependency(NodeId nodeId, String name);

    void addDependency(NodeId nodeId, String name, NodeId toNodeId);

    List<NodeId> getDependencies(NodeId nodeId);

    List<NodeId> getBackwardDependencies(NodeId nodeId);

    NodeId getRootNode();

    NodeId getProjectRootNode(NodeId projectNodeId);

    // cache management

    InputStream readFromCache(NodeId nodeId, String key);

    OutputStream writeToCache(NodeId nodeId, String key);

    void invalidateCache(NodeId nodeId, String key);

    void invalidateCache();

    void flush();

    @Override
    void close();
}
