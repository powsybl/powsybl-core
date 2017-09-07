/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import eu.itesla_project.afs.Folder;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.computation.ComputationManager;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemStorage implements AppFileSystemStorage {

    private final Path rootDir;

    private final String fileSystemName;

    private final List<LocalFileStorageExtension> extensions;

    private final ComputationManager computationManager;

    private final Map<Path, LocalFileStorage> cache = new HashMap<>();

    public LocalAppFileSystemStorage(Path rootDir, String fileSystemName, List<LocalFileStorageExtension> extensions,
                                     ComputationManager computationManager) {
        this.rootDir = Objects.requireNonNull(rootDir);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.extensions = Objects.requireNonNull(extensions);
        this.computationManager = Objects.requireNonNull(computationManager);
    }

    private LocalFileStorage scan(Path path, boolean useCache) {
        LocalFileStorage file = null;
        if (useCache && cache.containsKey(path)) {
            file = cache.get(path);
        } else {
            for (LocalFileStorageExtension scanner : extensions) {
                file = scanner.scan(path, computationManager);
                if (file != null) {
                    break;
                }
            }
            cache.put(path, file);
        }
        return file;
    }

    @Override
    public NodeId fromString(String str) {
        return new PathNodeId(rootDir.getFileSystem().getPath(str));
    }

    @Override
    public NodeId getRootNode() {
        return new PathNodeId(rootDir);
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFileStorage file = scan(path, true);
        if (file != null) {
            return file.getPseudoClass();
        } else if (Files.isDirectory(path)) {
            return Folder.PSEUDO_CLASS;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFileStorage file = scan(path, true);
        if (file != null) {
            return file.getName();
        } else if (Files.isDirectory(path)) {
            return path.equals(rootDir) ? fileSystemName : path.getFileName().toString();
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public List<NodeId> getChildNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        if (Files.isDirectory(path)) {
            List<NodeId> childNodesIds = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path childPath : stream) {
                    if (Files.isDirectory(childPath) ||
                            scan(childPath, false) != null) {
                        childNodesIds.add(new PathNodeId(childPath));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return childNodesIds;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public NodeId getChildNode(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Objects.requireNonNull(name);
        Path path = ((PathNodeId) nodeId).getPath();
        Path childPath = path.resolve(name);
        if (Files.isDirectory(childPath) ||
                (Files.exists(childPath) && scan(childPath, false) != null)) {
            return new PathNodeId(childPath);
        }
        return null;
    }

    @Override
    public NodeId getParentNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        return path.equals(rootDir) ? null : new PathNodeId(path.getParent());
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
    public NodeId createNode(NodeId parentNodeId, String name, String nodePseudoClass) {
        throw new AssertionError();
    }

    @Override
    public void deleteNode(NodeId nodeId) {
        throw new AssertionError();
    }

    @Override
    public String getStringAttribute(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFileStorage file = scan(path, true);
        if (file != null) {
            return file.getStringAttribute(name);
        }
        throw new AssertionError();
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
        throw new AssertionError();
    }

    @Override
    public void setIntAttribute(NodeId nodeId, String name, int value) {
        throw new AssertionError();
    }

    @Override
    public OptionalDouble getDoubleAttribute(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void setDoubleAttribute(NodeId nodeId, String name, double value) {
        throw new AssertionError();
    }

    @Override
    public Optional<Boolean> getBooleanAttribute(NodeId nodeId, String name) {
        throw new AssertionError();
    }

    @Override
    public void setBooleanAttribute(NodeId nodeId, String name, boolean value) {
        throw new AssertionError();
    }

    @Override
    public DataSource getDataSourceAttribute(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        LocalFileStorage file = scan(path, true);
        if (file != null) {
            return file.getDataSourceAttribute(name);
        }
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
    public NodeId getProjectRootNode(NodeId projectId) {
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
    public void commit() {
    }

    @Override
    public void rollback() {
    }

    @Override
    public void close() {
    }
}
