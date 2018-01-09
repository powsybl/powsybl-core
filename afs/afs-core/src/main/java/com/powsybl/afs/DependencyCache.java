/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DependencyCache<T> {

    private final ProjectFile projectFile;

    private final String dependencyName;

    private final Class<T> dependencyClass;

    private T cache;

    private boolean cached = false;

    private final Lock lock = new ReentrantLock();

    public DependencyCache(ProjectFile projectFile, String dependencyName, Class<T> dependencyClass) {
        this.projectFile = Objects.requireNonNull(projectFile);
        this.dependencyName = Objects.requireNonNull(dependencyName);
        this.dependencyClass = Objects.requireNonNull(dependencyClass);
        projectFile.addDependencyListener(this, () -> {
            lock.lock();
            try {
                cache = null;
                cached = false;
            } finally {
                lock.unlock();
            }
        });
    }

    public Optional<T> get() {
        lock.lock();
        try {
            if (!cached) {
                cache = projectFile.getDependency(dependencyName, dependencyClass).orElse(null);
                cached = true;
            }
            return Optional.ofNullable(cache);
        } finally {
            lock.unlock();
        }
    }
}
