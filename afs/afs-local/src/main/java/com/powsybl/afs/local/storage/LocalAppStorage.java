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
                            DEFAULT_VERSION,
                            Collections.emptyMap(),
                            Collections.emptyMap(),
                            Collections.emptyMap(),
                            Collections.emptyMap());
    }

    @Override
    public NodeInfo getNodeInfo(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LocalFile file = scanFile(path, true);
        if (file != null) {
            return new NodeInfo(nodeId,
                                file.getName(),
                                file.getPseudoClass(),
                                file.getDescription(),
                                attr.creationTime().toMillis(),
                                attr.lastModifiedTime().toMillis(),
                                DEFAULT_VERSION,
                                file.getStringMetadata(),
                                file.getDoubleMetadata(),
                                file.getIntMetadata(),
                                file.getBooleanMetadata());
        } else {
            LocalFolder folder = scanFolder(path, true);
            if (folder != null) {
                return new NodeInfo(nodeId,
                                    folder.getName(),
                                    Folder.PSEUDO_CLASS,
                                    "",
                                    attr.creationTime().toMillis(),
                                    attr.lastModifiedTime().toMillis(),
                                    DEFAULT_VERSION,
                                    Collections.emptyMap(),
                                    Collections.emptyMap(),
                                    Collections.emptyMap(),
                                    Collections.emptyMap());
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
    public List<NodeInfo> getChildNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            return folder.getChildPaths().stream()
                    .filter(this::isLocalNode)
                    .map(PathNodeId::new)
                    .map(this::getNodeInfo)
                    .collect(Collectors.toList());
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public NodeInfo getChildNode(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            Path childPath = folder.getChildPath(name);
            if (childPath != null && isLocalNode(childPath)) {
                return getNodeInfo(new PathNodeId(childPath));
            }
        }
        return null;
    }

    @Override
    public NodeInfo getParentNode(NodeId nodeId) {
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
        return parentPath == null ? null : getNodeInfo(new PathNodeId(parentPath));
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
    public NodeInfo createNode(NodeId parentNodeId, String name, String nodePseudoClass, String description, int version, Map<String, String> stringMetadata,
                               Map<String, Double> doubleMetadata, Map<String, Integer> intMetadata, Map<String, Boolean> booleanMetadata) {
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
    public Reader readStringData(NodeId nodeId, String name) {
        return getFile(nodeId).readStringData(name);
    }

    @Override
    public Writer writeStringData(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public InputStream readBinaryData(NodeId nodeId, String name) {
        return getFile(nodeId).readBinaryData(name);
    }

    @Override
    public OutputStream writeBinaryData(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public boolean dataExists(NodeId nodeId, String name) {
        return getFile(nodeId).dataExists(name);
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
    public NodeInfo getDependency(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void addDependency(NodeId nodeId, String name, NodeId toNodeId) {
        throw new AssertionError();
    }

    @Override
    public List<NodeInfo> getDependencies(NodeId nodeId) {
        throw new AssertionError();
    }

    @Override
    public List<NodeInfo> getBackwardDependencies(NodeId nodeId) {
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
