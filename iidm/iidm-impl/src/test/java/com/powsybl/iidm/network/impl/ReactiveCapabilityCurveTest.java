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
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(1.0f)
                    .endPoint()
                    .beginPoint()
                        .setP(2.0f)
                        .setMaxQ(10.0f)
                        .setMinQ(2.0f)
                    .endPoint()
                    .beginPoint()
                        .setP(100.0f)
                        .setMaxQ(10.0f)
                        .setMinQ(2.0f)
                    .endPoint()
                .add();
        assertEquals(ReactiveLimitsKind.CURVE, reactiveCapabilityCurve.getKind());
        assertEquals(100.0f, reactiveCapabilityCurve.getMaxP(), 0.0f);
        assertEquals(1.0f, reactiveCapabilityCurve.getMinP(), 0.0f);
        assertEquals(3, reactiveCapabilityCurve.getPoints().size());
        assertEquals(5.0f, reactiveCapabilityCurve.getMaxQ(1.0f), 0.0f);
        assertEquals(2.0f, reactiveCapabilityCurve.getMinQ(2.0f), 0.0f);
    }

    @Test
    public void invalidOnePointCurve() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should have at least two points");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(1.0f)
                    .endPoint()
                .add();
    }

    @Test
    public void duplicatePointsInCurve() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("a point already exists for active power");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(1.0f)
                    .endPoint()
                    .beginPoint()
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(1.0f)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidPoint() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("P is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(Float.NaN)
                        .setMaxQ(5.0f)
                        .setMinQ(1.0f)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidMaxQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("max Q is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0f)
                        .setMaxQ(Float.NaN)
                        .setMinQ(1.0f)
                    .endPoint()
                .add();
    }

    @Test
    public void invalidMinQ() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("min Q is not set");
        generator.newReactiveCapabilityCurve()
                    .beginPoint()
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(Float.NaN)
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
                        .setP(1.0f)
                        .setMaxQ(5.0f)
                        .setMinQ(50.0f)
                    .endPoint()
                .add();
    }

}
