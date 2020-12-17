/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public final class Util {

    private Util() {
    }

    public static String[] intersection(String[] strings1, String[] strings2) {
        return ArrayUtils.removeElements(strings1, ArrayUtils.removeElements(strings1, strings2));
    }
}
