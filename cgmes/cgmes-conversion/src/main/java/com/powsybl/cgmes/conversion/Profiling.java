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
    private final Map<String, Long> loopsCount = new HashMap<>(32);
    private final Map<String, Long> loopsTime = new HashMap<>(32);
    private long t0;
    private long loopCount;
    private long loopTime;
    private long loopT0;

    public void start() {
        t0 = System.currentTimeMillis();
    }

    public void startLoop() {
        loopCount = 0;
        loopTime = 0;
        start();
    }

    public void startLoopIteration() {
        loopT0 = System.currentTimeMillis();
    }

    public void endLoopIteration() {
        loopCount++;
        loopTime += System.currentTimeMillis() - loopT0;
    }

    public void endLoop(String op) {
        end(op);
        loopsTime.put(op, loopTime);
        loopsCount.put(op, loopCount);
    }

    public void end(String op) {
        if (optime.containsKey(op)) {
            throw new IllegalArgumentException("Operation " + op + " already exists");
        }
        ops.add(op);
        optime.put(op, System.currentTimeMillis() - t0);
    }

    public void report() {
        report(true);
    }

    public void report(boolean loopDetails) {
        if (LOGGER.isInfoEnabled()) {
            ops.forEach(op -> {
                if (loopDetails) {
                    reportLoopDetails(op);
                } else {
                    reportHideLoops(op);
                }
            });
        }
    }

    private void reportLoopDetails(String op) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(
                "%-20s %6d %s",
                op,
                optime.get(op),
                loopsCount.containsKey(op) ? String.format("LOOP count %6d iteration_time %6d", loopsCount.get(op), loopsTime.get(op)) : ""));
        }
    }

    private void reportHideLoops(String op) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(String.format(
                "%-20s %6d",
                op,
                loopsCount.containsKey(op) ? loopsTime.get(op) : optime.get(op)));
        }
    }
}
