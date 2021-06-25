/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import java.util.Set;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface DiscreteMeasurement {

    enum Type {
        TAP_POSITION,
        SWITCH_POSITION,
        SHUNT_COMPENSATOR_SECTION,
        OTHER
    }

    enum TapChanger {
        RATIO_TAP_CHANGER,
        PHASE_TAP_CHANGER,
        RATIO_TAP_CHANGER_1,
        PHASE_TAP_CHANGER_1,
        RATIO_TAP_CHANGER_2,
        PHASE_TAP_CHANGER_2,
        RATIO_TAP_CHANGER_3,
        PHASE_TAP_CHANGER_3
    }

    enum ValueType {
        BOOLEAN,
        INT,
        STRING
    }

    String getId();

    Type getType();

    TapChanger getTapChanger();

    Set<String> getPropertyNames();

    String getProperty(String name);

    DiscreteMeasurement putProperty(String name, String property);

    DiscreteMeasurement removeProperty(String name);

    ValueType getValueType();

    String getValueAsString();

    int getValueAsInt();

    boolean getValueAsBoolean();

    DiscreteMeasurement setValue(String value);

    DiscreteMeasurement setValue(int value);

    DiscreteMeasurement setValue(boolean value);

    boolean isValid();

    DiscreteMeasurement setValid(boolean valid);

    void remove();
}
