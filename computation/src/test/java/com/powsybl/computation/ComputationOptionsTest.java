/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Yichen TANG <yichen.tang at rte-france.com>
 */
public class ComputationOptionsTest {

    @Test
    public void testEmpty() {
        ComputationOptions empty = ComputationOptions.empty();
        assertFalse(empty.getTimeout("cmd").isPresent());
        assertFalse(empty.getQos("cmd").isPresent());
    }

    @Test
    public void testBuilder() {
        String cmdId = "cmd";
        ComputationOptions opts = new ComputationOptionsBuilder()
                .setTimeout(cmdId, 10)
                .setQos(cmdId, "p1")
                .setDeadline(cmdId, 42)
                .build();
        assertEquals(10, opts.getTimeout(cmdId).orElse(-1));
        assertEquals("p1", opts.getQos(cmdId).orElse("asdf"));
        assertEquals(42, opts.getDeadline(cmdId).orElse(-1));
    }

    @Test
    public void testInvalid() {
        try {
            ComputationOptions opts = new ComputationOptionsBuilder()
                    .setTimeout("inv", 0)
                    .build();
            fail();
        } catch (Exception e) {
            // ignore
        }
    }
}
