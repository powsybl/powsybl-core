/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;
import com.powsybl.iidm.network.extensions.Analog;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class AnalogImpl implements Analog {

    private final String id;
    private final Analog.Type type;
    private final Properties properties = new Properties();
    private final double value;
    private final Analog.Side side;

    AnalogImpl(String id, Analog.Type type, Properties properties, double value, Analog.Side side) {
        this.id = id;
        this.type = type;
        this.properties.putAll(properties);
        this.value = value;
        this.side = side;
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
    public Analog putProperty(String name, Object property) {
        properties.put(Objects.requireNonNull(name), property);
        return this;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Side getSide() {
        return side;
    }
}
