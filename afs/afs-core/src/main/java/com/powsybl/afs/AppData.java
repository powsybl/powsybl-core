/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.ListenableAppStorage;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.local.LocalComputationManager;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppData implements AutoCloseable {

    private final ComputationManager computationManager;

    private final Map<String, AppFileSystem> fileSystems = new HashMap<>();

    private final Map<Class<? extends File>, FileExtension> fileExtensions = new HashMap<>();

    private final Map<String, FileExtension> fileExtensionsByPseudoClass = new HashMap<>();

    private final Map<Class<?>, ProjectFileExtension> projectFileExtensions = new HashMap<>();

    private final Map<String, ProjectFileExtension> projectFileExtensionsByPseudoClass = new HashMap<>();

    private final Set<Class<? extends ProjectFile>> projectFileClasses = new HashSet<>();

    private final Map<ServiceExtension.ServiceKey, Object> services = new HashMap<>();

    private final Supplier<AppLogger> loggerFactory;

    private static class NoOpAppLogger implements AppLogger {

        @Override
        public AppLogger tagged(String tag) {
            return this;
        }

        @Override
        public void log(String message, Object... args) {
            // no-op
        }
    }

    public AppData() {
        this(LocalComputationManager.getDefault());
    }

    public AppData(ComputationManager computationManager) {
        this(computationManager,
                new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices(),
                new ServiceLoaderCache<>(FileExtension.class).getServices(),
                new ServiceLoaderCache<>(ProjectFileExtension.class).getServices(),
                new ServiceLoaderCache<>(ServiceExtension.class).getServices());
    }

    public AppData(ComputationManager computationManager, List<AppFileSystemProvider> fileSystemProviders,
                   List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions,
                   List<ServiceExtension> serviceExtensions) {
        this(computationManager, fileSystemProviders, fileExtensions, projectFileExtensions, serviceExtensions,
                NoOpAppLogger::new);
    }

    public AppData(ComputationManager computationManager, List<AppFileSystemProvider> fileSystemProviders,
                   List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions,
                   List<ServiceExtension> serviceExtensions, Supplier<AppLogger> loggerFactory) {
        this.computationManager = Objects.requireNonNull(computationManager);
        this.loggerFactory = Objects.requireNonNull(loggerFactory);
        Objects.requireNonNull(fileSystemProviders);
        Objects.requireNonNull(fileExtensions);
        Objects.requireNonNull(projectFileExtensions);
        for (AppFileSystemProvider provider : fileSystemProviders) {
            for (AppFileSystem fileSystem : provider.getFileSystems(computationManager)) {
                addFileSystem(fileSystem);
            }
        }
        for (FileExtension extension : fileExtensions) {
            this.fileExtensions.put(extension.getFileClass(), extension);
            this.fileExtensionsByPseudoClass.put(extension.getFilePseudoClass(), extension);
        }
        for (ProjectFileExtension extension : projectFileExtensions) {
            this.projectFileExtensions.put(extension.getProjectFileClass(), extension);
            this.projectFileExtensions.put(extension.getProjectFileBuilderClass(), extension);
            this.projectFileExtensionsByPseudoClass.put(extension.getProjectFilePseudoClass(), extension);
            this.projectFileClasses.add(extension.getProjectFileClass());
        }
        for (ServiceExtension extension : serviceExtensions) {
            this.services.put(extension.getServiceKey(), extension.createService());
        }
    }

    public AppLogger createLogger() {
        AppLogger logger = loggerFactory.get();
        if (logger == null) {
            throw new NullPointerException("Null logger");
        }
        return logger;
    }

    public void addFileSystem(AppFileSystem fileSystem) {
        Objects.requireNonNull(fileSystem);
        if (fileSystems.containsKey(fileSystem.getName())) {
            throw new AfsException("A file system with the same name '" + fileSystem.getName() + "' already exists");
        }
        fileSystem.setData(this);
        fileSystems.put(fileSystem.getName(), fileSystem);
    }

    public Collection<AppFileSystem> getFileSystems() {
        return fileSystems.values();
    }

    public AppFileSystem getFileSystem(String name) {
        Objects.requireNonNull(name);
        return fileSystems.get(name);
    }

    public Node getNode(String pathStr) {
        Objects.requireNonNull(pathStr);
        String[] path = pathStr.split(AppFileSystem.FS_SEPARATOR + AppFileSystem.PATH_SEPARATOR);
        if (path.length == 0) { // wrong file system name
            return null;
        }
        String fileSystemName = path[0];
        AppFileSystem fileSystem = fileSystems.get(fileSystemName);
        if (fileSystem == null) {
            return null;
        }
        return path.length == 1 ? fileSystem.getRootFolder() : fileSystem.getRootFolder().getChild(path[1], path.length > 2 ? Arrays.copyOfRange(path, 2, path.length - 1) : new String[] {});
    }

    public Set<Class<? extends ProjectFile>> getProjectFileClasses() {
        return projectFileClasses;
    }

    FileExtension getFileExtension(Class<? extends File> fileClass) {
        Objects.requireNonNull(fileClass);
        FileExtension extension = fileExtensions.get(fileClass);
        if (extension == null) {
            throw new AfsException("No extension found for file class '" + fileClass.getName() + "'");
        }
        return extension;
    }

    FileExtension getFileExtensionByPseudoClass(String filePseudoClass) {
        Objects.requireNonNull(filePseudoClass);
        return fileExtensionsByPseudoClass.get(filePseudoClass);
    }

    ProjectFileExtension getProjectFileExtension(Class<?> projectFileOrProjectFileBuilderClass) {
        Objects.requireNonNull(projectFileOrProjectFileBuilderClass);
        ProjectFileExtension extension = projectFileExtensions.get(projectFileOrProjectFileBuilderClass);
        if (extension == null) {
            throw new AfsException("No extension found for project file or project file builder class '"
                    + projectFileOrProjectFileBuilderClass.getName() + "'");
        }
        return extension;
    }

    ProjectFileExtension getProjectFileExtensionByPseudoClass(String projectFilePseudoClass) {
        Objects.requireNonNull(projectFilePseudoClass);
        return projectFileExtensionsByPseudoClass.get(projectFilePseudoClass);
    }

    public ComputationManager getComputationManager() {
        return computationManager;
    }

    public List<String> getRemotelyAccessibleFileSystemNames() {
        return fileSystems.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(AppFileSystem::isRemotelyAccessible)
                .map(AppFileSystem::getName)
                .collect(Collectors.toList());
    }

    public ListenableAppStorage getRemotelyAccessibleStorage(String fileSystemName) {
        AppFileSystem afs = fileSystems.get(fileSystemName);
        return afs != null ? afs.getStorage() : null;
    }

    <U> U findService(Class<U> serviceClass, boolean remote) {
        ServiceExtension.ServiceKey<U> serviceKey = new ServiceExtension.ServiceKey<>(serviceClass, remote);
        U service = (U) services.get(serviceKey);
        if (service == null) {
            throw new AfsException("No service found for key " + serviceKey + "");
        }
        return service;
    }

    @Override
    public void close() {
        getFileSystems().forEach(AppFileSystem::close);
    }
}
