/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.powsybl.afs.storage.AfsStorageException;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.math.timeseries.*;
import org.apache.commons.lang3.SystemUtils;
import org.mapdb.*;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
        return new MapDbAppStorage(fileSystemName, () -> {
            DBMaker.Maker maker = DBMaker.fileDB(dbFile)
                    .transactionEnable();
            // it is not recommanded to use mmap on Windows (crash)
            // http://www.mapdb.org/blog/mmap_files_alloc_and_jvm_crash/
            if (!SystemUtils.IS_OS_WINDOWS) {
                maker.fileMmapEnableIfSupported()
                        .fileMmapPreclearDisable();
            }
            return maker.make();
        });
    }

    private static final class NamedLink {

        private final UUID nodeUuid;

        private final String name;

        private NamedLink(UUID nodeUuid, String name) {
            this.nodeUuid = Objects.requireNonNull(nodeUuid);
            this.name = Objects.requireNonNull(name);
        }

        public UUID getNodeUuid() {
            return nodeUuid;
        }

        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return nodeUuid.hashCode() + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NamedLink) {
                NamedLink other = (NamedLink) obj;
                return nodeUuid.equals(other.nodeUuid) && name.equals(other.name);
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
            UuidSerializer.INSTANCE.serialize(out, namedLink.getNodeUuid());
            out.writeUTF(namedLink.getName());
        }

        @Override
        public NamedLink deserialize(DataInput2 input, int available) throws IOException {
            UUID nodeUuid = UuidSerializer.INSTANCE.deserialize(input, available);
            String name = input.readUTF();
            return new NamedLink(nodeUuid, name);
        }
    }

    private static final class UnorderedNodeUuidPair {

        private final UUID nodeUuid1;

        private final UUID nodeUuid2;

        private UnorderedNodeUuidPair(UUID nodeUuid1, UUID nodeUuid2) {
            this.nodeUuid1 = Objects.requireNonNull(nodeUuid1);
            this.nodeUuid2 = Objects.requireNonNull(nodeUuid2);
        }

        public UUID getNodeUuid1() {
            return nodeUuid1;
        }

        public UUID getNodeUuid2() {
            return nodeUuid2;
        }

        @Override
        public int hashCode() {
            return nodeUuid1.hashCode() + nodeUuid2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UnorderedNodeUuidPair) {
                UnorderedNodeUuidPair other = (UnorderedNodeUuidPair) obj;
                return (nodeUuid1.equals(other.nodeUuid1) && nodeUuid2.equals(other.nodeUuid2)) ||
                        (nodeUuid1.equals(other.nodeUuid2) && nodeUuid2.equals(other.nodeUuid1));
            }
            return false;
        }
    }

    public static final class UnorderedNodeUuidPairSerializer implements Serializer<UnorderedNodeUuidPair>, Serializable {

        public static final UnorderedNodeUuidPairSerializer INSTANCE = new UnorderedNodeUuidPairSerializer();

        private UnorderedNodeUuidPairSerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, UnorderedNodeUuidPair pair) throws IOException {
            UuidSerializer.INSTANCE.serialize(out, pair.getNodeUuid1());
            UuidSerializer.INSTANCE.serialize(out, pair.getNodeUuid2());
        }

        @Override
        public UnorderedNodeUuidPair deserialize(DataInput2 input, int available) throws IOException {
            UUID nodeUuid1 = UuidSerializer.INSTANCE.deserialize(input, available);
            UUID nodeUuid2 = UuidSerializer.INSTANCE.deserialize(input, available);
            return new UnorderedNodeUuidPair(nodeUuid1, nodeUuid2);
        }
    }

    private static final class TimeSeriesKey {

        private final UUID nodeUuid;

        private final int version;

        private final String timeSeriesName;

        private TimeSeriesKey(UUID nodeUuid, int version, String timeSeriesName) {
            this.nodeUuid = Objects.requireNonNull(nodeUuid);
            this.version = version;
            this.timeSeriesName = Objects.requireNonNull(timeSeriesName);
        }

        public UUID getNodeUuid() {
            return nodeUuid;
        }

        public int getVersion() {
            return version;
        }

        public String getTimeSeriesName() {
            return timeSeriesName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeUuid, version, timeSeriesName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TimeSeriesKey) {
                TimeSeriesKey other = (TimeSeriesKey) obj;
                return nodeUuid.equals(other.nodeUuid) &&
                        version == other.version &&
                        timeSeriesName.equals(other.timeSeriesName);
            }
            return false;
        }

        @Override
        public String toString() {
            return "TimeSeriesKey(nodeUuid=" + nodeUuid + ", version=" + version + ", timeSeriesName=" + timeSeriesName + ")";
        }
    }

    public static final class TimeSeriesKeySerializer implements Serializer<TimeSeriesKey>, Serializable {

        public static final TimeSeriesKeySerializer INSTANCE = new TimeSeriesKeySerializer();

        private TimeSeriesKeySerializer() {
        }

        @Override
        public void serialize(DataOutput2 out, TimeSeriesKey key) throws IOException {
            UuidSerializer.INSTANCE.serialize(out, key.getNodeUuid());
            out.writeInt(key.getVersion());
            out.writeUTF(key.getTimeSeriesName());
        }

        @Override
        public TimeSeriesKey deserialize(DataInput2 input, int available) throws IOException {
            UUID nodeUuid = UuidSerializer.INSTANCE.deserialize(input, available);
            int version = input.readInt();
            String timeSeriesName = input.readUTF();
            return new TimeSeriesKey(nodeUuid, version, timeSeriesName);
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

    private final Atomic.Var<NodeInfo> rootNodeVar;

    private final ConcurrentMap<UUID, UuidList> childNodesMap;

    private final ConcurrentMap<NamedLink, UUID> childNodeMap;

    private final ConcurrentMap<UUID, UUID> parentNodeMap;

    private final ConcurrentMap<UUID, NodeInfo> nodeInfoMap;

    private final ConcurrentMap<NamedLink, byte[]> dataMap;

    private final ConcurrentMap<UUID, Set<String>> dataNamesMap;

    private final ConcurrentMap<UUID, Set<String>> timeSeriesNamesMap;

    private final ConcurrentMap<NamedLink, TimeSeriesMetadata> timeSeriesMetadataMap;

    private final ConcurrentMap<TimeSeriesKey, Integer> timeSeriesLastChunkMap;

    private final ConcurrentMap<TimeSeriesChunkKey, DoubleArrayChunk> doubleTimeSeriesChunksMap;

    private final ConcurrentMap<TimeSeriesChunkKey, StringArrayChunk> stringTimeSeriesChunksMap;

    private final ConcurrentMap<UUID, UuidList> dependencyNodesMap;

    private final ConcurrentMap<NamedLink, UUID> dependencyNodeMap;

    private final ConcurrentMap<UnorderedNodeUuidPair, String> dependencyNameMap;

    private final ConcurrentMap<UUID, UuidList> backwardDependencyNodesMap;

    protected MapDbAppStorage(String fileSystemName, Supplier<DB> db) {
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.db = db.get();

        rootNodeVar = this.db.atomicVar("rootNode", NodeInfoSerializer.INSTANCE)
                .createOrOpen();

        childNodesMap = this.db
                .hashMap("childNodes", UuidSerializer.INSTANCE, UuidListSerializer.INSTANCE)
                .createOrOpen();

        childNodeMap = this.db
                .hashMap("childNode", NamedLinkSerializer.INSTANCE, UuidSerializer.INSTANCE)
                .createOrOpen();

        parentNodeMap = this.db
                .hashMap("parentNode", UuidSerializer.INSTANCE, UuidSerializer.INSTANCE)
                .createOrOpen();

        nodeInfoMap = this.db
                .hashMap("nodeInfo", UuidSerializer.INSTANCE, NodeInfoSerializer.INSTANCE)
                .createOrOpen();

        dataMap = this.db
                .hashMap("data", NamedLinkSerializer.INSTANCE, Serializer.BYTE_ARRAY)
                .createOrOpen();

        dataNamesMap = this.db
                .hashMap("dataNames", UuidSerializer.INSTANCE, Serializer.JAVA)
                .createOrOpen();

        timeSeriesNamesMap = this.db
                .hashMap("timeSeriesNamesMap", UuidSerializer.INSTANCE, Serializer.JAVA)
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
                .hashMap("dependencyNodes", UuidSerializer.INSTANCE, UuidListSerializer.INSTANCE)
                .createOrOpen();

        dependencyNodeMap = this.db
                .hashMap("dependencyNode", NamedLinkSerializer.INSTANCE, UuidSerializer.INSTANCE)
                .createOrOpen();

        dependencyNameMap = this.db
                .hashMap("dependencyName", UnorderedNodeUuidPairSerializer.INSTANCE, Serializer.STRING)
                .createOrOpen();

        backwardDependencyNodesMap = this.db
                .hashMap("backwardDependencyNodes", UuidSerializer.INSTANCE, UuidListSerializer.INSTANCE)
                .createOrOpen();
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

    private AfsStorageException createNodeNotFoundException(UUID nodeUuid) {
        return new AfsStorageException("Node " + nodeUuid + " not found");
    }

    static UUID checkNodeId(String nodeId) {
        try {
            return UUID.fromString(nodeId);
        } catch (IllegalArgumentException e) {
            throw new AfsStorageException("Node id '" + nodeId + "' is expected to be a UUID");
        }
    }

    private void checkNodeExists(UUID nodeUuid) {
        if (!nodeInfoMap.containsKey(nodeUuid)) {
            throw createNodeNotFoundException(nodeUuid);
        }
    }

    private static UUID checkNullableNodeId(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        return checkNodeId(nodeId);
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        NodeInfo rootNodeInfo = rootNodeVar.get();
        if (rootNodeInfo == null) {
            rootNodeInfo = createNode(null, name, nodePseudoClass, "", 0, new NodeGenericMetadata());
            rootNodeVar.set(rootNodeInfo);
        }
        return rootNodeInfo;
    }

    private NodeInfo getNodeInfo(UUID nodeUuid) {
        NodeInfo nodeInfo = nodeInfoMap.get(nodeUuid);
        if (nodeInfo == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return nodeInfo;
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        return getNodeInfo(nodeUuid);
    }

    @Override
    public void setDescription(String nodeId, String description) {
        UUID nodeUuid = checkNodeId(nodeId);
        NodeInfo nodeInfo = nodeInfoMap.get(nodeUuid);
        if (nodeInfo == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        Objects.requireNonNull(description);
        nodeInfo.setDescription(description);
        nodeInfoMap.put(nodeUuid, nodeInfo);
    }

    @Override
    public boolean isWritable(String nodeId) {
        return true;
    }

    @Override
    public List<NodeInfo> getChildNodes(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        UuidList childNodes = childNodesMap.get(nodeUuid);
        if (childNodes == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return childNodes.toList().stream().map(this::getNodeInfo).collect(Collectors.toList());
    }

    @Override
    public NodeInfo getChildNode(String parentString, String name) {
        UUID parentNodeUuid = checkNodeId(parentString);
        Objects.requireNonNull(name);
        checkNodeExists(parentNodeUuid);
        UUID childNodeUuid = childNodeMap.get(new NamedLink(parentNodeUuid, name));
        return childNodeUuid != null ? getNodeInfo(childNodeUuid) : null;
    }

    @Override
    public NodeInfo getParentNode(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        UUID parentNodeUuid = parentNodeMap.get(nodeUuid);
        return parentNodeUuid != null ? getNodeInfo(parentNodeUuid) : null;
    }

    @Override
    public void setParentNode(String nodeId, String newParentNodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        UUID newParentNodeUuid = checkNodeId(newParentNodeId);
        checkNodeExists(nodeUuid);
        checkNodeExists(newParentNodeUuid);
        UUID oldParentNodeUuid = parentNodeMap.get(nodeUuid);
        if (oldParentNodeUuid == null) {
            throw new AfsStorageException("Cannot change parent of root folder");
        }

        parentNodeMap.put(nodeUuid, newParentNodeUuid);

        // remove from old parent
        String name = nodeInfoMap.get(nodeUuid).getName();
        childNodeMap.remove(new NamedLink(oldParentNodeUuid, name));
        childNodesMap.put(oldParentNodeUuid, childNodesMap.get(oldParentNodeUuid).remove(nodeUuid));

        // add to new parent
        childNodesMap.put(newParentNodeUuid, childNodesMap.get(newParentNodeUuid).add(nodeUuid));
        childNodeMap.put(new NamedLink(newParentNodeUuid, name), nodeUuid);
    }

    @Override
    public NodeInfo createNode(String parentNodeId, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata) {
        UUID parentNodeUuid = checkNullableNodeId(parentNodeId);
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodePseudoClass);
        if (parentNodeUuid != null) {
            checkNodeExists(parentNodeUuid);
            // check parent node does not already have a child with the same name
            if (childNodeMap.containsKey(new NamedLink(parentNodeUuid, name))) {
                throw new AfsStorageException("Node " + parentNodeUuid + " already have a child named " + name);
            }
        }
        UUID nodeUuid = UUID.randomUUID();
        long creationTime = ZonedDateTime.now().toInstant().toEpochMilli();
        NodeInfo nodeInfo = new NodeInfo(nodeUuid.toString(), name, nodePseudoClass, description, creationTime, creationTime, version, genericMetadata);
        nodeInfoMap.put(nodeUuid, nodeInfo);
        dataNamesMap.put(nodeUuid, Collections.emptySet());
        childNodesMap.put(nodeUuid, new UuidList());
        if (parentNodeUuid != null) {
            parentNodeMap.put(nodeUuid, parentNodeUuid);
            childNodesMap.compute(parentNodeUuid, (useless, nodeUuidList) -> nodeUuidList.add(nodeUuid));
            childNodeMap.put(new NamedLink(parentNodeUuid, name), nodeUuid);
        }
        dependencyNodesMap.put(nodeUuid, new UuidList());
        backwardDependencyNodesMap.put(nodeUuid, new UuidList());
        return nodeInfo;
    }

    @Override
    public void deleteNode(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        deleteNode(nodeUuid);
    }

    private void deleteNode(UUID nodeUuid) {
        checkNodeExists(nodeUuid);
        // recursively delete children
        for (UUID childNodeUuid : childNodesMap.get(nodeUuid).toList()) {
            deleteNode(childNodeUuid);
        }
        NodeInfo nodeInfo = nodeInfoMap.remove(nodeUuid);
        for (String dataName : dataNamesMap.get(nodeUuid)) {
            NamedLink namedLink = new NamedLink(nodeUuid, dataName);
            dataMap.remove(namedLink);
        }
        dataNamesMap.remove(nodeUuid);
        childNodesMap.remove(nodeUuid);
        UUID parentNodeUuid = parentNodeMap.remove(nodeUuid);
        if (parentNodeUuid != null) {
            childNodesMap.compute(parentNodeUuid, (useless, nodeUuidList) -> nodeUuidList.remove(nodeUuid));
            childNodeMap.remove(new NamedLink(parentNodeUuid, nodeInfo.getName()));
        }
        for (UUID toNodeUuid : dependencyNodesMap.get(nodeUuid).toList()) {
            String dependencyName = dependencyNameMap.remove(new UnorderedNodeUuidPair(nodeUuid, toNodeUuid));
            dependencyNodeMap.remove(new NamedLink(nodeUuid, dependencyName));
            backwardDependencyNodesMap.put(toNodeUuid, backwardDependencyNodesMap.get(toNodeUuid).remove(nodeUuid));
        }
        dependencyNodesMap.remove(nodeUuid);
    }

    @Override
    public InputStream readBinaryData(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        byte[] value = dataMap.get(new NamedLink(nodeUuid, name));
        return value != null ? new ByteArrayInputStream(value) : null;
    }

    @Override
    public OutputStream writeBinaryData(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        return new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                super.close();

                // store the byte array
                NamedLink namedLink = new NamedLink(nodeUuid, name);
                dataMap.put(namedLink, toByteArray());
                dataNamesMap.put(nodeUuid, add(dataNamesMap.get(nodeUuid), name));
            }
        };
    }

    @Override
    public boolean dataExists(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        return dataNamesMap.get(nodeUuid).contains(name);
    }

    @Override
    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(metadata);
        checkNodeExists(nodeUuid);
        Set<String> names = timeSeriesNamesMap.get(nodeUuid);
        if (names == null) {
            names = new HashSet<>();
        }
        if (names.contains(metadata.getName())) {
            throw new AfsStorageException("Time series " + metadata.getName() + " already exists at node " + nodeUuid);
        }
        timeSeriesNamesMap.put(nodeUuid, add(names, metadata.getName()));
        timeSeriesMetadataMap.put(new NamedLink(nodeUuid, metadata.getName()), metadata);
    }

    @Override
    public Set<String> getTimeSeriesNames(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        Set<String> names = timeSeriesNamesMap.get(nodeUuid);
        if (names == null) {
            return Collections.emptySet();
        }
        return names;
    }

    private static AfsStorageException createTimeSeriesNotFoundAtNode(String timeSeriesName, UUID nodeUuid) {
        return new AfsStorageException("Time series " + timeSeriesName + " not found at node " + nodeUuid);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        List<TimeSeriesMetadata> metadataList = new ArrayList<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(nodeUuid, timeSeriesName));
            if (metadata == null) {
                throw createTimeSeriesNotFoundAtNode(timeSeriesName, nodeUuid);
            }
            metadataList.add(metadata);
        }
        return metadataList;
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P>> List<C> getChunks(UUID nodeId, int version, String timeSeriesName,
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
        List<T> getTimeSeries(String nodeId, Set<String> timeSeriesNames, int version,
                              ConcurrentMap<TimeSeriesChunkKey, C> map, BiFunction<TimeSeriesMetadata, List<C>, T> constr) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesIndex.checkVersion(version);
        Objects.requireNonNull(map);
        Objects.requireNonNull(constr);
        List<T> timeSeriesList = new ArrayList<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(nodeUuid, timeSeriesName));
            if (metadata == null) {
                throw createTimeSeriesNotFoundAtNode(timeSeriesName, nodeUuid);
            }
            List<C> chunks = getChunks(nodeUuid, version, timeSeriesName, metadata, map);
            if (!chunks.isEmpty()) {
                timeSeriesList.add(constr.apply(metadata, chunks));
            }
        }
        return timeSeriesList;
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P>> void addTimeSeriesData(String nodeId,
                                                                                      int version,
                                                                                      String timeSeriesName,
                                                                                      List<C> chunks,
                                                                                      ConcurrentMap<TimeSeriesChunkKey, C> map) {
        UUID nodeUuid = checkNodeId(nodeId);
        TimeSeriesIndex.checkVersion(version);
        Objects.requireNonNull(timeSeriesName);
        Objects.requireNonNull(chunks);
        Objects.requireNonNull(map);
        for (C chunk : chunks) {
            TimeSeriesKey key = new TimeSeriesKey(nodeUuid, version, timeSeriesName);
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
    public List<DoubleTimeSeries> getDoubleTimeSeries(String nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeries(nodeId, timeSeriesNames, version, doubleTimeSeriesChunksMap, StoredDoubleTimeSeries::new);
    }

    @Override
    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, doubleTimeSeriesChunksMap);
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(String nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeries(nodeId, timeSeriesNames, version, stringTimeSeriesChunksMap, StringTimeSeries::new);
    }

    @Override
    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, stringTimeSeriesChunksMap);
    }

    @Override
    public void removeAllTimeSeries(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        Set<String> names = timeSeriesNamesMap.get(nodeUuid);
        if (names != null) {
            names.forEach(name -> timeSeriesMetadataMap.remove(new NamedLink(nodeUuid, name)));
            timeSeriesNamesMap.remove(nodeUuid);
        }
    }

    @Override
    public NodeInfo getDependency(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        UUID dependencyNodeUuid = dependencyNodeMap.get(new NamedLink(nodeUuid, name));
        return dependencyNodeUuid != null ? getNodeInfo(dependencyNodeUuid) : null;
    }

    @Override
    public void addDependency(String nodeId, String name, String toNodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        UUID toNodeUuid = checkNodeId(toNodeId);
        checkNodeExists(nodeUuid);
        checkNodeExists(toNodeUuid);
        dependencyNodesMap.put(nodeUuid, dependencyNodesMap.get(nodeUuid).add(toNodeUuid));
        dependencyNodeMap.put(new NamedLink(nodeUuid, name), toNodeUuid);
        dependencyNameMap.put(new UnorderedNodeUuidPair(nodeUuid, toNodeUuid), name);
        backwardDependencyNodesMap.put(toNodeUuid, backwardDependencyNodesMap.get(toNodeUuid).add(nodeUuid));
    }

    @Override
    public List<NodeInfo> getDependencies(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        UuidList dependencyNodeUuids = dependencyNodesMap.get(nodeUuid);
        if (dependencyNodeUuids == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return dependencyNodeUuids.toList().stream().map(this::getNodeInfo).collect(Collectors.toList());
    }

    @Override
    public List<NodeInfo> getBackwardDependencies(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        UuidList backwardDependencyStrings = backwardDependencyNodesMap.get(nodeUuid);
        if (backwardDependencyStrings == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return backwardDependencyStrings.toList().stream().map(this::getNodeInfo).collect(Collectors.toList());
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
