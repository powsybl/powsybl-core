/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.MinMaxReactiveLimits;
import com.powsybl.iidm.network.MinMaxReactiveLimitsAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMinMaxReactiveLimitsTest {

    private Generator generator;

    @BeforeEach
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
        ValidationException e = assertThrows(ValidationException.class, () -> addMinMaxReactiveLimits(Double.NaN, 100.0));
        assertTrue(e.getMessage().contains("minimum reactive power is not set"));
    }

    @Test
    public void invalidMaxQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> addMinMaxReactiveLimits(10.0, Double.NaN));
        assertTrue(e.getMessage().contains("maximum reactive power is not set"));
    }

    @Test
    public void invalidMinQBiggerThenMaxQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> addMinMaxReactiveLimits(2.0, 1.0));
        assertTrue(e.getMessage().contains("maximum reactive power is expected to be greater than or equal to minimum reactive power"));
    }

    @Test
    public void testProperties() {
        MinMaxReactiveLimits limits = generator.newMinMaxReactiveLimits()
                .setMaxQ(100.0)
                .setMinQ(10.0)
                .add();

        // Test that MinMaxReactiveLimits supports properties
        assertFalse(limits.hasProperty());

        String key = "testKey";
        String value = "testValue";
        String oldValue = limits.setProperty(key, value);
        assertNull(oldValue);
        assertTrue(limits.hasProperty());
        assertTrue(limits.hasProperty(key));
        assertEquals(value, limits.getProperty(key));
        assertEquals(value, limits.getProperty(key, "default"));

        // Test updating property
        String newValue = "newValue";
        oldValue = limits.setProperty(key, newValue);
        assertEquals(value, oldValue);
        assertEquals(newValue, limits.getProperty(key));

        // Test removing property
        assertTrue(limits.removeProperty(key));
        assertFalse(limits.hasProperty());
        assertFalse(limits.hasProperty(key));
        assertNull(limits.getProperty(key));
        assertEquals("default", limits.getProperty(key, "default"));
    }

    @Test
    public void testAdderProperties() {
        // Test that MinMaxReactiveLimitsAdder supports properties
        MinMaxReactiveLimitsAdder adder = generator.newMinMaxReactiveLimits()
                .setMaxQ(100.0)
                .setMinQ(10.0);

        // Test adder properties
        assertFalse(adder.hasProperty());

        String key = "testKey";
        String value = "testValue";
        String oldValue = adder.setProperty(key, value);
        assertNull(oldValue);
        assertTrue(adder.hasProperty());
        assertTrue(adder.hasProperty(key));
        assertEquals(value, adder.getProperty(key));
        assertEquals(value, adder.getProperty(key, "default"));

        // Test that properties are copied to the created limits
        MinMaxReactiveLimits limits = adder.add();
        assertTrue(limits.hasProperty(key));
        assertEquals(value, limits.getProperty(key));
    }

    private void addMinMaxReactiveLimits(double minQ, double maxQ) {
        generator.newMinMaxReactiveLimits()
                .setMaxQ(maxQ)
                .setMinQ(minQ)
            .add();
    }
}
