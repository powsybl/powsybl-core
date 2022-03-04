/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataAttribute {

    public static final String LOC_NAME = "loc_name";
    public static final String FOLD_ID = "fold_id";
    public static final String FOR_NAME = "for_name";

    private final String name;

    private final DataAttributeType type;

    private final String description;

    public DataAttribute(String name, DataAttributeType type) {
        this(name, type, "");
    }

    public DataAttribute(String name, DataAttributeType type, String description) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.description = Objects.requireNonNull(description);
    }

    public String getName() {
        return name;
    }

    public DataAttributeType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DataAttribute(name=" + name + ", type=" + type + ", description=" + description + ")";
    }
}
