/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.parameters;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Parameter {

    private final String name;

    private final ParameterType type;

    private final String description;

    private final Object defaultValue;

    public Parameter(String name, ParameterType type, String description, Object defaultValue) {
        if (!type.getClazz().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException("Bad default value type " + defaultValue.getClass() + ", " + type.getClazz() + " was expected");
        }
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
        this.defaultValue = Objects.requireNonNull(defaultValue);
    }

    public String getName() {
        return name;
    }

    public ParameterType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
