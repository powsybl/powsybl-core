/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReactiveCapabilityCurve;
import com.powsybl.iidm.network.ReactiveLimitsKind;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ReactiveCapabilityCurveTest {

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
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should have at least two points");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add();
    }

    @Test
    public void duplicatePointsInCurve() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a point already exists for active power");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidPoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("P is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(Double.NaN)
                        .setMaxQ(5.0)
                        .setMinQ(1.0)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidMaxQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("max Q is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(Double.NaN)
                        .setMinQ(1.0)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidMinQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("min Q is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(Double.NaN)
                    .endPoint()
                .add();
    }

    @Ignore(value = "To be reactivated in IIDM v1.1")
    @Test
    public void invalidMinQGreaterThanMaxQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("maximum reactive power is expected to be greater than or equal to minimum reactive power");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0)
                        .setMaxQ(5.0)
                        .setMinQ(50.0)
                    .endPoint()
                .add();
    }

}
