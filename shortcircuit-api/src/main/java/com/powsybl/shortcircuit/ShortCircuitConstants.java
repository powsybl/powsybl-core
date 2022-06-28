/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public final class ShortCircuitConstants {

    private ShortCircuitConstants() {
    }

    public static final boolean DEFAULT_WITH_LIMIT_VIOLATIONS = true;
    public static final boolean DEFAULT_WITH_VOLTAGE_MAP = true;
    public static final StudyType DEFAULT_STUDY_TYPE = StudyType.TRANSIENT;
    public static final boolean DEFAULT_WITH_FEEDER_RESULT = true;
    public static final double DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD = 0.0;
    public static final boolean DEFAULT_USE_RESISTANCES = true;
    public static final boolean DEFAULT_USE_LOADS = true;
    public static final VoltageMapType DEFAULT_VOLTAGE_MAP_TYPE = VoltageMapType.NOMINAL;
    public static final NominalVoltageMapType DEFAULT_NOMINAL_VOLTAGE_MAP_TYPE = NominalVoltageMapType.IEC909;


}
