/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.util.IntCounter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum AmplSubset implements IntCounter {

    BUS(1),
    VOLTAGE_LEVEL(1),
    BRANCH(1),
    RATIO_TAP_CHANGER(1),
    PHASE_TAP_CHANGER(1),
    TAP_CHANGER_TABLE(1),
    LOAD(1),
    SHUNT(1),
    GENERATOR(1),
    TEMPORARY_CURRENT_LIMIT(1),
    THREE_WINDINGS_TRANSFO(1),
    FAULT(1),
    CURATIVE_ACTION(1),
    PREVENTIVE_ACTION(1),
    STATIC_VAR_COMPENSATOR(1),
    HVDC_LINE(1),
    LCC_CONVERTER_STATION(1),
    VSC_CONVERTER_STATION(1);

    private final int initialValue;

    AmplSubset(int initialValue) {
        this.initialValue = initialValue;
    }

    @Override
    public int getInitialValue() {
        return initialValue;
    }
}
