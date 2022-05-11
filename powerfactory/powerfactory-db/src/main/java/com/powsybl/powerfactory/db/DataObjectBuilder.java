/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.db;

import com.powsybl.powerfactory.model.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataObjectBuilder {

    private final DataObjectIndex index = new DataObjectIndex();

    private final DataScheme scheme = new DataScheme();

    public DataObjectIndex getIndex() {
        return index;
    }

    public void createClass(String name) {
        Objects.requireNonNull(name);
        if (scheme.classExists(name)) {
            return;
        }
        scheme.addClass(new DataClass(name));
    }

    public void createAttribute(String className, String attributeName, int type, String description) {
        Objects.requireNonNull(className);
        Objects.requireNonNull(attributeName);
        DataClass dataClass = scheme.getClassByName(className);
        if (dataClass.getAttributeByName(attributeName) != null) {
            return;
        }
        dataClass.addAttribute(new DataAttribute(attributeName, DataAttributeType.values()[type], description));
    }

    public void createObject(long id, String className) {
        DataClass dataClass = scheme.getClassByName(className);
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

    public void setDoubleVectorAttributeValue(long objectId, String attributeName, List<Double> value) {
        DataObject object = getObjectById(objectId);
        object.setDoubleVectorAttributeValue(attributeName, value);
    }

    public void setStringVectorAttributeValue(long objectId, String attributeName, List<String> value) {
        DataObject object = getObjectById(objectId);
        object.setStringVectorAttributeValue(attributeName, value);
    }
}
