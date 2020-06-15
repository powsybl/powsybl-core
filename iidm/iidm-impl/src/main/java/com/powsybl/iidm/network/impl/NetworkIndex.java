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

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class NetworkIndex {

    private final Map<String, Identifiable<?>> objectsById = new HashMap<>();
    private final Map<String, String> idByAlias = new HashMap<>();

    private final Map<Class<? extends Identifiable>, Set<Identifiable<?>>> objectsByClass = new HashMap<>();

    static void checkId(String id) {
        if (id == null || id.isEmpty()) {
            throw new PowsyblException("Invalid id '" + id + "'");
        }
    }

    static String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    void checkAndAdd(Identifiable<?> obj) {
        checkId(obj.getId());
        if (objectsById.containsKey(obj.getId())) {
            throw new PowsyblException("Object (" + obj.getClass().getName()
                    + ") '" + obj.getId() + "' already exists");
        }
        objectsById.put(obj.getId(), obj);
        obj.getAliases().forEach(alias -> addAlias(obj, alias));

        Set<Identifiable<?>> all = objectsByClass.get(obj.getClass());
        if (all == null) {
            all = new LinkedHashSet<>();
            objectsByClass.put(obj.getClass(), all);
        }
        all.add(obj);
    }

    boolean addAlias(Identifiable<?> obj, String alias) {
        if (objectsById.containsKey(alias)) {
            if (objectsById.get(alias).equals(obj)) {
                // Silently ignore affecting the objects id to its own aliases
                return false;
            }
            Identifiable<?> aliasConflict = objectsById.get(alias);
            String message = String.format("Object (%s) with alias '%s' cannot be created because alias already refers to object (%s) with ID '%s'",
                    obj.getClass(),
                    alias,
                    aliasConflict.getClass(),
                    aliasConflict.getId());
            throw new PowsyblException(message);
        } else if (idByAlias.containsKey(alias)) {
            if (objectsById.get(idByAlias.get(alias)).equals(obj)) {
                // Silently ignore affecting the same alias twice to an object
                return false;
            }
            Identifiable<?> aliasConflict = objectsById.get(idByAlias.get(alias));
            String message = String.format("Object (%s) with alias '%s' cannot be created because alias already refers to object (%s) with ID '%s'",
                    obj.getClass(),
                    alias,
                    aliasConflict.getClass(),
                    aliasConflict.getId());
            throw new PowsyblException(message);
        }
        idByAlias.put(alias, obj.getId());
        return true;
    }

    public <I extends Identifiable<I>> void removeAlias(Identifiable<?> obj, String alias) {
        if (!idByAlias.containsKey(alias)) {
            throw new PowsyblException(String.format("No alias '%s' found in the network", alias));
        } else if (!idByAlias.get(alias).equals(obj.getId())) {
            throw new PowsyblException(String.format("Alias '%s' do not correspond to object '%s'", alias, obj.getId()));
        } else {
            idByAlias.remove(alias);
        }
    }

    Identifiable get(String id) {
        return get(id, false);
    }

    Identifiable get(String id, boolean searchInAlias) {
        String idOrAlias = searchInAlias ? idByAlias.getOrDefault(id, id) : id;
        checkId(idOrAlias);
        return objectsById.get(idOrAlias);
    }

    <T extends Identifiable> T get(String id, Class<T> clazz) {
        return get(id, clazz, true);
    }

    <T extends Identifiable> T get(String id, Class<T> clazz, boolean searchInAlias) {
        Identifiable obj = get(id, searchInAlias);
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
        String idFromPotentialAlias = idByAlias.getOrDefault(id, id);
        checkId(idFromPotentialAlias);
        return objectsById.containsKey(idFromPotentialAlias);
    }

    void remove(Identifiable obj) {
        checkId(obj.getId());
        Identifiable old = objectsById.remove(obj.getId());
        if (old == null || old != obj) {
            throw new PowsyblException("Object (" + obj.getClass().getName()
                    + ") '" + obj.getId() + "' not found");
        }
        obj.getAliases().forEach(idByAlias::remove);
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
     * Compute intersection between this index and another one.
     * @param other the other index
     * @return list of objects id that exist in both indexes organized by class.
     */
    Multimap<Class<? extends Identifiable>, String> intersection(NetworkIndex other) {
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
     * Merge an other index into this one. At the end of the call the
     * other index is empty.
     * @param other the index to merge
     */
    void merge(NetworkIndex other) {
        for (Identifiable obj : other.objectsById.values()) {
            checkAndAdd(obj);
        }
        other.clean();
    }

    void printForDebug(PrintStream out) {
        for (Map.Entry<String, Identifiable<?>> entry : objectsById.entrySet()) {
            out.println(entry.getKey() + " " + System.identityHashCode(entry.getValue()));
        }
        for (Map.Entry<Class<? extends Identifiable>, Set<Identifiable<?>>> entry : objectsByClass.entrySet()) {
            out.println(entry.getKey() + " " + entry.getValue().stream().map(System::identityHashCode).collect(Collectors.toList()));
        }
    }
}
