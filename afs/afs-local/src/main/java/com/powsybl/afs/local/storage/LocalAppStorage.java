/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.Folder;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.computation.ComputationManager;
import com.powsybl.math.timeseries.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppStorage implements AppStorage {

    private static final int DEFAULT_VERSION = 0;

    private final Path rootDir;

    private final String fileSystemName;

    private final List<LocalFileScanner> fileScanners;

    private final List<LocalFolderScanner> folderScanners;

    private final ComputationManager computationManager;

    private final Map<Path, LocalFile> fileCache = new HashMap<>();

    private final Map<Path, LocalFolder> folderCache = new HashMap<>();

    public LocalAppStorage(Path rootDir, String fileSystemName, List<LocalFileScanner> fileScanners,
                           List<LocalFolderScanner> folderScanners, ComputationManager computationManager) {
        this.rootDir = Objects.requireNonNull(rootDir);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.fileScanners = Objects.requireNonNull(fileScanners);
        this.folderScanners = ImmutableList.<LocalFolderScanner>builder()
                .addAll(Objects.requireNonNull(folderScanners))
                .add(new DefaultLocalFolderScanner())
                .build();
        this.computationManager = Objects.requireNonNull(computationManager);
    }

    private LocalFile scanFile(Path path, boolean useCache) {
        LocalFile file = null;
        if (Files.isReadable(path)) {
            if (useCache && fileCache.containsKey(path)) {
                file = fileCache.get(path);
            } else {
                LocalFileScannerContext context = new LocalFileScannerContext(computationManager);
                for (LocalFileScanner fileScanner : fileScanners) {
                    file = fileScanner.scanFile(path, context);
                    if (file != null) {
                        break;
                    }
                }
                fileCache.put(path, file);
            }
        }
        return file;
    }

    private LocalFolder scanFolder(Path path, boolean useCache) {
        LocalFolder folder = null;
        if (Files.isReadable(path)) {
            if (useCache && folderCache.containsKey(path)) {
                folder = folderCache.get(path);
            } else {
                LocalFolderScannerContext context = new LocalFolderScannerContext(rootDir, fileSystemName, computationManager);
                for (LocalFolderScanner folderScanner : folderScanners) {
                    folder = folderScanner.scanFolder(path, context);
                    if (folder != null) {
                        break;
                    }
                }
                folderCache.put(path, folder);
            }
        }
        return folder;
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
        return new PathNodeId(rootDir.getFileSystem().getPath(str));
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(rootDir, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new NodeInfo(new PathNodeId(rootDir),
                            name,
                            nodePseudoClass,
                            "",
                            attr.creationTime().toMillis(),
                            attr.lastModifiedTime().toMillis(),
                            DEFAULT_VERSION);
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        return getNodeInfo(nodeId).getPseudoClass();
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        return getNodeInfo(nodeId).getName();
    }

    @Override
    public NodeInfo getNodeInfo(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(rootDir, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LocalFile file = scanFile(path, true);
        if (file != null) {
            return new NodeInfo(nodeId,
                                file.getName(),
                                file.getPseudoClass(),
                                "",
                                attr.creationTime().toMillis(),
                                attr.lastModifiedTime().toMillis(),
                                DEFAULT_VERSION);
        } else {
            LocalFolder folder = scanFolder(path, true);
            if (folder != null) {
                return new NodeInfo(nodeId,
                                    folder.getName(),
                                    Folder.PSEUDO_CLASS,
                                    "",
                                    attr.creationTime().toMillis(),
                                    attr.lastModifiedTime().toMillis(),
                                    DEFAULT_VERSION);
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public void setDescription(NodeId nodeId, String description) {
        throw new AssertionError();
    }

    private boolean isLocalNode(Path path) {
        return scanFolder(path, false) != null || scanFile(path, false) != null;
    }

    @Override
    public List<NodeId> getChildNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        List<NodeId> childNodesIds = new ArrayList<>();
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            childNodesIds.addAll(folder.getChildPaths().stream()
                    .filter(this::isLocalNode)
                    .map(PathNodeId::new)
                    .collect(Collectors.toList()));
        } else {
            throw new AssertionError();
        }
        return childNodesIds;
    }

    @Override
    public NodeId getChildNode(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            Path childPath = folder.getChildPath(name);
            if (childPath != null && isLocalNode(childPath)) {
                return new PathNodeId(childPath);
            }
        }
        return null;
    }

    @Override
    public NodeId getParentNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        Path parentPath;
        LocalFile file = scanFile(path, true);
        if (file != null) {
            parentPath = file.getParentPath();
        } else {
            LocalFolder folder = scanFolder(path, true);
            if (folder != null) {
                parentPath = folder.getParentPath();
            } else {
                throw new AssertionError();
            }
        }
        return parentPath == null ? null : new PathNodeId(parentPath);
    }

    @Override
    public void setParentNode(NodeId nodeId, NodeId newParentNodeId) {
        throw new AssertionError();
    }

    @Override
    public boolean isWritable(NodeId nodeId) {
        return false;
    }

    @Override
    public NodeInfo createNode(NodeId parentNodeId, String name, String nodePseudoClass, int version) {
        throw new AssertionError();
    }

    @Override
    public void deleteNode(NodeId nodeId) {
        throw new AssertionError();
    }

    private LocalFile getFile(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFile file = scanFile(path, true);
        if (file == null) {
            throw new AssertionError();
        }
        return file;
    }

    @Override
    public String getStringAttribute(NodeId nodeId, String name) {
        return getFile(nodeId).getStringAttribute(name);
    }

    @Override
    public void setStringAttribute(NodeId nodeId, String name, String value) {
        throw new AssertionError();
    }

    @Override
    public Reader readStringAttribute(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public Writer writeStringAttribute(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public OptionalInt getIntAttribute(NodeId nodeId, String name) {
        return getFile(nodeId).getIntAttribute(name);
    }

    @Override
    public void setIntAttribute(NodeId nodeId, String name, int value) {
        throw new AssertionError();
    }

    @Override
    public OptionalDouble getDoubleAttribute(NodeId nodeId, String name) {
        return getFile(nodeId).getDoubleAttribute(name);
    }

    @Override
    public void setDoubleAttribute(NodeId nodeId, String name, double value) {
        throw new AssertionError();
    }

    @Override
    public Optional<Boolean> getBooleanAttribute(NodeId nodeId, String name) {
        return getFile(nodeId).getBooleanAttribute(name);
    }

    @Override
    public void setBooleanAttribute(NodeId nodeId, String name, boolean value) {
        throw new AssertionError();
    }

    @Override
    public DataSource getDataSourceAttribute(NodeId nodeId, String name) {
        return getFile(nodeId).getDataSourceAttribute(name);
    }

    @Override
    public void createTimeSeries(NodeId nodeId, TimeSeriesMetadata metadata) {
        throw new AssertionError();
    }

    @Override
    public Set<String> getTimeSeriesNames(NodeId nodeId) {
        return getFile(nodeId).getTimeSeriesNames();
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(NodeId nodeId, Set<String> timeSeriesNames) {
        return getFile(nodeId).getTimeSeriesMetadata(timeSeriesNames);
    }

    @Override
    public List<DoubleTimeSeries> getDoubleTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return getFile(nodeId).getDoubleTimeSeries(timeSeriesNames, version);
    }

    @Override
    public void addDoubleTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<DoubleArrayChunk> chunks) {
        throw new AssertionError();
    }

    @Override
    public List<StringTimeSeries> getStringTimeSeries(NodeId nodeId, Set<String> timeSeriesNames, int version) {
        return getFile(nodeId).getStringTimeSeries(timeSeriesNames, version);
    }

    @Override
    public void addStringTimeSeriesData(NodeId nodeId, int version, String timeSeriesName, List<StringArrayChunk> chunks) {
        throw new AssertionError();
    }

    @Override
    public void removeAllTimeSeries(NodeId nodeId) {
        throw new AssertionError();
    }

    @Override
    public NodeId getDependency(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        throw new AssertionError();
    }

    @Override
    public List<NodeId> getDependencies(NodeId nodeId) {
        throw new AssertionError();
    }

    @Override
    public List<NodeId> getBackwardDependencies(NodeId nodeId) {
        throw new AssertionError();
    }

    @Override
    public InputStream readFromCache(NodeId projectFileId, String key) {
        throw new AssertionError();
    }

    @Override
    public OutputStream writeToCache(NodeId projectFileId, String key) {
        throw new AssertionError();
    }

    @Override
    public void invalidateCache(NodeId projectFileId, String key) {
        throw new AssertionError();
    }

    @Override
    public void invalidateCache() {
        throw new AssertionError();
    }

    @Override
    public void flush() {
        // read only storage so nothing to flush
    }

    @Override
    public void close() {
        // nothing to close
    }
}
