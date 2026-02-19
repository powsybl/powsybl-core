/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import java.util.Locale;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class AmplConstants {

    private AmplConstants() {
    }

    public static final String XNODE_COUNTRY_NAME = "XNODE";

    // Column headers:
    public static final String FAULT = "fault";
    public static final String DESCRIPTION = "description";
    public static final String SUBSTATION = "substation";
    public static final String TARGET_V = "targetV (pu)";
    public static final String TARGET_Q = "targetQ (MVar)";
    public static final String CON_BUS = "con. bus";
    public static final String MINP = "minP (MW)";
    public static final String MAXP = "maxP (MW)";
    public static final String V_REGUL = "v regul.";
    public static final String V_REGUL_BUS = "v regul. bus";
    public static final String ACTIVE_POWER = "P (MW)";
    public static final String REACTIVE_POWER = "Q (MVar)";
    public static final String MIN_Q_MAX_P = "minQmaxP (MVar)";
    public static final String MIN_Q0 = "minQ0 (MVar)";
    public static final String MIN_Q_MIN_P = "minQminP (MVar)";
    public static final String MAX_Q_MAX_P = "maxQmaxP (MVar)";
    public static final String MAX_Q0 = "maxQ0 (MVar)";
    public static final String MAX_Q_MIN_P = "maxQminP (MVar)";
    public static final String NUM = "num";
    public static final String BUS = "bus";
    public static final String P0 = "p0 (MW)";
    public static final String Q0 = "q0 (MVar)";
    public static final String ID = "id";
    // End column headers

    public static final float INVALID_FLOAT_VALUE = -99999f;

    public static final Locale LOCALE = Locale.US;

    /**
     * Base power for computations = 100 MVA.
     */
    public static final float SB = 100f;

    public static final String RATIO_TABLE_SUFFIX = "_ratio_table";
    public static final String PHASE_TABLE_SUFFIX = "_phase_table";
    public static final String LEG1_SUFFIX = "_leg1";
    public static final String LEG2_SUFFIX = "_leg2";
    public static final String LEG3_SUFFIX = "_leg3";

    public static final String VARIANT = "variant";
    public static final int DEFAULT_VARIANT_INDEX = 1;
}
