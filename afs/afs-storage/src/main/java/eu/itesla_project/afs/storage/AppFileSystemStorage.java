/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.storage;

import eu.itesla_project.iidm.datasource.DataSource;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

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

    void deleteNode(NodeId nodeId);

    String getStringAttribute(NodeId nodeId, String name);

    void setStringAttribute(NodeId nodeId, String name, String value);

    Reader readStringAttribute(NodeId nodeId, String name);

    Writer writeStringAttribute(NodeId nodeId, String name);

    DataSource getDataSourceAttribute(NodeId nodeId, String name);

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

    // transaction management

    void commit();

    void rollback();

    @Override
    void close();
}
