/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

@JsonIgnoreProperties({"attributes", "attributeByName"})
@JsonPropertyOrder({"name"})

public class DataClass {

    private final String name;

    private final List<DataAttribute> attributes = new ArrayList<>();

    private final Map<String, DataAttribute> attributesByName = new HashMap<>();

    public DataClass(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public DataClass addAttribute(DataAttribute attribute) {
        Objects.requireNonNull(attribute);
        if (attributesByName.containsKey(attribute.getName())) {
            throw new PowerFactoryException("Class '" + name + "' already has an attribute named '" + attribute.getName() + "'");
        }
        attributes.add(attribute);
        attributesByName.put(attribute.getName(), attribute);
        return this;
    }

    public List<DataAttribute> getAttributes() {
        return attributes;
    }

    public DataAttribute getAttributeByName(String name) {
        Objects.requireNonNull(name);
        return attributesByName.get(name);
    }

    @Override
    public String toString() {
        return "DataClass(name=" + name + ")";
    }

    public static DataClass init(String name) {
        return new DataClass(name)
                .addAttribute(new DataAttribute(DataAttribute.LOC_NAME, DataAttributeType.STRING));
    }
}
