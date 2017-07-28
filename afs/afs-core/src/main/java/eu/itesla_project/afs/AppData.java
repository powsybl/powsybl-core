/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs;

import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.util.ServiceLoaderCache;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppData implements AutoCloseable {

    private final ComputationManager computationManager;

    private final ComponentDefaultConfig componentDefaultConfig;

    private final Map<String, AppFileSystem> fileSystems = new HashMap<>();

    private final Map<Class<? extends File>, FileExtension> fileExtensions = new HashMap<>();

    private final Map<String, FileExtension> fileExtensionsByPseudoClass = new HashMap<>();

    private final Map<Class<?>, ProjectFileExtension> projectFileExtensions = new HashMap<>();

    private final Map<String, ProjectFileExtension> projectFileExtensionsByPseudoClass = new HashMap<>();

    private final Set<Class<? extends ProjectFile>> projectFileClasses = new HashSet<>();

    public AppData() {
        this(LocalComputationManager.getDefault(),
                ComponentDefaultConfig.load(),
                new ServiceLoaderCache<>(AppFileSystemProvider.class).getServices(),
                new ServiceLoaderCache<>(FileExtension.class).getServices(),
                new ServiceLoaderCache<>(ProjectFileExtension.class).getServices());
    }

    public AppData(ComputationManager computationManager, ComponentDefaultConfig componentDefaultConfig, List<AppFileSystemProvider> fileSystemProviders,
                   List<FileExtension> fileExtensions, List<ProjectFileExtension> projectFileExtensions) {
        this.computationManager = Objects.requireNonNull(computationManager);
        this.componentDefaultConfig = Objects.requireNonNull(componentDefaultConfig);
        Objects.requireNonNull(fileSystemProviders);
        Objects.requireNonNull(fileExtensions);
        Objects.requireNonNull(projectFileExtensions);
        for (AppFileSystemProvider provider : fileSystemProviders) {
            for (AppFileSystem fileSystem : provider.getFileSystems(computationManager)) {
                if (fileSystems.containsKey(fileSystem.getName())) {
                    throw new AfsException("A file system with the same name '" + fileSystem.getName() + "' already exists");
                }
                fileSystem.setData(this);
                fileSystems.put(fileSystem.getName(), fileSystem);
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
        FileExtension extension = fileExtensionsByPseudoClass.get(filePseudoClass);
        if (extension == null) {
            throw new AfsException("No extension found for file pseudo class '" + filePseudoClass + "'");
        }
        return extension;
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
        ProjectFileExtension extension = projectFileExtensionsByPseudoClass.get(projectFilePseudoClass);
        if (extension == null) {
            throw new AfsException("No extension found for project file or project file builder pseudo class '"
                    + projectFilePseudoClass + "'");
        }
        return extension;
    }

    public ComponentDefaultConfig getComponentDefaultConfig() {
        return componentDefaultConfig;
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

    public AppFileSystemStorage getRemotelyAccessibleStorage(String fileSystemName) {
        AppFileSystem afs = fileSystems.get(fileSystemName);
        return afs != null ? afs.getStorage() : null;
    }

    @Override
    public void close() throws Exception {
        getFileSystems().forEach(AppFileSystem::close);
    }
}
