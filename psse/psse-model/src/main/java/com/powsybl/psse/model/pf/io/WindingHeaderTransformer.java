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
public class WindingHeaderTransformer extends HeaderTransformer {
    private final String windingNumber;

    public WindingHeaderTransformer(String... args) {
        windingNumber = args[0];
    }

    @Override
    public String transformName(Field field, String name) {
        // Add "<windingNumber>" as a suffix
        return name + windingNumber;
    }
}
