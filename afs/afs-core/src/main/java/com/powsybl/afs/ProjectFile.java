/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.events.DependencyEvent;
import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.commons.util.WeakListenerList;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFile extends ProjectNode {

    protected final FileIcon icon;

    private final WeakListenerList<ProjectFileListener> listeners = new WeakListenerList<>();

    protected ProjectFile(ProjectFileCreationContext context, int codeVersion, FileIcon icon) {
        super(context, codeVersion, true);
        this.icon = Objects.requireNonNull(icon);
        storage.addListener(this, eventList -> {
            for (NodeEvent event : eventList.getEvents()) {
                if (event.getId().equals(getId())) {
                    switch (event.getType()) {
                        case DEPENDENCY_ADDED:
                        case DEPENDENCY_REMOVED:
                            listeners.notify(listener -> listener.dependencyChanged(((DependencyEvent) event).getDependencyName()));
                            break;

                        case BACKWARD_DEPENDENCY_ADDED:
                        case BACKWARD_DEPENDENCY_REMOVED:
                            listeners.notify(listener -> listener.backwardDependencyChanged(((DependencyEvent) event).getDependencyName()));
                            break;

                        default:
                            break;
                    }
                }
            }
        });
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public FileIcon getIcon() {
        return icon;
    }

    public List<ProjectDependency<ProjectNode>> getDependencies() {
        return storage.getDependencies(info.getId())
                .stream()
                .map(dependency -> new ProjectDependency<>(dependency.getName(), fileSystem.createProjectNode(dependency.getNodeInfo())))
                .collect(Collectors.toList());
    }

    public void setDependencies(String name, List<ProjectNode> projectNodes) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(projectNodes);
        for (NodeInfo toNodeInfo : storage.getDependencies(info.getId(), name)) {
            storage.removeDependency(info.getId(), name, toNodeInfo.getId());
        }
        for (ProjectNode projectNode : projectNodes) {
            storage.addDependency(info.getId(), name, projectNode.getId());
        }
        storage.flush();
    }

    public <T> List<T> getDependencies(String name, Class<T> nodeClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodeClass);
        return storage.getDependencies(info.getId(), name).stream()
                .map(fileSystem::createProjectNode)
                .filter(dependencyNode -> nodeClass.isAssignableFrom(dependencyNode.getClass()))
                .map(nodeClass::cast)
                .collect(Collectors.toList());
    }

    public void removeDependencies(String name) {
        Objects.requireNonNull(name);
        for (NodeInfo toNodeInfo : storage.getDependencies(info.getId(), name)) {
            storage.removeDependency(info.getId(), name, toNodeInfo.getId());
        }
        storage.flush();
    }

    public void addListener(ProjectFileListener listener) {
        listeners.add(this, listener);
    }

    public void removeListener(ProjectFileListener listener) {
        listeners.remove(this, listener);
    }

    public UUID startTask() {
        return fileSystem.getTaskMonitor().startTask(this).getId();
    }

    public AppLogger createLogger(UUID taskId) {
        return new TaskMonitorLogger(fileSystem.getTaskMonitor(), taskId);
    }

    public void stopTask(UUID id) {
        fileSystem.getTaskMonitor().stopTask(id);
    }

    public <U> U findService(Class<U> serviceClass) {
        return fileSystem.getData().findService(serviceClass, storage.isRemote());
    }
}
