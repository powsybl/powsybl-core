/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.afs.storage.events.AppStorageListener;
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

    private final WeakListenerList<ProjectFileListener> listeners = new WeakListenerList<>();

    private final AppStorageListener l = eventList -> {
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
    };

    protected ProjectFile(ProjectFileCreationContext context, int codeVersion) {
        super(context, codeVersion, true);
        storage.addListener(l);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public List<ProjectDependency<ProjectNode>> getDependencies() {
        return storage.getDependencies(info.getId())
                .stream()
                .map(dependency -> new ProjectDependency<>(dependency.getName(), project.createProjectNode(dependency.getNodeInfo())))
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
                .map(project::createProjectNode)
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
        listeners.add(listener);
    }

    public void removeListener(ProjectFileListener listener) {
        listeners.remove(listener);
    }

    public UUID startTask() {
        return project.getFileSystem().getTaskMonitor().startTask(this).getId();
    }

    public AppLogger createLogger(UUID taskId) {
        return new TaskMonitorLogger(project.getFileSystem().getTaskMonitor(), taskId);
    }

    public void stopTask(UUID id) {
        project.getFileSystem().getTaskMonitor().stopTask(id);
    }

    public <U> U findService(Class<U> serviceClass) {
        return project.getFileSystem().getData().findService(serviceClass, storage.isRemote());
    }
}
