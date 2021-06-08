/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface Analog {

    enum Side {
        ONE,
        TWO,
        THREE
    }

    enum Type {
        THREE_PHASE_POWER,
        THREE_PHASE_ACTIVE_POWER,
        THREE_PHRASE_REACTIVE_POWER,
        LINE_CURRENT,
        PHASE_VOLTAGE,
        LINE_TO_LINE_VOLTAGE,
        ANGLE,
        OTHER
    }

    String getId();

    Type getType();

    Object getProperty(String name);

    Analog putProperty(String name, Object property);

    double getValue();

    Side getSide();
}
