/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.powsybl.powerfactory.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectBuilder {

    private final DataObjectIndex index = new DataObjectIndex();

    private final Map<String, DataClass> classesByName = new HashMap<>();

    public DataObjectIndex getIndex() {
        return index;
    }

    public boolean createClass(String name) {
        if (classesByName.containsKey(name)) {
            return false;
        }
        DataClass dataClass = new DataClass(name);
        classesByName.put(dataClass.getName(), dataClass);
        return true;
    }

    public void createAttribute(String className, String attributeName, int type, String description) {
        DataClass dataClass = getClassByName(className);
        dataClass.addAttribute(new DataAttribute(attributeName, DataAttributeType.values()[type], description));
    }

    private DataClass getClassByName(String className) {
        DataClass dataClass = classesByName.get(className);
        if (dataClass == null) {
            throw new PowerFactoryException("Class '" + className + "' not found");
        }
        return dataClass;
    }

    public void createObject(long id, String className, long parentId) {
        DataClass dataClass = getClassByName(className);
        DataObject object = new DataObject(id, dataClass, index);
        if (parentId >= 0) {
            DataObject parentObject = getObjectById(parentId);
            object.setParent(parentObject);
        }
    }

    private DataObject getObjectById(long id) {
        return index.getDataObjectById(id)
                .orElseThrow(() -> new PowerFactoryException("Object '" + id + "' not found"));
    }

    public void setIntAttributeValue(long objectId, String attributeName, int value) {
        DataObject object = getObjectById(objectId);
        object.setIntAttributeValue(attributeName, value);
    }

    public void setStringAttributeValue(long objectId, String attributeName, String value) {
        DataObject object = getObjectById(objectId);
        object.setStringAttributeValue(attributeName, value);
    }
}
