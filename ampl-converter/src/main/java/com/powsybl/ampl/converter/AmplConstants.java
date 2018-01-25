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
final class AmplConstants {

    private AmplConstants() {
    }

    static final float INVALID_FLOAT_VALUE = -99999f;

    static final Locale LOCALE = Locale.US;

    static final float SB = 100f;

    static final String LEG1_SUFFIX = "_leg1";
    static final String LEG2_SUFFIX = "_leg2";
    static final String LEG3_SUFFIX = "_leg3";
}
