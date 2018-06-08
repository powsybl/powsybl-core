/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.math.casting;

import java.util.Objects;

public final class Double2Float {


    public static float safeCasting(double d) {
        if (Double.isNaN(d)) {
            return Float.NaN;
        }
        if (Math.abs(d) == Double.MAX_VALUE) {
            return Float.MAX_VALUE * (d > 0 ? 1 : -1);
        }
        if (Math.abs(d) > Float.MAX_VALUE) {
            throw new IllegalArgumentException("Can't casting " + d + " to float");
        }
        return (float) d;
    }

    public static Float safeCasting(Double d) {
        return Objects.isNull(d) ? null : safeCasting((double) d);
    }

    private Double2Float() {

    }

}
