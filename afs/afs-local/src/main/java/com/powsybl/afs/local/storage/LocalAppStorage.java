/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.Folder;
import com.powsybl.afs.storage.*;
import com.powsybl.commons.exceptions.UncheckedUnsupportedEncodingException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.timeseries.DoubleDataChunk;
import com.powsybl.timeseries.StringDataChunk;
import com.powsybl.timeseries.TimeSeriesMetadata;
import com.powsybl.timeseries.TimeSeriesVersions;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppStorage extends AbstractAppStorage {

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

    private Path nodeIdToPath(String nodeId) {
        Objects.requireNonNull(nodeId);
        try {
            return rootDir.getFileSystem().getPath(URLDecoder.decode(nodeId, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedUnsupportedEncodingException(e);
        }
    }

    private String pathToNodeId(Path path) {
        Objects.requireNonNull(path);
        try {
            return URLEncoder.encode(path.toString(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedUnsupportedEncodingException(e);
        }
    }

    @Override
    public NodeInfo createRootNodeIfNotExists(String name, String nodePseudoClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodePseudoClass);
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(rootDir, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new NodeInfo(pathToNodeId(rootDir),
                            name,
                            nodePseudoClass,
                            "",
                            attr.creationTime().toMillis(),
                            attr.lastModifiedTime().toMillis(),
                            DEFAULT_VERSION,
                            new NodeGenericMetadata());
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) {
        Path path = nodeIdToPath(nodeId);
        return getNodeInfo(path);
    }

    private NodeInfo getNodeInfo(Path path) {
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LocalFile file = scanFile(path, true);
        if (file != null) {
            return new NodeInfo(pathToNodeId(path),
                                file.getName(),
                                file.getPseudoClass(),
                                file.getDescription(),
                                attr.creationTime().toMillis(),
                                attr.lastModifiedTime().toMillis(),
                                DEFAULT_VERSION,
                                file.getGenericMetadata());
        } else {
            LocalFolder folder = scanFolder(path, true);
            if (folder != null) {
                return new NodeInfo(pathToNodeId(path),
                                    folder.getName(),
                                    Folder.PSEUDO_CLASS,
                                    "",
                                    attr.creationTime().toMillis(),
                                    attr.lastModifiedTime().toMillis(),
                                    DEFAULT_VERSION,
                                    new NodeGenericMetadata());
            } else {
                throw new AssertionError();
            }
        }
    }

    @Override
    public void setDescription(String nodeId, String description) {
        throw new AssertionError();
    }

    @Override
    public void setConsistent(String nodeId) {
        throw new AssertionError();
    }

    @Override
    public void renameNode(String nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void updateModificationTime(String nodeId) {
        throw new AssertionError();
    }

    private boolean isLocalNode(Path path) {
        return scanFolder(path, false) != null || scanFile(path, false) != null;
    }

    @Override
    public List<NodeInfo> getChildNodes(String nodeId) {
        Path path = nodeIdToPath(nodeId);
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            return folder.getChildPaths().stream()
                    .filter(this::isLocalNode)
                    .map(this::getNodeInfo)
                    .collect(Collectors.toList());
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public Optional<NodeInfo> getChildNode(String nodeId, String name) {
        Path path = nodeIdToPath(nodeId);
        Objects.requireNonNull(name);
        LocalFolder folder = scanFolder(path, false);
        if (folder != null) {
            Optional<Path> childPath = folder.getChildPath(name);
            if (childPath.isPresent() && isLocalNode(childPath.get())) {
                return childPath.map(this::getNodeInfo);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<NodeInfo> getParentNode(String nodeId) {
        Path path = nodeIdToPath(nodeId);
        Optional<Path> parentPath;
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
        return parentPath.map(this::getNodeInfo);
    }

    @Override
    public void setParentNode(String nodeId, String newParentString) {
        throw new AssertionError();
    }

    @Override
    public boolean isWritable(String nodeId) {
        return false;
    }

    @Override
    public boolean isConsistent(String nodeId) {
        return true;
    }

    @Override
    public NodeInfo createNode(String parentString, String name, String nodePseudoClass, String description, int version, NodeGenericMetadata genericMetadata) {
        throw new AssertionError();
    }

    @Override
    public String deleteNode(String nodeId) {
        throw new AssertionError();
    }

    private LocalFile getFile(String nodeId) {
        Path path = nodeIdToPath(nodeId);
        LocalFile file = scanFile(path, true);
        if (file == null) {
            throw new AssertionError();
        }
        return file;
    }

    @Override
    public Optional<InputStream> readBinaryData(String nodeId, String name) {
        Objects.requireNonNull(name);
        return getFile(nodeId).readBinaryData(name);
    }

    @Override
    public OutputStream writeBinaryData(String nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public boolean dataExists(String nodeId, String name) {
        Objects.requireNonNull(name);

        return getFile(nodeId).dataExists(name);
    }

    @Override
    public Set<String> getDataNames(String nodeId) {
        return getFile(nodeId).getDataNames();
    }

    @Override
    public boolean removeData(String nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void createTimeSeries(String nodeId, TimeSeriesMetadata metadata) {
        throw new AssertionError();
    }

    @Override
    public Set<String> getTimeSeriesNames(String nodeId) {
        return getFile(nodeId).getTimeSeriesNames();
    }

    @Override
    public boolean timeSeriesExists(String nodeId, String timeSeriesName) {
        return getFile(nodeId).timeSeriesExists(timeSeriesName);
    }

    @Override
    public List<TimeSeriesMetadata> getTimeSeriesMetadata(String nodeId, Set<String> timeSeriesNames) {
        Objects.requireNonNull(timeSeriesNames);
        return getFile(nodeId).getTimeSeriesMetadata(timeSeriesNames);
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId) {
        return getFile(nodeId).getTimeSeriesDataVersions();
    }

    @Override
    public Set<Integer> getTimeSeriesDataVersions(String nodeId, String timeSeriesName) {
        Objects.requireNonNull(timeSeriesName);
        return getFile(nodeId).getTimeSeriesDataVersions(timeSeriesName);
    }

    @Override
    public Map<String, List<DoubleDataChunk>> getDoubleTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesVersions.check(version);
        return getFile(nodeId).getDoubleTimeSeriesData(timeSeriesNames, version);
    }

    @Override
    public void addDoubleTimeSeriesData(String nodeId, int version, String timeSeriesName, List<DoubleDataChunk> chunks) {
        throw new AssertionError();
    }

    @Override
    public Map<String, List<StringDataChunk>> getStringTimeSeriesData(String nodeId, Set<String> timeSeriesNames, int version) {
        Objects.requireNonNull(timeSeriesNames);
        TimeSeriesVersions.check(version);
        return getFile(nodeId).getStringTimeSeriesData(timeSeriesNames, version);
    }

    @Override
    public void addStringTimeSeriesData(String nodeId, int version, String timeSeriesName, List<StringDataChunk> chunks) {
        throw new AssertionError();
    }

    @Override
    public void clearTimeSeries(String nodeId) {
        throw new AssertionError();
    }

    @Override
    public void addDependency(String nodeId, String name, String toNodeId) {
        throw new AssertionError();
    }

    @Override
    public Set<NodeInfo> getDependencies(String nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public Set<NodeDependency> getDependencies(String nodeId) {
        throw new AssertionError();
    }

    @Override
    public Set<NodeInfo> getBackwardDependencies(String nodeId) {
        throw new AssertionError();
    }

    @Override
    public void removeDependency(String nodeId, String name, String toNodeId) {
        throw new AssertionError();
    }

    @Override
    public void flush() {
        // read only storage so nothing to flush
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {
        // nothing to close
    }
}
