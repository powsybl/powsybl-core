/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.parameters;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public enum ParameterType {
    BOOLEAN(Boolean.class, Boolean.class),
    STRING(String.class, String.class),
    STRING_LIST(List.class, String.class),
    DOUBLE(Double.class, Double.class),
    INTEGER(Integer.class, Integer.class);

    private final Class<?> typeClass;
    private final Class<?> elementClass;

    ParameterType(Class<?> typeClass, Class<?> elementClass) {
        this.typeClass = typeClass;
        this.elementClass = elementClass;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public Class<?> getElementClass() {
        return elementClass;
    }
}
