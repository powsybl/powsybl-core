/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractReactiveCapabilityCurveTest {

    private Generator generator;

    @BeforeEach
    public void setUp() {
        Network network = FictitiousSwitchFactory.create();
        generator = network.getGenerator("CB");
    }

    @Test
    public void testAdder() {
        ReactiveCapabilityCurve reactiveCapabilityCurve = generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                    .beginPoint()
                        .setP(2.0)
                        .setMaxQ(10.0)
                        .setMinQ(2.0)
                    .endPoint()
                    .beginPoint()
                        .setP(100.0)
                        .setMaxQ(10.0)
                        .setMinQ(2.0)
                    .endPoint()
                .add();
        assertEquals(ReactiveLimitsKind.CURVE, reactiveCapabilityCurve.getKind());
        assertEquals(100.0, reactiveCapabilityCurve.getMaxP(), 0.0);
        assertEquals(1.0, reactiveCapabilityCurve.getMinP(), 0.0);
        assertEquals(3, reactiveCapabilityCurve.getPoints().size());
        assertEquals(5.0, reactiveCapabilityCurve.getMaxQ(1.0), 0.0);
        assertEquals(2.0, reactiveCapabilityCurve.getMinQ(2.0), 0.0);
    }

    @Test
    public void invalidOnePointCurve() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("should have at least two points"));
    }

    @Test
    public void duplicatePointsInCurve() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(6.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("a point already exists for active power"));
    }

    @Test
    public void invalidPoint() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(Double.NaN)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("P is not set"));
    }

    @Test
    public void invalidMaxQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(Double.NaN)
                        .setMinQ(1.0)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("max Q is not set"));
    }

    @Test
    public void invalidMinQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(Double.NaN)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("min Q is not set"));
    }

    @Disabled(value = "To be reactivated in IIDM v1.1")
    @Test
    public void invalidMinQGreaterThanMaxQ() {
        ValidationException e = assertThrows(ValidationException.class, () -> generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(50.0)
                    .endPoint()
                .add());
        assertTrue(e.getMessage().contains("maximum reactive power is expected to be greater than or equal to minimum reactive power"));
    }

}
