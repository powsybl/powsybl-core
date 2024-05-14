/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public final class ShortCircuitConstants {

    private ShortCircuitConstants() {
    }

    public static final boolean DEFAULT_WITH_LIMIT_VIOLATIONS = true;
    public static final boolean DEFAULT_WITH_VOLTAGE_RESULT = true;
    public static final StudyType DEFAULT_STUDY_TYPE = StudyType.TRANSIENT;
    public static final boolean DEFAULT_WITH_FEEDER_RESULT = true;
    public static final double DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD = 0.0;
    public static final boolean DEFAULT_WITH_FORTESCUE_RESULT = true;
    public static final double DEFAULT_SUB_TRANSIENT_COEFFICIENT = 0.7;
    public static final boolean DEFAULT_WITH_LOADS = true;
    public static final boolean DEFAULT_WITH_SHUNT_COMPENSATORS = true;
    public static final boolean DEFAULT_WITH_VSC_CONVERTER_STATIONS = true;
    public static final boolean DEFAULT_WITH_NEUTRAL_POSITION = false;
    public static final InitialVoltageProfileMode DEFAULT_INITIAL_VOLTAGE_PROFILE_MODE = InitialVoltageProfileMode.NOMINAL;
    public static final boolean DEFAULT_DETAILED_REPORT = true;
}
