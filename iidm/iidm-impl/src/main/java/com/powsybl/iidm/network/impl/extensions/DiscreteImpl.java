/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.Discrete;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class DiscreteImpl implements Discrete {

    private final String id;
    private final Discrete.Type type;
    private final Properties properties = new Properties();
    private final String valueAsString;
    private final int valueAsInt;

    DiscreteImpl(String id, Discrete.Type type, Properties properties, String valueAsString, int valueAsInt) {
        this.id = id;
        this.type = type;
        this.properties.putAll(properties);
        this.valueAsString = Objects.requireNonNullElseGet(valueAsString, () -> String.valueOf(valueAsInt));
        this.valueAsInt = valueAsInt;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Object getProperty(String name) {
        return properties.getProperty(name);
    }

    @Override
    public Discrete putProperty(String name, Object property) {
        properties.put(Objects.requireNonNull(name), property);
        return this;
    }

    @Override
    public String getValueAsString() {
        return valueAsString;
    }

    @Override
    public int getValueAsInt() {
        return valueAsInt;
    }
}
