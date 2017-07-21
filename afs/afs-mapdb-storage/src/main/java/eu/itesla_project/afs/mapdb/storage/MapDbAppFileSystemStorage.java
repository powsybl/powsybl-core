/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.mapdb.storage;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.afs.storage.PseudoClass;
import eu.itesla_project.iidm.datasource.DataSource;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppFileSystemStorage implements AppFileSystemStorage {

    public static MapDbAppFileSystemStorage createHeap(String fileSystemName) {
        DBMaker.Maker maker = DBMaker.heapDB();
        return new MapDbAppFileSystemStorage(fileSystemName, maker, () -> maker.make());
    }

    public static MapDbAppFileSystemStorage createMmapFile(String fileSystemName, File dbFile) {
        DBMaker.Maker maker = DBMaker.fileDB(dbFile);
        return new MapDbAppFileSystemStorage(fileSystemName, maker, () -> maker
                .fileMmapEnable()
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .transactionEnable()
                .make());
    }

    private final DBMaker.Maker maker;

    private final DB db;

    private static class NamedLink implements Serializable {

        private static final long serialVersionUID = 5645222029377034394L;

        private final NodeId nodeId;

        private final String name;

        public NamedLink(NodeId nodeId, String name) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.name = Objects.requireNonNull(name);
        }

        @Override
        public int hashCode() {
            return nodeId.hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NamedLink) {
                NamedLink childNode = (NamedLink) obj;
                return nodeId.equals(childNode.nodeId) && name.equals(childNode.name);
            }
            return false;
        }
    }

    private static class UnorderedNodeIdPair implements Serializable {

        private static final long serialVersionUID = 5740826508016859275L;

        private final NodeId nodeId1;

        private final NodeId nodeId2;

        public UnorderedNodeIdPair(NodeId nodeId1, NodeId nodeId2) {
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
        }

        @Override
        public int hashCode() {
            return nodeId1.hashCode() + nodeId2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UnorderedNodeIdPair) {
                UnorderedNodeIdPair other = (UnorderedNodeIdPair) obj;
                return (nodeId1.equals(other.nodeId1) && nodeId2.equals(other.nodeId2)) ||
                        (nodeId1.equals(other.nodeId2) && nodeId2.equals(other.nodeId1));
            }
            return false;
        }
    }

    private final ConcurrentMap<String, NodeId> rootNodeMap;

    private final ConcurrentMap<NodeId, List<NodeId>> childNodesMap;

    private final ConcurrentMap<NamedLink, NodeId> childNodeMap;

    private final ConcurrentMap<NodeId, NodeId> parentNodeMap;

    private final ConcurrentMap<NodeId, String> nodeNameMap;

    private final ConcurrentMap<NodeId, String> nodePseudoClassMap;

    private final ConcurrentMap<NamedLink, String> stringAttributeMap;

    private final ConcurrentMap<NodeId, Set<String>> stringAttributesMap;

    private final ConcurrentMap<NodeId, NodeId> projectRootNodeMap;

    private final ConcurrentMap<MapDbDataSource.Key, byte[]> dataSourceAttributeDataMap;

    private final ConcurrentMap<String, byte[]> dataSourceAttributeData2Map;

    private final ConcurrentMap<NodeId, List<NodeId>> dependencyNodesMap;

    private final ConcurrentMap<NamedLink, NodeId> dependencyNodeMap;

    private final ConcurrentMap<UnorderedNodeIdPair, String> dependencyNameMap;

    private final ConcurrentMap<NodeId, List<NodeId>> backwardDependencyNodesMap;

    private final ConcurrentMap<NamedLink, byte[]> cacheMap;

    private MapDbAppFileSystemStorage(String fileSystemName, DBMaker.Maker maker, Supplier<DB> db) {
        this.maker = Objects.requireNonNull(maker);
        this.db = db.get();

        rootNodeMap = this.db
                .hashMap("rootNode", Serializer.STRING, Serializer.JAVA)
                .createOrOpen();

        childNodesMap = this.db
                .hashMap("childNodes", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        childNodeMap = this.db
                .hashMap("childNode", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        parentNodeMap = this.db
                .hashMap("parentNode", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        nodeNameMap = this.db
                .hashMap("nodeName", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        nodePseudoClassMap = this.db
                .hashMap("nodePseudoClass", Serializer.JAVA, Serializer.STRING)
                .createOrOpen();

        stringAttributeMap = this.db
                .hashMap("stringAttribute", Serializer.JAVA, Serializer.STRING)
                .createOrOpen();

        stringAttributesMap = this.db
                .hashMap("stringAttributes", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        projectRootNodeMap = this.db
                .hashMap("projectRootNode", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        dataSourceAttributeDataMap = this.db
                .hashMap("dataSourceAttributeData", Serializer.JAVA, Serializer.BYTE_ARRAY)
                .createOrOpen();

        dataSourceAttributeData2Map = this.db
                .hashMap("dataSourceAttributeData2", Serializer.STRING, Serializer.BYTE_ARRAY)
                .createOrOpen();

        dependencyNodesMap = this.db
                .hashMap("dependencyNodes", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        dependencyNodeMap = this.db
                .hashMap("dependencyNode", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        dependencyNameMap = this.db
                .hashMap("dependencyName", Serializer.JAVA, Serializer.STRING)
                .createOrOpen();

        backwardDependencyNodesMap = this.db
                .hashMap("backwardDependencyNodes", Serializer.JAVA, Serializer.JAVA)
                .createOrOpen();

        cacheMap = this.db
                .hashMap("cache", Serializer.JAVA, Serializer.BYTE_ARRAY)
                .createOrOpen();

        // create root node
        if (rootNodeMap.isEmpty()) {
            NodeId rootNodeId = createNode(null, fileSystemName, PseudoClass.FOLDER_PSEUDO_CLASS);
            rootNodeMap.put("rootNode", rootNodeId);
        }
    }

    private static List<NodeId> remove(List<NodeId> nodeIds, NodeId nodeId) {
        List<NodeId> newNodeIds = new ArrayList<>(nodeIds);
        newNodeIds.remove(nodeId);
        return newNodeIds;
    }

    private static Set<String> remove(Set<String> strings, String string) {
        Set<String> newStrings = new HashSet<>(strings);
        newStrings.remove(string);
        return newStrings;
    }

    private static List<NodeId> add(List<NodeId> nodeIds, NodeId nodeId) {
        return ImmutableList.<NodeId>builder()
                .addAll(nodeIds)
                .add(nodeId)
                .build();
    }

    private static Set<String> add(Set<String> strings, String string) {
        return ImmutableSet.<String>builder()
                .addAll(strings)
                .add(string)
                .build();
    }

    @Override
    public NodeId fromString(String str) {
        return new UuidNodeId(UUID.fromString(str));
    }

    @Override
    public NodeId getRootNode() {
        return rootNodeMap.get("rootNode");
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        String name = nodeNameMap.get(nodeId);
        if (name == null) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return name;
    }

    @Override
    public List<NodeId> getChildNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        List<NodeId> childNodes = childNodesMap.get(nodeId);
        if (childNodes == null) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return childNodes;
    }

    @Override
    public NodeId getChildNode(NodeId parentNodeId, String name) {
        Objects.requireNonNull(parentNodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(parentNodeId)) {
            throw new AfsStorageException("Parent node " + parentNodeId + " not found");
        }
        return childNodeMap.get(new NamedLink(parentNodeId, name));
    }

    @Override
    public NodeId getParentNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw new AfsStorageException("Parent node " + nodeId + " not found");
        }
        return parentNodeMap.get(nodeId);
    }

    @Override
    public boolean isWritable(NodeId nodeId) {
        return true;
    }

    @Override
    public NodeId createNode(NodeId parentNodeId, String name, String nodePseudoClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodePseudoClass);
        if (parentNodeId != null && !nodeNameMap.containsKey(parentNodeId)) {
            throw new AfsStorageException("Parent node " + parentNodeId + " not found");
        }
        NodeId nodeId = UuidNodeId.generate();
        nodeNameMap.put(nodeId, name);
        nodePseudoClassMap.put(nodeId, nodePseudoClass);
        stringAttributesMap.put(nodeId, Collections.emptySet());
        childNodesMap.put(nodeId, Collections.emptyList());
        if (parentNodeId != null) {
            parentNodeMap.put(nodeId, parentNodeId);
            childNodesMap.put(parentNodeId, add(childNodesMap.get(parentNodeId), nodeId));
            childNodeMap.put(new NamedLink(parentNodeId, name), nodeId);
        }
        if (nodePseudoClass.equals(PseudoClass.PROJECT_PSEUDO_CLASS)) {
            // create root project folder
            NodeId projectRootNodeId = createNode(null, "root", PseudoClass.PROJECT_FOLDER_PSEUDO_CLASS);
            projectRootNodeMap.put(nodeId, projectRootNodeId);
        }
        dependencyNodesMap.put(nodeId, Collections.emptyList());
        backwardDependencyNodesMap.put(nodeId, Collections.emptyList());
        return nodeId;
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        String nodePseudoClass = nodePseudoClassMap.get(nodeId);
        if (nodePseudoClass == null) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return nodePseudoClass;
    }

    @Override
    public void deleteNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        if (!childNodesMap.get(nodeId).isEmpty()) {
            throw new AfsStorageException("Cannot delete a node with children, remove children before");
        }
        if (!getBackwardDependencies(nodeId).isEmpty()) {
            throw new AfsStorageException("Cannot delete a node that is a dependency of another node");
        }
        String name = nodeNameMap.remove(nodeId);
        String nodePseudoClass = nodePseudoClassMap.remove(nodeId);
        for (String attributeName : stringAttributesMap.get(nodeId)) {
            stringAttributeMap.remove(new NamedLink(nodeId, attributeName));
        }
        stringAttributesMap.remove(nodeId);
        childNodesMap.remove(nodeId);
        NodeId parentNodeId = parentNodeMap.remove(nodeId);
        if (parentNodeId != null) {
            childNodesMap.put(parentNodeId, remove(childNodesMap.get(parentNodeId), nodeId));
            childNodeMap.remove(new NamedLink(parentNodeId, name));
        }
        if (nodePseudoClass.equals(PseudoClass.PROJECT_PSEUDO_CLASS)) {
            // also remove everything inside the project
            throw new UnsupportedOperationException("TODO"); // TODO
        }
        for (NodeId toNodeId : getDependencies(nodeId)) {
            String dependencyName = dependencyNameMap.remove(new UnorderedNodeIdPair(nodeId, toNodeId));
            dependencyNodeMap.remove(new NamedLink(nodeId, dependencyName));
            backwardDependencyNodesMap.put(toNodeId, remove(backwardDependencyNodesMap.get(toNodeId), nodeId));
        }
        dependencyNodesMap.remove(nodeId);
    }

    @Override
    public String getStringAttribute(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return stringAttributeMap.get(new NamedLink(nodeId, name));
    }

    @Override
    public void setStringAttribute(NodeId nodeId, String name, String value) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        NamedLink namedLink = new NamedLink(nodeId, name);
        if (value == null) {
            stringAttributeMap.remove(namedLink);
            stringAttributesMap.put(nodeId, remove(stringAttributesMap.get(nodeId), name));
        } else {
            stringAttributeMap.put(namedLink, value);
            stringAttributesMap.put(nodeId, add(stringAttributesMap.get(nodeId), name));
        }
    }

    @Override
    public Reader readStringAttribute(NodeId nodeId, String name) {
        String value = getStringAttribute(nodeId, name);
        return value != null ? new StringReader(value) : null;
    }

    @Override
    public Writer writeStringAttribute(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        return new StringWriter() {
            @Override
            public void close() throws IOException {
                super.close();
                setStringAttribute(nodeId, name, toString());
            }
        };
    }

    @Override
    public DataSource getDataSourceAttribute(NodeId nodeId, String name) {
        return new MapDbDataSource(nodeId, name, dataSourceAttributeDataMap, dataSourceAttributeData2Map);
    }

    @Override
    public NodeId getProjectRootNode(NodeId projectNodeId) {
        Objects.requireNonNull(projectNodeId);
        NodeId projectRootNodeId = projectRootNodeMap.get(projectNodeId);
        if (projectRootNodeId == null) {
            throw new AfsStorageException("Node " + projectNodeId + " not found");
        }
        return projectRootNodeId;
    }

    @Override
    public NodeId getDependency(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw new AfsStorageException("Node " + nodeId+ " not found");
        }
        return dependencyNodeMap.get(new NamedLink(nodeId, name));
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        Objects.requireNonNull(toNodeId);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw new AfsStorageException("Node " + nodeId+ " not found");
        }
        if (!nodeNameMap.containsKey(toNodeId)) {
            throw new AfsStorageException("Node " + nodeId+ " not found");
        }
        dependencyNodesMap.put(nodeId, add(dependencyNodesMap.get(nodeId), toNodeId));
        dependencyNodeMap.put(new NamedLink(nodeId, name), toNodeId);
        dependencyNameMap.put(new UnorderedNodeIdPair(nodeId, toNodeId), name);
        backwardDependencyNodesMap.put(toNodeId, ImmutableList.<NodeId>builder()
                .addAll(backwardDependencyNodesMap.get(toNodeId))
                .add(nodeId)
                .build());
    }

    @Override
    public List<NodeId> getDependencies(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        List<NodeId> dependencyNodeIds = dependencyNodesMap.get(nodeId);
        if (dependencyNodeIds == null) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return dependencyNodeIds;
    }

    @Override
    public List<NodeId> getBackwardDependencies(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        List<NodeId> backwardDependencyNodeIds = backwardDependencyNodesMap.get(nodeId);
        if (backwardDependencyNodeIds == null) {
            throw new AfsStorageException("Node " + nodeId + " not found");
        }
        return backwardDependencyNodeIds;
    }

    @Override
    public InputStream readFromCache(NodeId nodeId, String key) {
        byte[] value = cacheMap.get(new NamedLink(nodeId, key));
        return value != null ? new ByteArrayInputStream(value) : null;
    }

    @Override
    public OutputStream writeToCache(NodeId nodeId, String key) {
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                cacheMap.put(new NamedLink(nodeId, key), toByteArray());
            }
        };
    }

    @Override
    public void invalidateCache(NodeId nodeId, String key) {
        cacheMap.remove(new NamedLink(nodeId, key));
    }

    @Override
    public void invalidateCache() {
        cacheMap.clear();
    }

    @Override
    public void commit() {
        db.commit();
    }

    @Override
    public void rollback() {
        db.rollback();
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}
