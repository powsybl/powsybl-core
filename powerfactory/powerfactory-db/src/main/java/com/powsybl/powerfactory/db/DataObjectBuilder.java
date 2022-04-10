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

    public void createClass(String name) {
        if (classesByName.containsKey(name)) {
            return;
        }
        DataClass dataClass = new DataClass(name);
        classesByName.put(dataClass.getName(), dataClass);
    }

    public void createAttribute(String className, String attributeName, int type, String description) {
        DataClass dataClass = getClassByName(className);
        if (dataClass.getAttributeByName(attributeName) != null) {
            return;
        }
        dataClass.addAttribute(new DataAttribute(attributeName, DataAttributeType.values()[type], description));
    }

    private DataClass getClassByName(String className) {
        DataClass dataClass = classesByName.get(className);
        if (dataClass == null) {
            throw new PowerFactoryException("Class '" + className + "' not found");
        }
        return dataClass;
    }

    public void createObject(long id, String className) {
        DataClass dataClass = getClassByName(className);
        new DataObject(id, dataClass, index);
    }

    public void setObjectParent(long id, long parentId) {
        DataObject object = getObjectById(id);
        DataObject parentObject = getObjectById(parentId);
        object.setParent(parentObject);
    }

    private DataObject getObjectById(long id) {
        return index.getDataObjectById(id)
                .orElseThrow(() -> new PowerFactoryException("Object '" + id + "' not found"));
    }

    public void setIntAttributeValue(long objectId, String attributeName, int value) {
        DataObject object = getObjectById(objectId);
        object.setIntAttributeValue(attributeName, value);
    }

    public void setLongAttributeValue(long objectId, String attributeName, long value) {
        DataObject object = getObjectById(objectId);
        object.setLongAttributeValue(attributeName, value);
    }

    public void setDoubleAttributeValue(long objectId, String attributeName, double value) {
        DataObject object = getObjectById(objectId);
        object.setDoubleAttributeValue(attributeName, value);
    }

    public void setStringAttributeValue(long objectId, String attributeName, String value) {
        DataObject object = getObjectById(objectId);
        object.setStringAttributeValue(attributeName, value);
    }

    public void setObjectAttributeValue(long objectId, String attributeName, long otherObjectId) {
        DataObject object = getObjectById(objectId);
        object.setObjectAttributeValue(attributeName, otherObjectId);
    }
}
