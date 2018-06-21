/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class MinMaxReactiveLimitsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Generator generator;

    @Before
    public void setUp() {
        Network network = FictitiousSwitchFactory.create();
        generator = network.getGenerator("CB");
    }

    @Test
    public void testAdder() {
        // adder
        MinMaxReactiveLimits minMaxReactiveLimits = generator.newMinMaxReactiveLimits()
                                                        .setMaxQ(100.0)
                                                        .setMinQ(10.0)
                                                    .add();
        assertEquals(100.0, minMaxReactiveLimits.getMaxQ(), 0.0);
        assertEquals(100.0, minMaxReactiveLimits.getMaxQ(1.0), 0.0);
        assertEquals(10.0, minMaxReactiveLimits.getMinQ(), 0.0);
        assertEquals(10.0, minMaxReactiveLimits.getMinQ(1.0), 0.0);
        assertEquals(ReactiveLimitsKind.MIN_MAX, minMaxReactiveLimits.getKind());
    }

    @Test
    public void invalidMinQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("minimum reactive power is not set");
        addMinMaxReactiveLimits(Double.NaN, 100.0);
    }

    @Test
    public void invalidMaxQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("maximum reactive power is not set");
        addMinMaxReactiveLimits(10.0, Double.NaN);
    }

    @Test
    public void invalidMinQBiggerThenMaxQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("maximum reactive power is expected to be greater than or equal to minimum reactive power");
        addMinMaxReactiveLimits(2.0, 1.0);
    }

    private void addMinMaxReactiveLimits(double minQ, double maxQ) {
        generator.newMinMaxReactiveLimits()
                .setMaxQ(maxQ)
                .setMinQ(minQ)
            .add();
    }
}
