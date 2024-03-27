/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class Util {

    private Util() {
    }

    public static String[] retainAll(String[] strings, String[] stringsToKeep) {
        if (strings == null || stringsToKeep == null) {
            return new String[0];
        }
        Set<String> setStringsToKeep = new HashSet<>(Arrays.asList(stringsToKeep));
        List<String> kept = new ArrayList<>();
        for (String s : strings) {
            if (setStringsToKeep.contains(s)) {
                kept.add(s);
            }
        }
        return kept.toArray(new String[0]);
    }
}
