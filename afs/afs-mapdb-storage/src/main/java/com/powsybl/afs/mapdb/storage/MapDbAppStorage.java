/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.powsybl.afs.storage.*;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.math.timeseries.*;
import org.mapdb.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbAppStorage implements AppStorage {

    public static MapDbAppStorage createMem(String fileSystemName) {
        DBMaker.Maker maker = DBMaker.memoryDB();
        return new MapDbAppStorage(fileSystemName, maker::make);
    }

    public static MapDbAppStorage createHeap(String fileSystemName) {
        DBMaker.Maker maker = DBMaker.heapDB();
        return new MapDbAppStorage(fileSystemName, maker::make);
    }

    public static MapDbAppStorage createMmapFile(String fileSystemName, File dbFile) {
        DBMaker.Maker maker = DBMaker.fileDB(dbFile);
        return new MapDbAppStorage(fileSystemName, () -> maker
                .fileMmapEnable()
                .fileMmapEnableIfSupported()
                .fileMmapPreclearDisable()
                .transactionEnable()
                .make());
    }

    private static final class NamedLink {

        private final UuidNodeId nodeId;

        private final String name;

        private NamedLink(UuidNodeId nodeId, String name) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.name = Objects.requireNonNull(name);
        }

        public UuidNodeId getNodeId() {
            return nodeId;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return nodeId.hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NamedLink) {
                NamedLink other = (NamedLink) obj;
                return nodeId.equals(other.nodeId) && name.equals(other.name);
            }
            return false;
        }
    }

    public static final class NamedLinkSerializer implements Serializer<NamedLink>, Serializable {

        public static final NamedLinkSerializer INSTANCE = new NamedLinkSerializer();

        private NamedLinkSerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, NamedLink namedLink) throws IOException {
            UuidNodeIdSerializer.INSTANCE.serialize(out, namedLink.getNodeId());
            out.writeUTF(namedLink.getName());
        }

        @Override
        public NamedLink deserialize(DataInput2 input, int available) throws IOException {
            UuidNodeId nodeId = UuidNodeIdSerializer.INSTANCE.deserialize(input, available);
            String name = input.readUTF();
            return new NamedLink(nodeId, name);
        }
    }

    private static final class UnorderedNodeIdPair {

        private final UuidNodeId nodeId1;

        private final UuidNodeId nodeId2;

        private UnorderedNodeIdPair(UuidNodeId nodeId1, UuidNodeId nodeId2) {
            this.nodeId1 = Objects.requireNonNull(nodeId1);
            this.nodeId2 = Objects.requireNonNull(nodeId2);
        }

        public UuidNodeId getNodeId1() {
            return nodeId1;
        }

        public UuidNodeId getNodeId2() {
            return nodeId2;
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

    public static final class UnorderedNodeIdPairSerializer implements Serializer<UnorderedNodeIdPair>, Serializable {

        public static final UnorderedNodeIdPairSerializer INSTANCE = new UnorderedNodeIdPairSerializer();

        private UnorderedNodeIdPairSerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, UnorderedNodeIdPair pair) throws IOException {
            UuidNodeIdSerializer.INSTANCE.serialize(out, pair.getNodeId1());
            UuidNodeIdSerializer.INSTANCE.serialize(out, pair.getNodeId2());
        }

        @Override
        public UnorderedNodeIdPair deserialize(DataInput2 input, int available) throws IOException {
            UuidNodeId nodeId1 = UuidNodeIdSerializer.INSTANCE.deserialize(input, available);
            UuidNodeId nodeId2 = UuidNodeIdSerializer.INSTANCE.deserialize(input, available);
            return new UnorderedNodeIdPair(nodeId1, nodeId2);
        }
    }

    private static final class TimeSeriesKey {

        private final UuidNodeId nodeId;

        private final int version;

        private final String timeSeriesName;

        private TimeSeriesKey(UuidNodeId nodeId, int version, String timeSeriesName) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.version = version;
            this.timeSeriesName = Objects.requireNonNull(timeSeriesName);
        }

        public UuidNodeId getNodeId() {
            return nodeId;
        }

        public int getVersion() {
            return version;
        }

        public String getTimeSeriesName() {
            return timeSeriesName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, version, timeSeriesName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TimeSeriesKey) {
                TimeSeriesKey other = (TimeSeriesKey) obj;
                return nodeId.equals(other.nodeId) &&
                        version == other.version &&
                        timeSeriesName.equals(other.timeSeriesName);
            }
            return false;
        }

        @Override
        public String toString() {
            return "TimeSeriesKey(nodeId=" + nodeId + ", version=" + version + ", timeSeriesName=" + timeSeriesName + ")";
        }
    }

    public static final class TimeSeriesKeySerializer implements Serializer<TimeSeriesKey>, Serializable {

        public static final TimeSeriesKeySerializer INSTANCE = new TimeSeriesKeySerializer();

        private TimeSeriesKeySerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, TimeSeriesKey key) throws IOException {
            UuidNodeIdSerializer.INSTANCE.serialize(out, key.getNodeId());
            out.writeInt(key.getVersion());
            out.writeUTF(key.getTimeSeriesName());
        }

        @Override
        public TimeSeriesKey deserialize(DataInput2 input, int available) throws IOException {
            UuidNodeId nodeId1 = UuidNodeIdSerializer.INSTANCE.deserialize(input, available);
            int version = input.readInt();
            String timeSeriesName = input.readUTF();
            return new TimeSeriesKey(nodeId1, version, timeSeriesName);
        }
    }

    private static final class TimeSeriesChunkKey {

        private final TimeSeriesKey timeSeriesKey;

        private final int chunk;

        private TimeSeriesChunkKey(TimeSeriesKey timeSeriesKey, int chunk) {
            this.timeSeriesKey = Objects.requireNonNull(timeSeriesKey);
            this.chunk = chunk;
        }

        public TimeSeriesKey getTimeSeriesKey() {
            return timeSeriesKey;
        }

        public int getChunk() {
            return chunk;
        }

        @Override
        public int hashCode() {
            return Objects.hash(timeSeriesKey, chunk);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TimeSeriesChunkKey) {
                TimeSeriesChunkKey other = (TimeSeriesChunkKey) obj;
                return timeSeriesKey.equals(other.timeSeriesKey) &&
                        chunk == other.chunk;
            }
            return false;
        }

        @Override
        public String toString() {
            return "TimeSeriesChunkKey(key=" + timeSeriesKey + ", chunk=" + chunk + ")";
        }
    }

    public static final class TimeSeriesChunkKeySerializer implements Serializer<TimeSeriesChunkKey>, Serializable {

        public static final TimeSeriesChunkKeySerializer INSTANCE = new TimeSeriesChunkKeySerializer();

        private TimeSeriesChunkKeySerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, TimeSeriesChunkKey chunkKey) throws IOException {
            TimeSeriesKeySerializer.INSTANCE.serialize(out, chunkKey.getTimeSeriesKey());
            out.writeInt(chunkKey.getChunk());
        }

        @Override
        public TimeSeriesChunkKey deserialize(DataInput2 input, int available) throws IOException {
            TimeSeriesKey timeSeriesKey = TimeSeriesKeySerializer.INSTANCE.deserialize(input, available);
            int chunk = input.readInt();
            return new TimeSeriesChunkKey(timeSeriesKey, chunk);
        }
    }

    private final String fileSystemName;

    private final DB db;

    private final Atomic.Var<UuidNodeId> rootNodeVar;

    private final ConcurrentMap<UuidNodeId, UuidNodeIdList> childNodesMap;

    private final ConcurrentMap<NamedLink, UuidNodeId> childNodeMap;

    private final ConcurrentMap<UuidNodeId, UuidNodeId> parentNodeMap;

    private final ConcurrentMap<UuidNodeId, String> nodeNameMap;

    private final ConcurrentMap<UuidNodeId, String> nodePseudoClassMap;

    private final ConcurrentMap<NamedLink, String> stringAttributeMap;

    private final ConcurrentMap<NamedLink, Integer> integerAttributeMap;

    private final ConcurrentMap<NamedLink, Float> floatAttributeMap;

    private final ConcurrentMap<NamedLink, Double> doubleAttributeMap;

    private final ConcurrentMap<NamedLink, Boolean> booleanAttributeMap;

    private final ConcurrentMap<UuidNodeId, Set<String>> attributesMap;

    private final ConcurrentMap<MapDbDataSource.Key, byte[]> dataSourceAttributeDataMap;

    private final ConcurrentMap<String, byte[]> dataSourceAttributeData2Map;

    private final ConcurrentMap<UuidNodeId, Set<String>> timeSeriesNamesMap;

    private final ConcurrentMap<NamedLink, TimeSeriesMetadata> timeSeriesMetadataMap;

    private final ConcurrentMap<TimeSeriesKey, Integer> timeSeriesLastChunkMap;

    private final ConcurrentMap<TimeSeriesChunkKey, DoubleArrayChunk> doubleTimeSeriesChunksMap;

    private final ConcurrentMap<TimeSeriesChunkKey, StringArrayChunk> stringTimeSeriesChunksMap;

    private final ConcurrentMap<UuidNodeId, UuidNodeIdList> dependencyNodesMap;

    private final ConcurrentMap<NamedLink, UuidNodeId> dependencyNodeMap;

    private final ConcurrentMap<UnorderedNodeIdPair, String> dependencyNameMap;

    private final ConcurrentMap<UuidNodeId, UuidNodeIdList> backwardDependencyNodesMap;

    private final ConcurrentMap<NamedLink, byte[]> cacheMap;

    protected MapDbAppStorage(String fileSystemName, Supplier<DB> db) {
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.db = db.get();

        rootNodeVar = this.db.atomicVar("rootNode", UuidNodeIdSerializer.INSTANCE)
                .createOrOpen();

        childNodesMap = this.db
                .hashMap("childNodes", UuidNodeIdSerializer.INSTANCE, UuidNodeIdListSerializer.INSTANCE)
                .createOrOpen();

        childNodeMap = this.db
                .hashMap("childNode", NamedLinkSerializer.INSTANCE, UuidNodeIdSerializer.INSTANCE)
                .createOrOpen();

        parentNodeMap = this.db
                .hashMap("parentNode", UuidNodeIdSerializer.INSTANCE, UuidNodeIdSerializer.INSTANCE)
                .createOrOpen();

        nodeNameMap = this.db
                .hashMap("nodeName", UuidNodeIdSerializer.INSTANCE, Serializer.STRING)
                .createOrOpen();

        nodePseudoClassMap = this.db
                .hashMap("nodePseudoClass", UuidNodeIdSerializer.INSTANCE, Serializer.STRING)
                .createOrOpen();

        stringAttributeMap = this.db
                .hashMap("stringAttribute", NamedLinkSerializer.INSTANCE, Serializer.STRING)
                .createOrOpen();

        integerAttributeMap = this.db
                .hashMap("integerAttribute", NamedLinkSerializer.INSTANCE, Serializer.INTEGER)
                .createOrOpen();

        floatAttributeMap = this.db
                .hashMap("floatAttribute", NamedLinkSerializer.INSTANCE, Serializer.FLOAT)
                .createOrOpen();

        doubleAttributeMap = this.db
                .hashMap("doubleAttribute", NamedLinkSerializer.INSTANCE, Serializer.DOUBLE)
                .createOrOpen();

        booleanAttributeMap = this.db
                .hashMap("booleanAttribute", NamedLinkSerializer.INSTANCE, Serializer.BOOLEAN)
                .createOrOpen();

        attributesMap = this.db
                .hashMap("attributes", UuidNodeIdSerializer.INSTANCE, Serializer.JAVA)
                .createOrOpen();

        dataSourceAttributeDataMap = this.db
                .hashMap("dataSourceAttributeData", MapDbDataSource.KeySerializer.INSTANCE, Serializer.BYTE_ARRAY)
                .createOrOpen();

        dataSourceAttributeData2Map = this.db
                .hashMap("dataSourceAttributeData2", Serializer.STRING, Serializer.BYTE_ARRAY)
                .createOrOpen();

        timeSeriesNamesMap = this.db
                .hashMap("timeSeriesNamesMap", UuidNodeIdSerializer.INSTANCE, Serializer.JAVA)
                .createOrOpen();

        timeSeriesMetadataMap = this.db
                .hashMap("timeSeriesMetadataMap", NamedLinkSerializer.INSTANCE, TimeSeriesMetadataSerializer.INSTANCE)
                .createOrOpen();

        timeSeriesLastChunkMap = this.db
                .hashMap("timeSeriesLastChunkMap", TimeSeriesKeySerializer.INSTANCE, Serializer.INTEGER)
                .createOrOpen();

        doubleTimeSeriesChunksMap = this.db
                .hashMap("doubleTimeSeriesChunksMap", TimeSeriesChunkKeySerializer.INSTANCE, DoubleArrayChunkSerializer.INSTANCE)
                .createOrOpen();

        stringTimeSeriesChunksMap = this.db
                .hashMap("stringTimeSeriesChunksMap", TimeSeriesChunkKeySerializer.INSTANCE, StringArrayChunkSerializer.INSTANCE)
                .createOrOpen();

        dependencyNodesMap = this.db
                .hashMap("dependencyNodes", UuidNodeIdSerializer.INSTANCE, UuidNodeIdListSerializer.INSTANCE)
                .createOrOpen();

        dependencyNodeMap = this.db
                .hashMap("dependencyNode", NamedLinkSerializer.INSTANCE, UuidNodeIdSerializer.INSTANCE)
                .createOrOpen();

        dependencyNameMap = this.db
                .hashMap("dependencyName", UnorderedNodeIdPairSerializer.INSTANCE, Serializer.STRING)
                .createOrOpen();

        backwardDependencyNodesMap = this.db
                .hashMap("backwardDependencyNodes", UuidNodeIdSerializer.INSTANCE, UuidNodeIdListSerializer.INSTANCE)
                .createOrOpen();

        cacheMap = this.db
                .hashMap("cache", NamedLinkSerializer.INSTANCE, Serializer.BYTE_ARRAY)
                .createOrOpen();
    }

    private static Set<String> remove(Set<String> strings, String string) {
        Set<String> newStrings = new HashSet<>(strings);
        newStrings.remove(string);
        return newStrings;
    }

    private static Set<String> add(Set<String> strings, String string) {
        return ImmutableSet.<String>builder()
                .addAll(strings)
                .add(string)
                .build();
    }

    @Override
    public String getFileSystemName() {
        return fileSystemName;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public NodeId fromString(String str) {
        return new UuidNodeId(UUID.fromString(str));
    }

    private AfsStorageException createNodeNotFoundException(NodeId nodeId) {
        return new AfsStorageException("Node " + nodeId + " not found");
    }

    private static UuidNodeId checkNodeId(NodeId nodeId) {
        if (!(nodeId instanceof UuidNodeId)) {
            throw new AfsStorageException("node id is expected to be a UUID");
        }
        return (UuidNodeId) nodeId;
    }

    private static UuidNodeId checkNullableNodeId(NodeId nodeId) {
        if (nodeId == null) {
            return null;
        }
        return checkNodeId(nodeId);
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        UuidNodeId rootNodeId = rootNodeVar.get();
        if (rootNodeId == null) {
            rootNodeId = createNode(null, name, nodePseudoClass);
            rootNodeVar.set(rootNodeId);
        }
        return new NodeInfo(rootNodeId, name, nodePseudoClass);
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        checkNodeId(nodeId);
        String name = nodeNameMap.get(nodeId);
        if (name == null) {
            throw createNodeNotFoundException(nodeId);
        }
        return name;
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        String nodePseudoClass = nodePseudoClassMap.get(nodeId);
        if (nodePseudoClass == null) {
            throw createNodeNotFoundException(nodeId);
        }
        return nodePseudoClass;
    }

    @Override
    public boolean isWritable(NodeId nodeId) {
        return true;
    }

    @Override
    public List<NodeId> getChildNodes(NodeId nodeId) {
        checkNodeId(nodeId);
        UuidNodeIdList childNodes = childNodesMap.get(nodeId);
        if (childNodes == null) {
            throw createNodeNotFoundException(nodeId);
        }
        return childNodes.getNodeIds();
    }

    @Override
    public NodeId getChildNode(NodeId parentNodeId, String name) {
        UuidNodeId parentUuidNodeId = checkNodeId(parentNodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(parentUuidNodeId)) {
            throw createNodeNotFoundException(parentUuidNodeId);
        }
        return childNodeMap.get(new NamedLink(parentUuidNodeId, name));
    }

    @Override
    public NodeId getParentNode(NodeId nodeId) {
        checkNodeId(nodeId);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw createNodeNotFoundException(nodeId);
        }
        return parentNodeMap.get(nodeId);
    }

    @Override
    public void setParentNode(NodeId nodeId, NodeId newParentNodeId) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        UuidNodeId newParentUuidNodeId = checkNodeId(newParentNodeId);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        if (!nodeNameMap.containsKey(newParentUuidNodeId)) {
            throw createNodeNotFoundException(newParentUuidNodeId);
        }
        UuidNodeId oldParentNodeId = parentNodeMap.get(uuidNodeId);
        if (oldParentNodeId == null) {
            throw new AfsStorageException("Cannot change parent of root folder");
        }

        parentNodeMap.put(uuidNodeId, newParentUuidNodeId);

        // remove from old parent
        String name = nodeNameMap.get(uuidNodeId);
        childNodeMap.remove(new NamedLink(oldParentNodeId, name));
        childNodesMap.put(oldParentNodeId, childNodesMap.get(oldParentNodeId).remove(uuidNodeId));

        // add to new parent
        childNodesMap.put(newParentUuidNodeId, childNodesMap.get(newParentUuidNodeId).add(uuidNodeId));
        childNodeMap.put(new NamedLink(newParentUuidNodeId, name), uuidNodeId);
    }

    @Override
    public UuidNodeId createNode(NodeId parentNodeId, String name, String nodePseudoClass) {
        UuidNodeId parentUuidNodeId = checkNullableNodeId(parentNodeId);
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodePseudoClass);
        if (parentUuidNodeId != null) {
            if (!nodeNameMap.containsKey(parentUuidNodeId)) {
                throw createNodeNotFoundException(parentUuidNodeId);
            }
            // check parent node does not already have a child with the same name
            if (childNodeMap.containsKey(new NamedLink(parentUuidNodeId, name))) {
                throw new AfsStorageException("Node " + parentUuidNodeId + " already have a child named " + name);
            }
        }
        UuidNodeId nodeId = UuidNodeId.generate();
        nodeNameMap.put(nodeId, name);
        nodePseudoClassMap.put(nodeId, nodePseudoClass);
        attributesMap.put(nodeId, Collections.emptySet());
        childNodesMap.put(nodeId, new UuidNodeIdList());
        if (parentUuidNodeId != null) {
            parentNodeMap.put(nodeId, parentUuidNodeId);
            childNodesMap.compute(parentUuidNodeId, (useless, nodeIdList) -> nodeIdList.add(nodeId));
            childNodeMap.put(new NamedLink(parentUuidNodeId, name), nodeId);
        }
        dependencyNodesMap.put(nodeId, new UuidNodeIdList());
        backwardDependencyNodesMap.put(nodeId, new UuidNodeIdList());
        return nodeId;
    }

    @Override
    public void deleteNode(NodeId nodeId) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        // recursively delete children
        for (NodeId childNodeId : getChildNodes(uuidNodeId)) {
            deleteNode(childNodeId);
        }
        String name = nodeNameMap.remove(uuidNodeId);
        nodePseudoClassMap.remove(uuidNodeId);
        for (String attributeName : attributesMap.get(uuidNodeId)) {
            NamedLink namedLink = new NamedLink(uuidNodeId, attributeName);
            stringAttributeMap.remove(namedLink);
            integerAttributeMap.remove(namedLink);
            floatAttributeMap.remove(namedLink);
            doubleAttributeMap.remove(namedLink);
            booleanAttributeMap.remove(namedLink);
        }
        attributesMap.remove(uuidNodeId);
        childNodesMap.remove(uuidNodeId);
        UuidNodeId parentNodeId = parentNodeMap.remove(uuidNodeId);
        if (parentNodeId != null) {
            childNodesMap.compute(parentNodeId, (useless, nodeIdList) -> nodeIdList.remove(uuidNodeId));
            childNodeMap.remove(new NamedLink(parentNodeId, name));
        }
        for (NodeId toNodeId : getDependencies(uuidNodeId)) {
            UuidNodeId toUuidNodeId = checkNodeId(toNodeId);
            String dependencyName = dependencyNameMap.remove(new UnorderedNodeIdPair(uuidNodeId, toUuidNodeId));
            dependencyNodeMap.remove(new NamedLink(uuidNodeId, dependencyName));
            backwardDependencyNodesMap.put(toUuidNodeId, backwardDependencyNodesMap.get(toUuidNodeId).remove(uuidNodeId));
        }
        dependencyNodesMap.remove(uuidNodeId);
    }

    private <T> T getAttribute(ConcurrentMap<NamedLink, T> map, NodeId nodeId, String name) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        return map.get(new NamedLink(uuidNodeId, name));
    }

    private <T> void setAttribute(ConcurrentMap<NamedLink, T> map, NodeId nodeId, String name, T value) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        NamedLink namedLink = new NamedLink(uuidNodeId, name);
        if (value == null) {
            map.remove(namedLink);
            attributesMap.put(uuidNodeId, remove(attributesMap.get(uuidNodeId), name));
        } else {
            map.put(namedLink, value);
            attributesMap.put(uuidNodeId, add(attributesMap.get(uuidNodeId), name));
        }
    }

    @Override
    public String getStringAttribute(NodeId nodeId, String name) {
        return getAttribute(stringAttributeMap, nodeId, name);
    }

    @Override
    public void setStringAttribute(NodeId nodeId, String name, String value) {
        setAttribute(stringAttributeMap, nodeId, name, value);
    }

    @Override
    public OptionalInt getIntAttribute(NodeId nodeId, String name) {
        Integer i = getAttribute(integerAttributeMap, nodeId, name);
        return i == null ? OptionalInt.empty() : OptionalInt.of(i);
    }

    @Override
    public void setIntAttribute(NodeId nodeId, String name, int value) {
        setAttribute(integerAttributeMap, nodeId, name, value);
    }

    @Override
    public OptionalDouble getDoubleAttribute(NodeId nodeId, String name) {
        Double d = getAttribute(doubleAttributeMap, nodeId, name);
        return d == null ? OptionalDouble.empty() : OptionalDouble.of(d);
    }

    @Override
    public void setDoubleAttribute(NodeId nodeId, String name, double value) {
        setAttribute(doubleAttributeMap, nodeId, name, value);
    }

    @Override
    public Optional<Boolean> getBooleanAttribute(NodeId nodeId, String name) {
        Boolean b = getAttribute(booleanAttributeMap, nodeId, name);
        return b == null ? Optional.empty() : Optional.of(b);
    }

    @Override
    public void setBooleanAttribute(NodeId nodeId, String name, boolean value) {
        setAttribute(booleanAttributeMap, nodeId, name, value);
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
        return new MapDbDataSource(checkNodeId(nodeId), name, dataSourceAttributeDataMap, dataSourceAttributeData2Map);
    }

    @Override
    public void createTimeSeries(NodeId nodeId, TimeSeriesMetadata metadata) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(metadata);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        Set<String> names = timeSeriesNamesMap.get(uuidNodeId);
        if (names == null) {
            names = new HashSet<>();
        }
        if (names.contains(metadata.getName())) {
            throw new AfsStorageException("Time series " + metadata.getName() + " already exists at node " + uuidNodeId);
        }
        timeSeriesNamesMap.put(uuidNodeId, add(names, metadata.getName()));
        timeSeriesMetadataMap.put(new NamedLink(uuidNodeId, metadata.getName()), metadata);
    }

    @Override
    public Set<String> getTimeSeriesNames(NodeId nodeId) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        Set<String> names = timeSeriesNamesMap.get(uuidNodeId);
        if (names == null) {
            return Collections.emptySet();
        }
        return names;
    }

    private static AfsStorageException createTimeSeriesNotFoundAtNode(String timeSeriesName, NodeId nodeId) {
        return new AfsStorageException("Time series " + timeSeriesName + " not found at node " + nodeId);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(NodeId nodeId, Set<String> timeSeriesNames) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        List<TimeSeriesMetadata> metadataList = new ArrayList<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(uuidNodeId, timeSeriesName));
            if (metadata == null) {
                throw createTimeSeriesNotFoundAtNode(timeSeriesName, uuidNodeId);
            }
            metadataList.add(metadata);
        }
        return metadataList;
    }

    private static void checkVersion(int version) {
        if (version < 0) {
            throw new IllegalArgumentException("Bad version " + version);
        }
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P>> List<C> getChunks(UuidNodeId nodeId, int version, String timeSeriesName,
                                                                                 TimeSeriesMetadata metadata,
                                                                                 ConcurrentMap<TimeSeriesChunkKey, C> map) {
        TimeSeriesKey key = new TimeSeriesKey(nodeId, version, timeSeriesName);
        Integer lastChunkNum = timeSeriesLastChunkMap.get(key);
        if (lastChunkNum == null) {
            return Collections.emptyList();
        }
        List<C> chunks = new ArrayList<>(lastChunkNum + 1);
        for (int chunkNum = 0; chunkNum <= lastChunkNum; chunkNum++) {
            C chunk = map.get(new TimeSeriesChunkKey(key, chunkNum));
            if (chunk == null) {
                throw new AssertionError("chunk is null");
            }
            if (chunk.getDataType() != metadata.getDataType()) {
                throw new IllegalStateException("Bad chunk data type");
            }
            chunks.add(chunk);
        }
        return chunks;
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P>, T extends TimeSeries<P>>
        List<T> getTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version,
                              ConcurrentMap<TimeSeriesChunkKey, C> map, BiFunction<TimeSeriesMetadata, List<C>, T> constr) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        checkVersion(version);
        Objects.requireNonNull(map);
        Objects.requireNonNull(constr);
        List<T> timeSeriesList = new ArrayList<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(uuidNodeId, timeSeriesName));
            if (metadata == null) {
                throw createTimeSeriesNotFoundAtNode(timeSeriesName, nodeId);
            }
            List<C> chunks = getChunks(uuidNodeId, version, timeSeriesName, metadata, map);
            if (!chunks.isEmpty()) {
                timeSeriesList.add(constr.apply(metadata, chunks));
            }
        }
        return timeSeriesList;
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P>> void addTimeSeriesData(NodeId nodeId,
                                                                                      int version,
                                                                                      String timeSeriesName,
                                                                                      List<C> chunks,
                                                                                      ConcurrentMap<TimeSeriesChunkKey, C> map) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        checkVersion(version);
        Objects.requireNonNull(timeSeriesName);
        Objects.requireNonNull(chunks);
        Objects.requireNonNull(map);
        for (C chunk : chunks) {
            TimeSeriesKey key = new TimeSeriesKey(uuidNodeId, version, timeSeriesName);
            Integer lastNum = timeSeriesLastChunkMap.get(key);
            int num;
            if (lastNum == null) {
                num = 0;
            } else {
                num = lastNum + 1;
            }
            timeSeriesLastChunkMap.put(key, num);
            map.put(new TimeSeriesChunkKey(key, num), chunk);
        }
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeries(nodeId, timeSeriesNames, version, doubleTimeSeriesChunksMap, StoredDoubleTimeSeries::new);
    }

    @Override
    public void addDoubleTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, doubleTimeSeriesChunksMap);
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeries(nodeId, timeSeriesNames, version, stringTimeSeriesChunksMap, StringTimeSeries::new);
    }

    @Override
    public void addStringTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, stringTimeSeriesChunksMap);
    }

    @Override
    public void removeAllTimeSeries(NodeId nodeId) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Set<String> names = timeSeriesNamesMap.get(uuidNodeId);
        if (names != null) {
            names.forEach(name -> timeSeriesMetadataMap.remove(new NamedLink(uuidNodeId, name)));
            timeSeriesNamesMap.remove(uuidNodeId);
        }
    }

    @Override
    public NodeId getDependency(NodeId nodeId, String name) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        if (!nodeNameMap.containsKey(uuidNodeId)) {
            throw createNodeNotFoundException(nodeId);
        }
        return dependencyNodeMap.get(new NamedLink(uuidNodeId, name));
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        UuidNodeId uuidToNodeId = checkNodeId(toNodeId);
        if (!nodeNameMap.containsKey(nodeId)) {
            throw createNodeNotFoundException(uuidNodeId);
        }
        if (!nodeNameMap.containsKey(toNodeId)) {
            throw createNodeNotFoundException(uuidToNodeId);
        }
        dependencyNodesMap.put(uuidNodeId, dependencyNodesMap.get(uuidNodeId).add(uuidToNodeId));
        dependencyNodeMap.put(new NamedLink(uuidNodeId, name), uuidToNodeId);
        dependencyNameMap.put(new UnorderedNodeIdPair(uuidNodeId, uuidToNodeId), name);
        backwardDependencyNodesMap.put(uuidToNodeId, backwardDependencyNodesMap.get(uuidToNodeId).add(uuidNodeId));
    }

    @Override
    public List<NodeId> getDependencies(NodeId nodeId) {
        checkNodeId(nodeId);
        UuidNodeIdList dependencyNodeIds = dependencyNodesMap.get(nodeId);
        if (dependencyNodeIds == null) {
            throw createNodeNotFoundException(nodeId);
        }
        return dependencyNodeIds.getNodeIds();
    }

    @Override
    public List<NodeId> getBackwardDependencies(NodeId nodeId) {
        checkNodeId(nodeId);
        UuidNodeIdList backwardDependencyNodeIds = backwardDependencyNodesMap.get(nodeId);
        if (backwardDependencyNodeIds == null) {
            throw createNodeNotFoundException(nodeId);
        }
        return backwardDependencyNodeIds.getNodeIds();
    }

    @Override
    public InputStream readFromCache(NodeId nodeId, String key) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        byte[] value = cacheMap.get(new NamedLink(uuidNodeId, key));
        return value != null ? new ByteArrayInputStream(value) : null;
    }

    @Override
    public OutputStream writeToCache(NodeId nodeId, String key) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();
                cacheMap.put(new NamedLink(uuidNodeId, key), toByteArray());
            }
        };
    }

    @Override
    public void invalidateCache(NodeId nodeId, String key) {
        UuidNodeId uuidNodeId = checkNodeId(nodeId);
        cacheMap.remove(new NamedLink(uuidNodeId, key));
    }

    @Override
    public void invalidateCache() {
        cacheMap.clear();
    }

    @Override
    public void flush() {
        db.commit();
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}
