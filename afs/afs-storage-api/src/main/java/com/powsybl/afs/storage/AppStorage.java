/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.commons.PowsyblException;
import com.powsybl.timeseries.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * A storage which maintains data for an application file system. This is a low level object,
 * and should not be used by users of the AFS API, but only when extending the AFS API
 * with a new storage implementation or new file types for example.
 *
 * <p>
 * An AppStorage implements low level methods to walk through a filesystem and to write and read data from this filesystem.
 * It relies on nodes uniquely identified by and ID.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface AppStorage extends AutoCloseable {

    String getFileSystemName();

    boolean isRemote();

    /**
     * Returns the root node of the tree, creating it if it does not exist.
     */
    NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass);

    /**
     * Creates a new node in the tree under a parent node. Returns {@code NodeInfo} corresponding to the newly created node.
     * The new node is by default inconsistent, {@link #setConsistent(String nodeId)} method should explicitly be called to set it consistent.
     */
    NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata);

    /**
     * Update a node metadata
     *
     * @param nodeId          id of the node
     * @param genericMetadata new metadata that will override previous ones
     */
    default void setMetadata(String nodeId, NodeGenericMetadata genericMetadata) {
        throw new PowsyblException("Not implemented");
    }

    boolean isWritable(String nodeId);

    /**
     * Return true if the node with ID {@code nodeId} is consistent, otherwise it returns false .
     */
    default boolean isConsistent(String nodeId) {
        throw new PowsyblException("Not implemented");
    }

    /**
     * Gets NodeInfo object for the node with ID {@code nodeId}.
     */
    NodeInfo getNodeInfo(String nodeId);

    void setDescription(String nodeId, String description);

    /**
     * mark the node with ID {@code nodeId} as consistent node.
     */
    default void setConsistent(String nodeId) {
        throw new PowsyblException("Not implemented");
    }

    void updateModificationTime(String nodeId);

    /**
     * Gets {@code NodeInfo} for consistent child nodes of the node with ID {@code nodeId}.
     */
    List<NodeInfo> getChildNodes(String nodeId);

    /**
     * Gets {@code NodeInfo} for all inconsistent nodes.
     */
    default List<NodeInfo> getInconsistentNodes() {
        return Collections.emptyList();
    }

    /**
     * Gets {@code NodeInfo} for child node with name {@code name} of the node with ID {@code nodeId}, empty if such a node does not exist or node is inconsistent.
     */
    Optional<NodeInfo> getChildNode(String nodeId, String name);

    /**
     * Gets {@code NodeInfo} for parent node of the node with ID {@code nodeId}, empty if such a node does not exist or inconsistent.
     */
    Optional<NodeInfo> getParentNode(String nodeId);

    /**
     * Sets new parent node for the node with ID {@code nodeId}.
     */
    void setParentNode(String nodeId, String newParentNodeId);

    /**
     * Deletes the node with ID {@code nodeId}.
     */
    String deleteNode(String nodeId);

    /**
     * Rename the node with ID {@code nodeId}
     */
    default void renameNode(String nodeId, String name) {
        throw new UnsupportedOperationException("To implement");
    }

    /**
     * Reads data associated to the node with ID {@code nodeId}. A node may have several data blobs associated to it,
     * with different names. The parameters {@code name} specifies which of those data is requested.
     */
    Optional<InputStream> readBinaryData(String nodeId, String name);

    /**
     * Returns an {@code OutputStream} to write data associated to the node with ID {@code nodeId}.
     * A node may have several data blobs associated to it, with different names.
     */
    OutputStream writeBinaryData(String nodeId, String name);

    /**
     * Returns {@code true} if data named {@code name} associated with the node with ID {@code nodeId} exists.
     */
    boolean dataExists(String nodeId, String name);

    /**
     * Returns the lists of names of data associated to the node with ID {@code nodeId}.
     */
    Set<String> getDataNames(String nodeId);

    /**
     * Removes the data blob named {@code name} associated with the node with ID {@code nodeId}.
     */
    boolean removeData(String nodeId, String name);

    /**
     * Creates a time series associated with node with ID {@code nodeId}.
     */
    void createTimeSeries(String nodeId, TimeSeriesMetadata metadata);

    /**
     * Returns names of all time series associated with node with ID {@code nodeId}.
     */
    Set<String> getTimeSeriesNames(String nodeId);

    /**
     * Returns {@code true} if a time series named {@code timeSeriesName} associated with the node with ID {@code nodeId} exists.
     */
    boolean timeSeriesExists(String nodeId, String timeSeriesName);

    /**
     * Returns metadata of time series associated with node with ID {@code nodeId} and with name in {@code timeSeriesNames}.
     */
    List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames);

    /**
     * Gets versions of time series data associated with node with ID {@code nodeId}.
     */
    Set<Integer> getTimeSeriesDataVersions(String nodeId);

    /**
     * Gets versions of data for the time series with name {@code timeSeriesName} associated with node with ID {@code nodeId}.
     */
    Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName);

    /**
     * Gets data (double) for the time series with names {@code timeSeriesNames} associated with node with ID {@code nodeId}.
     */
    Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version);

    /**
     * Adds data (double) to the time series with names {@code timeSeriesNames} associated with node with ID {@code nodeId}.
     */
    void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleDataChunk> chunks);

    /**
     * Gets data (string) for the time series with names {@code timeSeriesNames} associated with node with ID {@code nodeId}.
     */
    Map<String, List<StringDataChunk>> getStringTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version);

    /**
     * Adds data (string) to the time series with names {@code timeSeriesNames} associated with node with ID {@code nodeId}.
     */
    void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringDataChunk> chunks);

    /**
     * Deletes time series associated with node with ID {@code nodeId}
     */
    void clearTimeSeries(String nodeId);

    /**
     * Adds a dependency from node with ID {@code nodeId} to node with ID {@code toNodeId}.
     * The dependency will be associated with the specified {@code name}.
     */
    void addDependency(String nodeId, String name, String toNodeId);

    /**
     * Gets {@code NodeInfo} objects for dependencies of node with ID {@code nodeId}, and associated to the dependency name {@code name}.
     */
    Set<NodeInfo> getDependencies(String nodeId, String name);

    /**
     * Gets all dependencies ({@code NodeDependency} objects) of node with ID {@code nodeId}.
     */
    Set<NodeDependency> getDependencies(String nodeId);

    /**
     * Gets {@code NodeInfo} objects of nodes which depend on the node with ID {@code nodeId}.
     */
    Set<NodeInfo> getBackwardDependencies(String nodeId);

    /**
     * Removes a dependency named {@code name} from node with ID {@code nodeId} to node with ID {@code toNodeId}.
     */
    void removeDependency(String nodeId, String name, String toNodeId);

    /**
     * Get the events store.
     */
    EventsBus getEventsBus();

    /**
     * Flush any changes to underlying storage.
     */
    void flush();

    /**
     * Check if storage is closed.
     */
    boolean isClosed();

    /**
     * Closes any resource associated with this storage.
     */
    @Override
    void close();
}
