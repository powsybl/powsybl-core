/**
 * Copyright (c) 2020, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.Sets;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

final class Property {

    private static final Logger LOGGER = LoggerFactory.getLogger(Property.class);

    private final PropertyType type;
    private final Object value;

    public Property(int value) {
        this.type = PropertyType.INTEGER;
        this.value = value;
    }

    public Property(String value) {
        this.type = PropertyType.STRING;
        this.value = value;
    }

    public Property(Double value) {
        this.type = PropertyType.DOUBLE;
        this.value = value;
    }

    public Property(Boolean value) {
        this.type = PropertyType.BOOLEAN;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public int intValue() {
        if (type == PropertyType.INTEGER) {
            return (int) value;
        }
        throw new PowsyblException("The property is not an integer");
    }

    public String stringValue() {
        if (type == PropertyType.STRING) {
            return (String) value;
        }
        throw new PowsyblException("The property is not a string");
    }

    public double doubleValue() {
        if (type == PropertyType.DOUBLE) {
            return (double) value;
        }
        throw new PowsyblException("The property is not a double");
    }

    public Boolean booleanValue() {
        if (type == PropertyType.BOOLEAN) {
            return (Boolean) value;
        }
        throw new PowsyblException("The property is not a boolean");
    }

    public PropertyType propertyType() {
        return type;
    }

    public static void mergeProperties(DanglingLine dl1, DanglingLine dl2, Map<String, Property> properties) {
        Set<String> properties1 = dl1.getPropertyNames();
        Set<String> properties2 = dl2.getPropertyNames();
        Set<String> commonProperties = Sets.intersection(properties1, properties2);
        Sets.difference(properties1, commonProperties).forEach(prop -> putMergedProperty(prop, dl1, properties));
        Sets.difference(properties2, commonProperties).forEach(prop -> putMergedProperty(prop, dl2, properties));
        commonProperties.forEach(prop -> mergeProperty(prop, dl1, dl2, properties));
        properties1.forEach(prop -> putMergedProperty(prop, prop + "_1", dl1, properties));
        properties2.forEach(prop -> putMergedProperty(prop, prop + "_2", dl2, properties));
    }

    private static void mergeProperty(String key, DanglingLine dl1, DanglingLine dl2, Map<String, Property> properties) {
        String error = "Inconsistencies of property for '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line";
        if (dl1.getPropertyType(key) != dl2.getPropertyType(key)) {
            LOGGER.error("Inconsistencies of property type for '{}' between both sides of merged line. '{}' on side 1 and '{}' on side 2. Removing the property of merged line",
                key, dl1.getPropertyType(key), dl2.getPropertyType(key));
            return;
        }
        switch (dl1.getPropertyType(key)) {
            case STRING:
                String str1 = dl1.getStringProperty(key);
                String str2 = dl2.getStringProperty(key);
                if (str1.equals("")) {
                    putMergedProperty(key, dl2, properties);
                } else if (str2.equals("") || str1.equals(str2)) {
                    putMergedProperty(key, dl1, properties);
                } else {
                    LOGGER.error(error, key, str1, str2);
                }
                break;
            case INTEGER:
                int int1 = dl1.getIntegerProperty(key);
                int int2 = dl2.getIntegerProperty(key);
                if (int1 == int2) {
                    putMergedProperty(key, dl2, properties);
                } else {
                    LOGGER.error(error, key, int1, int2);
                }
                break;
            case DOUBLE:
                double dbl1 = dl1.getDoubleProperty(key);
                double dbl2 = dl2.getDoubleProperty(key);
                if (Double.isNaN(dbl1)) {
                    putMergedProperty(key, dl2, properties);
                } else if (Double.isNaN(dbl2) || dbl1 == dbl2) {
                    putMergedProperty(key, dl1, properties);
                } else {
                    LOGGER.error(error, key, dbl1, dbl2);
                }
                break;
            case BOOLEAN:
                boolean bool1 = dl1.getBooleanProperty(key);
                boolean bool2 = dl2.getBooleanProperty(key);
                if (bool1 == bool2) {
                    putMergedProperty(key, dl1, properties);
                } else {
                    LOGGER.error(error, key, bool1, bool2);
                }
                break;
        }
    }

    private static void putMergedProperty(String key, String newKey, DanglingLine dl, Map<String, Property> properties) {
        switch (dl.getPropertyType(key)) {
            case STRING:
                properties.put(newKey, new Property(dl.getStringProperty(key)));
                break;
            case INTEGER:
                properties.put(newKey, new Property(dl.getIntegerProperty(key)));
                break;
            case DOUBLE:
                properties.put(newKey, new Property(dl.getDoubleProperty(key)));
                break;
            case BOOLEAN:
                properties.put(newKey, new Property(dl.getBooleanProperty(key)));
                break;
        }
    }

    private static void putMergedProperty(String key, DanglingLine dl, Map<String, Property> properties) {
        putMergedProperty(key, key, dl, properties);
    }

}
