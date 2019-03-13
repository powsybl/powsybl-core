/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Profiling {

    private static final Logger LOGGER = LoggerFactory.getLogger(Profiling.class);

    private final List<String> ops = new ArrayList<>(32);
    private final Map<String, Long> optime = new HashMap<>(32);
    private long t0;

    public void start() {
        t0 = System.currentTimeMillis();
    }

    public void end(String op) {
        if (optime.containsKey(op)) {
            throw new IllegalArgumentException("Operation " + op + " already exists");
        }
        ops.add(op);
        optime.put(op, System.currentTimeMillis() - t0);
    }

    public void report() {
        if (LOGGER.isInfoEnabled()) {
            ops.forEach(op -> LOGGER.info(String.format("%-20s : %6d", op, optime.get(op))));
        }
    }
}
