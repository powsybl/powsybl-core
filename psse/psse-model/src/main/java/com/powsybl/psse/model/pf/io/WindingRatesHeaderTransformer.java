/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf.io;

import com.univocity.parsers.annotations.HeaderTransformer;

import java.lang.reflect.Field;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class WindingRatesHeaderTransformer extends HeaderTransformer {
    private final String windingNumber;

    public WindingRatesHeaderTransformer(String... args) {
        windingNumber = args[0];
    }

    @Override
    public String transformName(Field field, String name) {
        if (name.equals("ratea")) {
            return "rata" + windingNumber;
        }
        if (name.equals("rateb")) {
            return "ratb" + windingNumber;
        }
        if (name.equals("ratec")) {
            return "ratc" + windingNumber;
        }
        // Add the prefix "wdg<windingNumber>"
        return "wdg" + windingNumber + name;
    }
}
