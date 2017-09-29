/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ObjectStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStore.class);

    private final Map<String, Identifiable<?>> objectsById = new HashMap<>();

    private final Map<Class<? extends Identifiable>, Set<Identifiable<?>>> objectsByClass = new HashMap<>();

    static void checkId(String id) {
        if (id == null || id.isEmpty()) {
            throw new PowsyblException("Invalid id '" + id + "'");
        }
    }

    static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    String getUniqueId(String baseId) {
        String checkedBaseId;
        if (baseId != null && baseId.length() > 0) {
            if (!objectsById.containsKey(baseId)) {
                return baseId;
            }
            checkedBaseId = baseId;
        } else {
            checkedBaseId = "autoid";
        }
        String uniqueId;
        int i = 0;
        do {
            uniqueId = checkedBaseId + '#' + Integer.toString(i++);
        } while (i < Integer.MAX_VALUE && objectsById.containsKey(uniqueId));
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Object '{}' is not unique, rename to '{}'", baseId, uniqueId);
        }
        return uniqueId;
    }

    void checkAndAdd(Identifiable<?> obj) {
        checkId(obj.getId());
        if (objectsById.containsKey(obj.getId())) {
            throw new PowsyblException("Object (" + obj.getClass().getName()
                    + ") '" + obj.getId() + "' already exists");
        }
        objectsById.put(obj.getId(), obj);
        Set<Identifiable<?>> all = objectsByClass.get(obj.getClass());
        if (all == null) {
            all = new LinkedHashSet<>();
            objectsByClass.put(obj.getClass(), all);
        }
        all.add(obj);
    }

    Identifiable get(String id) {
        checkId(id);
        return objectsById.get(id);
    }

    <T extends Identifiable> T get(String id, Class<T> clazz) {
        checkId(id);
        Identifiable obj = objectsById.get(id);
        if (obj != null && clazz.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        } else {
            return null;
        }
    }

    Collection<Identifiable<?>> getAll() {
        return objectsById.values();
    }

    <T extends Identifiable> Set<T> getAll(Class<T> clazz) {
        Set<Identifiable<?>> all = objectsByClass.get(clazz);
        if (all == null) {
            return Collections.emptySet();
        }
        return (Set<T>) all;
    }

    boolean contains(String id) {
        checkId(id);
        return objectsById.containsKey(id);
    }

    void remove(Identifiable obj) {
        checkId(obj.getId());
        Identifiable old = objectsById.remove(obj.getId());
        if (old == null || old != obj) {
            throw new PowsyblException("Object (" + obj.getClass().getName()
                    + ") '" + obj.getId() + "' not found");
        }
        Set<Identifiable<?>> all = objectsByClass.get(obj.getClass());
        if (all != null) {
            all.remove(obj);
        }
    }

    void clean() {
        objectsById.clear();
        objectsByClass.clear();
    }

    /**
     * Compute intersection between this object store and another one.
     * @param other the other object store
     * @return list of objects id that exist in both object store organized by class.
     */
    Multimap<Class<? extends Identifiable>, String> intersection(ObjectStore other) {
        Multimap<Class<? extends Identifiable>, String> intersection = HashMultimap.create();
        for (Map.Entry<Class<? extends Identifiable>, Set<Identifiable<?>>> entry : other.objectsByClass.entrySet()) {
            Class<? extends Identifiable> clazz = entry.getKey();
            Set<Identifiable<?>> objects = entry.getValue();
            for (Identifiable obj : objects) {
                if (objectsById.containsKey(obj.getId())) {
                    intersection.put(clazz, obj.getId());
                }
            }
        }
        return intersection;
    }

    /**
     * Merge an other object store into this one. At the end of the call the
     * other object store is empty.
     * @param other the object store to merge
     */
    void merge(ObjectStore other) {
        for (Identifiable obj : other.objectsById.values()) {
            checkAndAdd(obj);
        }
        other.clean();
    }

    void printForDebug() {
        for (Map.Entry<String, Identifiable<?>> entry : objectsById.entrySet()) {
            System.out.println(entry.getKey() + " " + System.identityHashCode(entry.getValue()));
        }
        for (Map.Entry<Class<? extends Identifiable>, Set<Identifiable<?>>> entry : objectsByClass.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue().stream().map(System::identityHashCode).collect(Collectors.toList()));
        }
    }

}
