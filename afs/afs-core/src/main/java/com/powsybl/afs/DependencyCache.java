/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Allows to request and cache dependencies of a project file to one or several other project nodes.
 * The request is defined by a dependency name, and by the type of the "target" nodes.
 * <p>
 * The objects are cached when retrieved, and will not be fetched again until invalidation of the cache.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DependencyCache<T extends ProjectNode> {

    private final ProjectFile projectFile;

    private final String dependencyName;

    private final Class<T> dependencyClass;

    private final ProjectFileListener l = new DefaultProjectFileListener() {
        @Override
        public void dependencyChanged(String name) {
            if (dependencyName.equals(name)) {
                invalidate();
            }
        }
    };

    private List<T> cache;

    private boolean cached = false;

    private final Lock lock = new ReentrantLock();

    public DependencyCache(ProjectFile projectFile, String dependencyName, Class<T> dependencyClass) {
        this.projectFile = Objects.requireNonNull(projectFile);
        this.dependencyName = Objects.requireNonNull(dependencyName);
        this.dependencyClass = Objects.requireNonNull(dependencyClass);
        projectFile.addListener(l);
    }

    /**
     * Invalidates the cache: dependencies will be fetched again on next request.
     */
    public void invalidate() {
        lock.lock();
        try {
            if (cached) {
                cache = null;
                cached = false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the first dependency of the project file which matches the specified dependency name and class.
     * Result is cached, and will not be fetched again until the cache is invalidated.
     */
    public Optional<T> getFirst() {
        List<T> all = getAll();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
    }

    /**
     * Gets all the dependencies of the project file which match the specified dependency name and class.
     * Result is cached, and will not be fetched again until the cache is invalidated.
     */
    public List<T> getAll() {
        lock.lock();
        try {
            if (!cached) {
                cache = projectFile.getDependencies(dependencyName, dependencyClass);
                cached = true;
            }
            return cache;
        } finally {
            lock.unlock();
        }
    }
}
