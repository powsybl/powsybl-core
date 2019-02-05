/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public final class TemporaryLimitsUtils {

    public static String getTmpLimitName(int acceptableDuration) {
        if (acceptableDuration < 60) {
            return "IT" + acceptableDuration + "s";
        } else {
            return "IT" + acceptableDuration / 60;
        }
    }

    private TemporaryLimitsUtils() {
    }
}
