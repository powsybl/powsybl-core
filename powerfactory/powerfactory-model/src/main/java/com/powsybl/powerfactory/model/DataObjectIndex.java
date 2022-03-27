/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectIndex {

    private final Map<Long, DataObject> objectsById = new HashMap<>();

    public void addDataObject(DataObject obj) {
        Objects.requireNonNull(obj);
        if (objectsById.containsKey(obj.getId())) {
            throw new PowerFactoryException("Object '" + obj.getId() + "' already exists");
        }
        objectsById.put(obj.getId(), obj);
    }

    public Collection<DataObject> getDataObjects() {
        return objectsById.values();
    }

    public Optional<DataObject> getDataObject(long id) {
        return Optional.ofNullable(objectsById.get(id));
    }
}
