/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.parameters;

import com.powsybl.commons.PowsyblException;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Parameter {

    private final List<String> names = new ArrayList<>();

    private final ParameterType type;

    private final String description;

    private final Object defaultValue;

    public Parameter(String name, ParameterType type, String description, Object defaultValue) {
        if (defaultValue != null && !type.getClazz().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException("Bad default value type " + defaultValue.getClass() + ", " + type.getClazz() + " was expected");
        }
        names.add(Objects.requireNonNull(name));
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
        this.defaultValue = checkDefaultValue(type, defaultValue);
    }

    private static Object checkDefaultValue(ParameterType type, Object defaultValue) {
        if (type == ParameterType.BOOLEAN && defaultValue == null) {
            throw new PowsyblException("With Boolean parameter you are not allowed to pass a null default value");
        }
        if (type == ParameterType.DOUBLE && defaultValue == null) {
            throw new PowsyblException("With Double parameter you are not allowed to pass a null default value");
        }
        return defaultValue;
    }

    public Parameter addAdditionalNames(String... names) {
        Objects.requireNonNull(names);
        this.names.addAll(Arrays.asList(names));
        return this;
    }

    public String getName() {
        return names.get(0);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
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
