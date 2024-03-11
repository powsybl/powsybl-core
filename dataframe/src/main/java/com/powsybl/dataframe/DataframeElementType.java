/**
 * Copyright (c) 2021-2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public enum DataframeElementType {
    BUS,
    LINE,
    TWO_WINDINGS_TRANSFORMER,
    THREE_WINDINGS_TRANSFORMER,
    GENERATOR,
    LOAD,
    BATTERY,
    SHUNT_COMPENSATOR,
    NON_LINEAR_SHUNT_COMPENSATOR_SECTION,
    LINEAR_SHUNT_COMPENSATOR_SECTION,
    DANGLING_LINE,
    TIE_LINE,
    LCC_CONVERTER_STATION,
    VSC_CONVERTER_STATION,
    STATIC_VAR_COMPENSATOR,
    SWITCH,
    VOLTAGE_LEVEL,
    SUBSTATION,
    BUSBAR_SECTION,
    HVDC_LINE,
    RATIO_TAP_CHANGER_STEP,
    PHASE_TAP_CHANGER_STEP,
    RATIO_TAP_CHANGER,
    PHASE_TAP_CHANGER,
    REACTIVE_CAPABILITY_CURVE_POINT,
    OPERATIONAL_LIMITS,
    MINMAX_REACTIVE_LIMITS,
    ALIAS,
    IDENTIFIABLE,
    INJECTION,
    BRANCH,
    TERMINAL
}
