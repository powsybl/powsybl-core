/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ProjectFile extends ProjectNode {

    protected final FileIcon icon;

    private final WeakHashMap<Object, List<DependencyListener>> listeners = new WeakHashMap<>();

    protected ProjectFile(ProjectFileCreationContext context, int codeVersion, FileIcon icon) {
        super(context, codeVersion, true);
        this.icon = Objects.requireNonNull(icon);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public FileIcon getIcon() {
        return icon;
    }

    public List<ProjectNode> getDependencies() {
        return storage.getDependencies(info.getId())
                .stream()
                .map(fileSystem::findProjectNode)
                .collect(Collectors.toList());
    }

    public <T> Optional<T> getDependency(String name, Class<T> nodeClass) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(nodeClass);
        return storage.getDependency(info.getId(), name)
                .map(fileSystem::findProjectNode)
                .filter(dependencyNode -> nodeClass.isAssignableFrom(dependencyNode.getClass()))
                .map(nodeClass::cast);
    }

    public void addDependencyListener(Object source, DependencyListener listener) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(listener);
        listeners.computeIfAbsent(source, k -> new ArrayList<>()).add(listener);
    }

    protected void notifyDependencyListeners() {
        listeners.values().stream().flatMap(Collection::stream).forEach(DependencyListener::dependencyChanged);
        // propagate
        getBackwardDependencies().forEach(ProjectFile::notifyDependencyListeners);
    }
}
