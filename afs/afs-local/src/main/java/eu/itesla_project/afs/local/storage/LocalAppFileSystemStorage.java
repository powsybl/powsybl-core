/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import eu.itesla_project.afs.ext.base.Case;
import eu.itesla_project.afs.core.Folder;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.import_.*;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystemStorage implements AppFileSystemStorage {

    private final Path rootDir;

    private final String fileSystemName;

    private final ComputationManager computationManager;

    private final ImportConfig importConfig;

    private final ImportersLoader importersLoader;

    public LocalAppFileSystemStorage(Path rootDir, String fileSystemName, ComputationManager computationManager,
                                     ImportConfig importConfig, ImportersLoader importersLoader) {
        this.rootDir = Objects.requireNonNull(rootDir);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
        this.computationManager = Objects.requireNonNull(computationManager);
        this.importConfig = Objects.requireNonNull(importConfig);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    @Override
    public NodeId fromString(String str) {
        return new PathNodeId(rootDir.getFileSystem().getPath(str));
    }

    @Override
    public NodeId getRootNode() {
        return new PathNodeId(rootDir);
    }

    private Importer findImporter(Path path) {
        ReadOnlyDataSource dataSource = Importers.createDataSource(path);
        for (Importer importer : Importers.list(importersLoader, computationManager, importConfig)) {
            if (importer.exists(dataSource)) {
                return importer;
            }
        }
        return null;
    }

    @Override
    public String getNodePseudoClass(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        if (Files.isDirectory(path)) {
            return Folder.PSEUDO_CLASS;
        } else if (findImporter(path) != null) {
            return Case.PSEUDO_CLASS;
        } else {
            throw new AssertionError();
        }
    }

    @Override
    public String getNodeName(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        return path.equals(rootDir) ? fileSystemName : path.getFileName().toString();
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
                            findImporter(childPath) != null) {
                        childNodesIds.add(new PathNodeId(childPath));
                    }
                }
            } catch(IOException e) {
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
                (Files.exists(childPath) && findImporter(childPath) != null)) {
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
        switch (name) {
            case "format":
                return findImporter(path).getFormat();

            case "description":
                return findImporter(path).getComment();

            default:
                throw new AssertionError(name);
        }
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
    public DataSource getDataSourceAttribute(NodeId nodeId, String name) {
        Objects.requireNonNull(nodeId);
        Path path = ((PathNodeId) nodeId).getPath();
        switch (name) {
            case "dataSource":
                return Importers.createDataSource(path);

            default:
                throw new AssertionError();
        }
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
