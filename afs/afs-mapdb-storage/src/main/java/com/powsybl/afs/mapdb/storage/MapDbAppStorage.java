/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.powsybl.afs.storage.*;
import com.powsybl.timeseries.*;
import org.apache.commons.lang3.SystemUtils;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final String fileSystemName;

    private final DB db;

    private final Atomic.Var<NodeInfo> rootNodeVar;

    private final ConcurrentMap<UUID, List<UUID>> childNodesMap;

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

    private final ConcurrentMap<UUID, List<NamedLink>> dependencyNodesMap;

    private final ConcurrentMap<NamedLink, List<UUID>> dependencyNodesByNameMap;

    private final ConcurrentMap<UUID, List<UUID>> backwardDependencyNodesMap;

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
                .hashMap("dataNames", UuidSerializer.INSTANCE, StringSetSerializer.INSTANCE)
                .createOrOpen();

        timeSeriesNamesMap = this.db
                .hashMap("timeSeriesNamesMap", UuidSerializer.INSTANCE, StringSetSerializer.INSTANCE)
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
                .hashMap("dependencyNodes", UuidSerializer.INSTANCE, NamedLinkListSerializer.INSTANCE)
                .createOrOpen();

        dependencyNodesByNameMap = this.db
                .hashMap("dependencyNodesByName", NamedLinkSerializer.INSTANCE, UuidListSerializer.INSTANCE)
                .createOrOpen();

        backwardDependencyNodesMap = this.db
                .hashMap("backwardDependencyNodes", UuidSerializer.INSTANCE, UuidListSerializer.INSTANCE)
                .createOrOpen();
    }

    private static <K, V> Map<K, Set<V>> addToSet(Map<K, Set<V>> map, K key, V value) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Set<V> values = map.get(key);
        Set<V> values2;
        if (values == null) {
            values2 = ImmutableSet.of(value);
        } else {
            values2 = ImmutableSet.<V>builder()
                    .addAll(values)
                    .add(value)
                    .build();
        }
        map.put(key, values2);
        return map;
    }

    private static <K> IllegalArgumentException createKeyNotFoundException(K key) {
        return new IllegalArgumentException("Key " + key + " not found");
    }

    private static <K, V> Map<K, List<V>> addToList(Map<K, List<V>> map, K key, V value) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        List<V> values = map.get(key);
        List<V> values2;
        if (values == null) {
            values2 = ImmutableList.of(value);
        } else {
            values2 = ImmutableList.<V>builder()
                    .addAll(values)
                    .add(value)
                    .build();
        }
        map.put(key, values2);
        return map;
    }

    private static <K, V> boolean removeFromList(Map<K, List<V>> map, K key, V value) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        List<V> values = map.get(key);
        if (values == null) {
            throw createKeyNotFoundException(key);
        }
        List<V> values2 = new ArrayList<>(values);
        boolean removed = values2.remove(value);
        map.put(key, values2);
        return removed;
    }

    private static <K, V> boolean removeFromSet(Map<K, Set<V>> map, K key, V value) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        Set<V> values = map.get(key);
        if (values == null) {
            throw createKeyNotFoundException(key);
        }
        Set<V> values2 = new HashSet<>(values);
        boolean removed = values2.remove(value);
        map.put(key, values2);
        return removed;
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
        Objects.requireNonNull(description);
        NodeInfo nodeInfo = getNodeInfo(nodeId);
        nodeInfo.setDescription(description);
        nodeInfoMap.put(nodeUuid, nodeInfo);
    }

    @Override
    public void updateModificationTime(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        NodeInfo nodeInfo = getNodeInfo(nodeId);
        long modificationTime = ZonedDateTime.now().toInstant().toEpochMilli();
        nodeInfo.setModificationTime(modificationTime);
        nodeInfoMap.put(nodeUuid, nodeInfo);
    }

    @Override
    public boolean isWritable(String nodeId) {
        return true;
    }

    @Override
    public List<NodeInfo> getChildNodes(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        List<UUID> childNodes = childNodesMap.get(nodeUuid);
        if (childNodes == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return childNodes.stream().map(this::getNodeInfo).collect(Collectors.toList());
    }

    @Override
    public Optional<NodeInfo> getChildNode(String parentString, String name) {
        UUID parentNodeUuid = checkNodeId(parentString);
        Objects.requireNonNull(name);
        checkNodeExists(parentNodeUuid);
        UUID childNodeUuid = childNodeMap.get(new NamedLink(parentNodeUuid, name));
        return Optional.ofNullable(childNodeUuid).map(this::getNodeInfo);
    }

    @Override
    public Optional<NodeInfo> getParentNode(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        UUID parentNodeUuid = parentNodeMap.get(nodeUuid);
        return Optional.ofNullable(parentNodeUuid).map(this::getNodeInfo);
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
        removeFromList(childNodesMap, oldParentNodeUuid, nodeUuid);

        // add to new parent
        addToList(childNodesMap, newParentNodeUuid, nodeUuid);
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
        childNodesMap.put(nodeUuid, new ArrayList<>());
        if (parentNodeUuid != null) {
            parentNodeMap.put(nodeUuid, parentNodeUuid);
            addToList(childNodesMap, parentNodeUuid, nodeUuid);
            childNodeMap.put(new NamedLink(parentNodeUuid, name), nodeUuid);
        }
        dependencyNodesMap.put(nodeUuid, new ArrayList<>());
        backwardDependencyNodesMap.put(nodeUuid, new ArrayList<>());
        return nodeInfo;
    }

    @Override
    public void renameNode(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        NodeInfo nodeInfo = getNodeInfo(nodeId);
        getParentNode(nodeId).ifPresent(parentNode -> {
            UUID parentNodeUuid = checkNodeId(parentNode.getId());
            childNodeMap.remove(new NamedLink(parentNodeUuid, nodeInfo.getName()));
            childNodeMap.put(new NamedLink(parentNodeUuid, name), nodeUuid);
        });
        nodeInfo.setName(name);
        nodeInfoMap.put(nodeUuid, nodeInfo);
    }

    @Override
    public String deleteNode(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        UUID parentNodeUuid = deleteNode(nodeUuid);
        return parentNodeUuid.toString();
    }

    private UUID deleteNode(UUID nodeUuid) {
        checkNodeExists(nodeUuid);

        // deleting root node is not allowed
        if (nodeUuid.toString().equals(rootNodeVar.get().getId())) {
            throw new AfsStorageException("Cannot delete root node");
        }

        // recursively delete children
        for (UUID childNodeUuid : childNodesMap.get(nodeUuid)) {
            deleteNode(childNodeUuid);
        }
        NodeInfo nodeInfo = nodeInfoMap.remove(nodeUuid);
        for (String dataName : dataNamesMap.get(nodeUuid)) {
            dataMap.remove(new NamedLink(nodeUuid, dataName));
        }
        dataNamesMap.remove(nodeUuid);
        childNodesMap.remove(nodeUuid);
        UUID parentNodeUuid = parentNodeMap.remove(nodeUuid);
        removeFromList(childNodesMap, parentNodeUuid, nodeUuid);
        childNodeMap.remove(new NamedLink(parentNodeUuid, nodeInfo.getName()));

        // update dependencies of backward dependencies
        for (UUID otherNodeUuid : backwardDependencyNodesMap.get(nodeUuid)) {
            List<NamedLink> linksToRemove = new ArrayList<>();
            for (NamedLink link : dependencyNodesMap.get(otherNodeUuid)) {
                if (link.getNodeUuid().equals(nodeUuid)) {
                    linksToRemove.add(link);
                }
            }
            for (NamedLink linkToRemove : linksToRemove) {
                removeFromList(dependencyNodesMap, otherNodeUuid, linkToRemove);
                dependencyNodesByNameMap.remove(new NamedLink(otherNodeUuid, linkToRemove.getName()));
            }
        }

        // remove dependencies
        for (NamedLink link : dependencyNodesMap.get(nodeUuid)) {
            dependencyNodesByNameMap.remove(new NamedLink(nodeUuid, link.getName()));
            removeFromList(backwardDependencyNodesMap, link.getNodeUuid(), nodeUuid);
        }
        dependencyNodesMap.remove(nodeUuid);
        return parentNodeUuid;
    }

    @Override
    public Optional<InputStream> readBinaryData(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        byte[] value = dataMap.get(new NamedLink(nodeUuid, name));
        return Optional.ofNullable(value).map(ByteArrayInputStream::new);
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
                dataMap.put(new NamedLink(nodeUuid, name), toByteArray());
                addToSet(dataNamesMap, nodeUuid, name);
            }
        };
    }

    @Override
    public boolean dataExists(String nodeId, String name) {
        Objects.requireNonNull(name);
        return getDataNames(nodeId).contains(name);
    }

    @Override
    public Set<String> getDataNames(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        return dataNamesMap.get(nodeUuid);
    }

    @Override
    public boolean removeData(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        boolean removed = removeFromSet(dataNamesMap, nodeUuid, name);
        dataMap.remove(new NamedLink(nodeUuid, name));
        return removed;
    }

    @Override
    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(metadata);
        checkNodeExists(nodeUuid);
        Set<String> names = timeSeriesNamesMap.get(nodeUuid);
        if (names != null && names.contains(metadata.getName())) {
            throw new AfsStorageException("Time series " + metadata.getName() + " already exists at node " + nodeUuid);
        }
        addToSet(timeSeriesNamesMap, nodeUuid, metadata.getName());
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

    @Override
    public boolean timeSeriesExists(String nodeId, String timeSeriesName) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        Objects.requireNonNull(timeSeriesName);
        Set<String> timeSeriesNames = timeSeriesNamesMap.get(nodeUuid);
        return timeSeriesNames != null && timeSeriesNames.contains(timeSeriesName);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        List<TimeSeriesMetadata> metadataList = new ArrayList<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(nodeUuid, timeSeriesName));
            if (metadata != null) {
                metadataList.add(metadata);
            }
        }
        return metadataList;
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        return Stream.concat(doubleTimeSeriesChunksMap.keySet().stream(),
                stringTimeSeriesChunksMap.keySet().stream())
                .map(TimeSeriesChunkKey::getTimeSeriesKey)
                .filter(key -> key.getNodeUuid().equals(nodeUuid))
                .map(TimeSeriesKey::getVersion)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName) {
        UUID nodeUuid = checkNodeId(nodeId);
        checkNodeExists(nodeUuid);
        Objects.requireNonNull(timeSeriesName);
        return Stream.concat(doubleTimeSeriesChunksMap.keySet().stream(),
                             stringTimeSeriesChunksMap.keySet().stream())
                .map(TimeSeriesChunkKey::getTimeSeriesKey)
                .filter(key -> key.getNodeUuid().equals(nodeUuid) && key.getTimeSeriesName().equals(timeSeriesName))
                .map(TimeSeriesKey::getVersion)
                .collect(Collectors.toSet());
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P, C>> List<C> getChunks(UUID nodeId, int version, String timeSeriesName,
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

    private <P extends AbstractPoint, C extends ArrayChunk<P, C>>
        Map<String, List<C>> getTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version, ConcurrentMap<TimeSeriesChunkKey, C> map) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesIndex.checkVersion(version);
        Objects.requireNonNull(map);
        Map<String, List<C>> timeSeriesData = new HashMap<>();
        for (String timeSeriesName : timeSeriesNames) {
            TimeSeriesMetadata metadata = timeSeriesMetadataMap.get(new NamedLink(nodeUuid, timeSeriesName));
            if (metadata != null &&
                    ((metadata.getDataType() == TimeSeriesDataType.DOUBLE && map == doubleTimeSeriesChunksMap)
                        || (metadata.getDataType() == TimeSeriesDataType.STRING && map == stringTimeSeriesChunksMap))) {
                List<C> chunks = getChunks(nodeUuid, version, timeSeriesName, metadata, map);
                timeSeriesData.put(timeSeriesName, chunks);
            }
        }
        return timeSeriesData;
    }

    private <P extends AbstractPoint, C extends ArrayChunk<P, C>> void addTimeSeriesData(String nodeId,
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
    public Map<String, List<DoubleArrayChunk>> getDoubleTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeriesData(nodeId, timeSeriesNames, version, doubleTimeSeriesChunksMap);
    }

    @Override
    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, doubleTimeSeriesChunksMap);
    }

    @Override
    public Map<String, List<StringArrayChunk>> getStringTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        return getTimeSeriesData(nodeId, timeSeriesNames, version, stringTimeSeriesChunksMap);
    }

    @Override
    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        addTimeSeriesData(nodeId, version, timeSeriesName, chunks, stringTimeSeriesChunksMap);
    }

    @Override
    public void clearTimeSeries(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        Set<String> names = timeSeriesNamesMap.get(nodeUuid);
        if (names != null) {
            names.forEach(name -> timeSeriesMetadataMap.remove(new NamedLink(nodeUuid, name)));
            timeSeriesNamesMap.remove(nodeUuid);
        }
    }

    @Override
    public void addDependency(String nodeId, String name, String toNodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        UUID toNodeUuid = checkNodeId(toNodeId);
        checkNodeExists(nodeUuid);
        checkNodeExists(toNodeUuid);
        addToList(dependencyNodesMap, nodeUuid, new NamedLink(toNodeUuid, name));
        addToList(dependencyNodesByNameMap, new NamedLink(nodeUuid, name), toNodeUuid);
        addToList(backwardDependencyNodesMap, toNodeUuid, nodeUuid);
    }

    @Override
    public Set<NodeInfo> getDependencies(String nodeId, String name) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        checkNodeExists(nodeUuid);
        List<UUID> dependencyNodes = dependencyNodesByNameMap.get(new NamedLink(nodeUuid, name));
        if (dependencyNodes == null) {
            return Collections.emptySet();
        }
        return dependencyNodes.stream().map(this::getNodeInfo).collect(Collectors.toSet());
    }

    @Override
    public Set<NodeDependency> getDependencies(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        List<NamedLink> dependencyNodes = dependencyNodesMap.get(nodeUuid);
        if (dependencyNodes == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return dependencyNodes.stream()
                              .map(namedLink -> new NodeDependency(namedLink.getName(), getNodeInfo(namedLink.getNodeUuid())))
                              .collect(Collectors.toSet());
    }

    @Override
    public Set<NodeInfo> getBackwardDependencies(String nodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        List<UUID> backwardDependencyNodes = backwardDependencyNodesMap.get(nodeUuid);
        if (backwardDependencyNodes == null) {
            throw createNodeNotFoundException(nodeUuid);
        }
        return backwardDependencyNodes.stream()
                                      .map(this::getNodeInfo)
                                      .collect(Collectors.toSet());
    }

    @Override
    public void removeDependency(String nodeId, String name, String toNodeId) {
        UUID nodeUuid = checkNodeId(nodeId);
        Objects.requireNonNull(name);
        UUID toNodeUuid = checkNodeId(toNodeId);
        checkNodeExists(nodeUuid);
        checkNodeExists(toNodeUuid);
        removeFromList(dependencyNodesMap, nodeUuid, new NamedLink(toNodeUuid, name));
        removeFromList(dependencyNodesByNameMap, new NamedLink(nodeUuid, name), toNodeUuid);
        removeFromList(backwardDependencyNodesMap, toNodeUuid, nodeUuid);
    }

    @Override
    public void flush() {
        db.commit();
    }

    @Override
    public boolean isClosed() {
        return db.isClosed();
    }

    @Override
    public void close() {
        db.commit();
        db.close();
    }
}
