/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

import com.powsybl.powerfactory.model.DataAttributeType;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DgsAttribute {

    private static final Pattern ATTR_DESCR_PATTERN = Pattern.compile("(.+)\\(([airp]+)(:\\d*)?\\)");

    private final String name;
    private final DataAttributeType type;

    DgsAttribute(String name, DataAttributeType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public DataAttributeType getType() {
        return type;
    }

    private static DataAttributeType getDataAttributeType(char attributeType) {
        DataAttributeType type;
        switch (attributeType) {
            case 'a':
                type = DataAttributeType.STRING;
                break;
            case 'i':
                type = DataAttributeType.INTEGER;
                break;
            case 'r':
                type = DataAttributeType.FLOAT;
                break;
            case 'p':
                type = DataAttributeType.OBJECT;
                break;
            default:
                throw new AssertionError("Unexpected attribute type: " + attributeType);
        }
        return type;
    }

    static DgsAttribute parse(String field) {
        Matcher matcher = ATTR_DESCR_PATTERN.matcher(field);
        if (!matcher.matches()) {
            throw new PowerFactoryException("Invalid attribute description: '" + field + "'");
        }

        return new DgsAttribute(matcher.group(1), getDataAttributeType(matcher.group(2).charAt(0)));
    }
}
