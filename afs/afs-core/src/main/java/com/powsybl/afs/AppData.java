/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.EventsBus;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An instance of AppData is the root of an application file system.
 *
 * Usually, an application will only have one instance of AppData.
 * It is the first entrypoint of AFS, through which you can access to individual {@link AppFileSystem} objects.
 *
 * <pre>
 *   //Get AppData instance.
 *   AppData appData = ...
 *
 *   //Print file system names to console
 *   appData.getFileSystems().stream().map(AppFileSystem::getName).forEach(System.out::println);
 *
 *   //Get file system with name "fs1"
 *   AppFileSystem fs1 = appData.getFileSystem("fs1");
 *
 *   //Get root folder of "fs1"
 *   Folder root = fs1.getRootFolder();
 *
 *   //Get the node of type Project at /folder1/folder2/my_project, if it exists.
 *   Optional&lt;Project&gt; project = root.getChild(Project.class, "folder1", "folder2", "my_project");
 *
 *   ...
 *
 * </pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppData implements AutoCloseable {

    private final ComputationManager shortTimeExecutionComputationManager;

    private final ComputationManager longTimeExecutionComputationManager;

    private Map<String, AppFileSystem> fileSystems;

    private final List<AppFileSystemProvider> fileSystemProviders;

    private final List<ServiceExtension> serviceExtensions;

    private final Map<Class<? extends File>, FileExtension> fileExtensions = new HashMap<>();

    private final Map<String, FileExtension> fileExtensionsByPseudoClass = new HashMap<>();

    private final Map<Class<?>, ProjectFileExtension> projectFileExtensions = new HashMap<>();

    private final Map<String, ProjectFileExtension> projectFileExtensionsByPseudoClass = new HashMap<>();

    private final Set<Class<? extends ProjectFile>> projectFileClasses = new HashSet<>();

    private final EventsBus eventsBus;

    private Map<ServiceExtension.ServiceKey, Object> services;

    private SecurityTokenProvider tokenProvider = () -> null;

    public AppData(ComputationManager shortTimeExecutionComputationManager, ComputationManager longTimeExecutionComputationManager) {
        this(shortTimeExecutionComputationManager, longTimeExecutionComputationManager, getDefaultEventsBus());
    }

    public AppData(ComputationManager shortTimeExecutionComputationManager, ComputationManager longTimeExecutionComputationManager, EventsBus eventsBus) {
        this(shortTimeExecutionComputationManager, longTimeExecutionComputationManager,
                getDefaultFileSystemProviders(), getDefaultFileExtensions(), getDefaultProjectFileExtensions(), getDefaultServiceExtensions(), eventsBus);
    }

    public AppData(ComputationManager shortTimeExecutionComputationManager,
                   ComputationManager longTimeExecutionComputationManager, List<AppFileSystemProvider> fileSystemProviders) {
        this(shortTimeExecutionComputationManager, longTimeExecutionComputationManager,
                fileSystemProviders, getDefaultEventsBus());
    }

    public AppData(ComputationManager shortTimeExecutionComputationManager,
                   ComputationManager longTimeExecutionComputationManager, List<AppFileSystemProvider> fileSystemProviders, EventsBus eventsBus) {
        this(shortTimeExecutionComputationManager, longTimeExecutionComputationManager,
                fileSystemProviders, getDefaultFileExtensions(), getDefaultProjectFileExtensions(), getDefaultServiceExtensions(), eventsBus);
    }

    public AppData(ComputationManager shortTimeExecutionComputationManager, ComputationManager longTimeExecutionComputationManager,
                   List<AppFileSystemProvider> fileSystemProviders, List<FileExtension> fileExtensions,
                   List<ProjectFileExtension> projectFileExtensions, List<ServiceExtension> serviceExtensions) {
        this(shortTimeExecutionComputationManager, longTimeExecutionComputationManager, fileSystemProviders, fileExtensions,
                projectFileExtensions, serviceExtensions, getDefaultEventsBus());
    }

    public AppData(ComputationManager shortTimeExecutionComputationManager, ComputationManager longTimeExecutionComputationManager,
                   List<AppFileSystemProvider> fileSystemProviders, List<FileExtension> fileExtensions,
                   List<ProjectFileExtension> projectFileExtensions, List<ServiceExtension> serviceExtensions, EventsBus eventsBus) {
        Objects.requireNonNull(fileSystemProviders);
        Objects.requireNonNull(fileExtensions);
        Objects.requireNonNull(projectFileExtensions);
        this.eventsBus = Objects.requireNonNull(eventsBus);
        this.shortTimeExecutionComputationManager = Objects.requireNonNull(shortTimeExecutionComputationManager);
        this.longTimeExecutionComputationManager = longTimeExecutionComputationManager;
        this.fileSystemProviders = Objects.requireNonNull(fileSystemProviders);
        this.serviceExtensions = Objects.requireNonNull(serviceExtensions);
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
    }

    private static List<AppFileSystemProvider> getDefaultFileSystemProviders() {
        return new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices();
    }

    private static List<FileExtension> getDefaultFileExtensions() {
        return new ServiceLoaderCache<>(FileExtension.class).getServices();
    }

    private static List<ProjectFileExtension> getDefaultProjectFileExtensions() {
        return new ServiceLoaderCache<>(ProjectFileExtension.class).getServices();
    }

    private static EventsBus getDefaultEventsBus() {
        return new InMemoryEventsBus();
    }

    private static List<ServiceExtension> getDefaultServiceExtensions() {
        return new ServiceLoaderCache<>(ServiceExtension.class).getServices();
    }

    private synchronized void loadFileSystems() {
        if (fileSystems == null) {
            fileSystems = new HashMap<>();
            AppFileSystemProviderContext context = new AppFileSystemProviderContext(shortTimeExecutionComputationManager, tokenProvider.getToken(), eventsBus);
            for (AppFileSystemProvider provider : fileSystemProviders) {
                for (AppFileSystem fileSystem : provider.getFileSystems(context)) {
                    fileSystem.setData(this);
                    fileSystems.put(fileSystem.getName(), fileSystem);
                }
            }
        }
    }

    private synchronized void loadServices() {
        if (services == null) {
            services = new HashMap<>();
            ServiceCreationContext context = new ServiceCreationContext(tokenProvider.getToken());
            for (ServiceExtension extension : serviceExtensions) {
                services.put(extension.getServiceKey(), extension.createService(context));
            }
        }
    }

    public void addFileSystem(AppFileSystem fileSystem) {
        Objects.requireNonNull(fileSystem);
        loadFileSystems();
        if (fileSystems.containsKey(fileSystem.getName())) {
            throw new AfsException("A file system with the same name '" + fileSystem.getName() + "' already exists");
        }
        fileSystem.setData(this);
        fileSystems.put(fileSystem.getName(), fileSystem);
    }

    /**
     * The list of available file systems.
     */
    public Collection<AppFileSystem> getFileSystems() {
        loadFileSystems();
        return fileSystems.values();
    }

    /**
     * Gets a file system by its {@code name}.
     */
    public AppFileSystem getFileSystem(String name) {
        Objects.requireNonNull(name);
        loadFileSystems();
        return fileSystems.get(name);
    }

    private void closeFileSystems() {
        if (fileSystems != null) {
            fileSystems.values().forEach(AppFileSystem::close);
        }
    }

    public void setTokenProvider(SecurityTokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        // clean loaded file systems and services
        closeFileSystems();
        fileSystems = null;
        services = null;
    }

    private static String[] more(String[] path) {
        return path.length > 2 ? Arrays.copyOfRange(path, 2, path.length - 1) : new String[] {};
    }

    public Optional<Node> getNode(String pathStr) {
        Objects.requireNonNull(pathStr);
        loadFileSystems();
        String[] path = pathStr.split(AppFileSystem.FS_SEPARATOR + AppFileSystem.PATH_SEPARATOR);
        if (path.length == 0) { // wrong file system name
            return Optional.empty();
        }
        String fileSystemName = path[0];
        AppFileSystem fileSystem = fileSystems.get(fileSystemName);
        if (fileSystem == null) {
            return Optional.empty();
        }
        return path.length == 1 ? Optional.of(fileSystem.getRootFolder())
                                : fileSystem.getRootFolder().getChild(path[1], more(path));
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

    public ComputationManager getShortTimeExecutionComputationManager() {
        return shortTimeExecutionComputationManager;
    }

    public ComputationManager getLongTimeExecutionComputationManager() {
        return longTimeExecutionComputationManager != null ? longTimeExecutionComputationManager : shortTimeExecutionComputationManager;
    }

    /**
     * Gets the list of remotely accessible file systems. Should not be used by the AFS API users.
     */
    public List<String> getRemotelyAccessibleFileSystemNames() {
        loadFileSystems();
        return fileSystems.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(AppFileSystem::isRemotelyAccessible)
                .map(AppFileSystem::getName)
                .collect(Collectors.toList());
    }

    /**
     * Gets low level storage interface for remotely accessible file systems. Should not be used by the AFS API users.
     */
    public AppStorage getRemotelyAccessibleStorage(String fileSystemName) {
        Objects.requireNonNull(fileSystemName);
        loadFileSystems();
        AppFileSystem afs = fileSystems.get(fileSystemName);
        return afs != null ? afs.getStorage() : null;
    }

    /**
     * Gets a registered service of type U. To register a service, see {@link ServiceExtension}.
     *
     * @param serviceClass  the requested service type
     * @param remoteStorage if {@code true}, tries to get a remote implementation first.
     * @throws AfsException if no service implementation is found.
     */
    <U> U findService(Class<U> serviceClass, boolean remoteStorage) {
        loadServices();
        U service = null;
        if (remoteStorage) {
            service = (U) services.get(new ServiceExtension.ServiceKey<>(serviceClass, true));
        }
        if (service == null) {
            service = (U) services.get(new ServiceExtension.ServiceKey<>(serviceClass, false));
        }
        if (service == null) {
            throw new AfsException("No service found for class " + serviceClass);
        }
        return service;
    }

    /**
     * get the appData event Store instance.
     */
    public EventsBus getEventsBus() {
        return eventsBus;
    }

    /**
     * Closes any resources used by underlying file systems.
     */
    @Override
    public void close() {
        closeFileSystems();
    }
}
