/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectIndex {

    private final Map<Long, DataObject> objectsById = new HashMap<>();

    private final Map<String, List<DataObject>> objectsByClass = new HashMap<>();

    public void addDataObject(DataObject obj) {
        Objects.requireNonNull(obj);
        if (objectsById.containsKey(obj.getId())) {
            throw new PowerFactoryException("Object '" + obj.getId() + "' already exists");
        }
        objectsById.put(obj.getId(), obj);
        objectsByClass.computeIfAbsent(obj.getDataClassName(), k -> new ArrayList<>())
                .add(obj);
    }

    public Collection<DataObject> getDataObjects() {
        return objectsById.values();
    }

    public List<DataObject> getRootDataObjects() {
        return objectsById.values().stream().filter(obj -> obj.getParent() == null).collect(Collectors.toList());
    }

    public Optional<DataObject> getDataObjectById(long id) {
        return Optional.ofNullable(objectsById.get(id));
    }

    public List<DataObject> getDataObjectsByClass(String className) {
        Objects.requireNonNull(className);
        return objectsByClass.getOrDefault(className, Collections.emptyList());
    }
}
