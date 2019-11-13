/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public abstract class AbstractTransformerConversion
    extends AbstractConductingEquipmentConversion {

    protected static final String STRING_POWER_TRANSFORMER = "PowerTransformer";
    protected static final String STRING_R = "r";
    protected static final String STRING_X = "x";
    protected static final String STRING_RATIO_TAP_CHANGER = "RatioTapChanger";
    protected static final String STRING_PHASE_TAP_CHANGER = "PhaseTapChanger";
    protected static final String STRING_RATEDU = "ratedU";
    protected static final String STRING_G = "g";
    protected static final String STRING_B = "b";
    protected static final String STRING_PHASE_ANGLE_CLOCK = "phaseAngleClock";
    protected static final String STRING_STEP_VOLTAGE_INCREMENT = "stepVoltageIncrement";
    protected static final String STRING_STEP_PHASE_SHIFT_INCREMENT = "stepPhaseShiftIncrement";

    protected static final String STRING_LOW_STEP = "lowStep";
    protected static final String STRING_HIGH_STEP = "highStep";
    protected static final String STRING_NEUTRAL_STEP = "neutralStep";
    protected static final String STRING_NORMAL_STEP = "normalStep";
    protected static final String STRING_SV_TAP_STEP = "SVtapStep";
    protected static final String STRING_LTC_FLAG = "ltcFlag";
    protected static final String STRING_STEP = "step";
    protected static final String STRING_RATIO = "ratio";
    protected static final String STRING_ANGLE = "angle";
    protected static final String STRING_PHASE_TAP_CHANGER_TYPE = "phaseTapChangerType";
    protected static final String STRING_X_STEP_MIN = "xStepMin";
    protected static final String STRING_X_STEP_MAX = "xStepMax";
    protected static final String STRING_X_MIN = "xMin";
    protected static final String STRING_X_MAX = "xMax";
    protected static final String STRING_VOLTAGE_STEP_INCREMENT = "voltageStepIncrement";
    protected static final String STRING_WINDING_CONNECTION_ANGLE = "windingConnectionAngle";
    protected static final String STRING_SYMMETRICAL = "symmetrical";
    protected static final String STRING_ASYMMETRICAL = "asymmetrical";
    protected static final String STRING_TABULAR = "tabular";
    protected static final String STRING_TCUL_CONTROL_MODE = "tculControlMode";
    protected static final String STRING_TAP_CHANGER_CONTROL_ENABLED = "tapChangerControlEnabled";

    protected enum TapChangerType {
        NULL, FIXED, NON_REGULATING, REGULATING
    }

    public AbstractTransformerConversion(String type, PropertyBags ends, Context context) {
        super(type, ends, context);
    }

}
