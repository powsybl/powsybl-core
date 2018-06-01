/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import java.util.Locale;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class AmplConstants {

    private AmplConstants() {
    }

    public static final float INVALID_FLOAT_VALUE = -99999f;

    public static final Locale LOCALE = Locale.US;

    /**
     * Base power for computations = 100 MVA.
     */
    public static final float SB = 100f;

    public static final String LEG1_SUFFIX = "_leg1";
    public static final String LEG2_SUFFIX = "_leg2";
    public static final String LEG3_SUFFIX = "_leg3";
}
