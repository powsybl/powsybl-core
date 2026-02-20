/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class ReactiveCapabilityShapeLimitsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveCapabilityShapeLimitsTest.class);
    public static final String INSIDE = "INSIDE";
    public static final String OUTSIDE = "OUTSIDE";
    public static final String TEST_POINT_P_Q_U_IS = "Test Point (P={}, Q={}, U={}) is {}";
    public static final double EPSILON_PRECISION = 1e-9;

    private void assertPQUInEnvelope(ReactiveCapabilityShapePolyhedron operatingEnvelope, double p, double q, double u, boolean expected) {
        boolean isInside = operatingEnvelope.isInside(p, q, u);
        LOGGER.debug(TEST_POINT_P_Q_U_IS, p, q, u, isInside ? INSIDE : OUTSIDE); // Expected: INSIDE
        assertEquals(expected, isInside);
    }

    private void assertQBoundsInEnvelope(ReactiveCapabilityShapePolyhedron operatingEnvelope, double p, double u, double expectedMinQ, double expectedMaxQ) {

        double minQ = operatingEnvelope.getMinQ(p, u);
        double maxQ = operatingEnvelope.getMaxQ(p, u);
        LOGGER.debug("For (P={}, U={}: Min Q = {}, Max Q = {})", p, u, minQ, maxQ);
        assertEquals(expectedMinQ, minQ, EPSILON_PRECISION);
        assertEquals(expectedMaxQ, maxQ, EPSILON_PRECISION);
    }

    @Test
    void testPQUBoundsConstraints() {

        List<ReactiveCapabilityShapePlane> envelopeConstraints = new ArrayList<>();
        ReactiveCapabilityShapePolyhedron operatingEnvelope = ReactiveCapabilityShapePolyhedron.build(envelopeConstraints);
        operatingEnvelope.withActivePowerBounds(100, 200).withVoltageBounds(200, 400).withReactivePowerBounds(-40, 60);

        // Test on P Upper / Lower bounds
        assertPQUInEnvelope(operatingEnvelope, 50, 20, 300, false);
        assertPQUInEnvelope(operatingEnvelope, 210, 20, 300, false);
        assertPQUInEnvelope(operatingEnvelope, 150, 20, 300, true);

        // Test on Q Upper / Lower bounds
        assertPQUInEnvelope(operatingEnvelope, 150, -80, 300, false);
        assertPQUInEnvelope(operatingEnvelope, 150, 80, 300, false);
        assertPQUInEnvelope(operatingEnvelope, 150, 0, 300, true);

        // Test on V Upper / Lower bounds
        assertPQUInEnvelope(operatingEnvelope, 150, 20, 50, false);
        assertPQUInEnvelope(operatingEnvelope, 150, 20, 500, false);
        assertPQUInEnvelope(operatingEnvelope, 150, 20, 300, true);
    }

    @Test
    void testReactiveLimits() {

        List<ReactiveCapabilityShapePlane> envelopeConstraints = Arrays.asList(
            ReactiveCapabilityShapePlaneImpl.build(0.0, 1.0).lessOrEqual(80.0),     // Q + P ≤ 80
            ReactiveCapabilityShapePlaneImpl.build(0.0, 1.0).greaterOrEqual(10.0),  // Q + P ≥ 10
            ReactiveCapabilityShapePlaneImpl.build(1.0, 0.0).lessOrEqual(51.1),     // Q + U ≤ 51.1
            ReactiveCapabilityShapePlaneImpl.build(1.0, 0.0).greaterOrEqual(20)     // Q + U ≥ 20
        );
        // 20 ≤ Q ≤ 100
        ReactiveCapabilityShapePolyhedron operatingEnvelope = ReactiveCapabilityShapePolyhedron.build(envelopeConstraints)
                .withReactivePowerBounds(20, 100);

        assertPQUInEnvelope(operatingEnvelope, 25, 20, 1, true);
        assertPQUInEnvelope(operatingEnvelope, 30, 120, 1, false);
        assertPQUInEnvelope(operatingEnvelope, 130, 50, 2, false);

        assertQBoundsInEnvelope(operatingEnvelope, 35, 1, 20, 45);
        assertQBoundsInEnvelope(operatingEnvelope, 30, 10.5, 20, 40.6);

        double p6 = 30.0;
        assertTrue(Math.abs(operatingEnvelope.getMinQ(p6) - 20.0) < EPSILON_PRECISION);
        assertTrue(Math.abs(operatingEnvelope.getMaxQ(p6) - 50.0) < EPSILON_PRECISION);

        // test infeasible cases
        assertThrows(PowsyblException.class, () -> operatingEnvelope.getMinQ(30000));
        assertThrows(PowsyblException.class, () -> operatingEnvelope.getMinQ(30, 20000));
        assertThrows(PowsyblException.class, () -> operatingEnvelope.getMinQ(30000, 20000));

    }

    @Test
    void testGeneratorReactivePlaneToString() {
        // Generator PQV upper limit: Q + 0.002*U - 0.300*P ≤ 200
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.002, -0.3).lessOrEqual(200.0);

        assertEquals(0.002, plane.getAlpha(), EPSILON_PRECISION);
        assertEquals(-0.3, plane.getBeta(), EPSILON_PRECISION);
        assertEquals(200.0, plane.getGamma(), EPSILON_PRECISION);
        assertTrue(plane.isLessOrEqual());

        String expected = "Q + 0.002 * U - 0.300 * P ≤ 200.000";
        String actual = plane.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testBatteryReactivePlaneToString() {
        // Battery PQV lower limit: Q - 0.001*U + 0.200*P ≥ -150
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(-0.001, 0.2).greaterOrEqual(-150.0);

        assertEquals(-0.001, plane.getAlpha(), EPSILON_PRECISION);
        assertEquals(0.2, plane.getBeta(), EPSILON_PRECISION);
        assertEquals(-150.0, plane.getGamma(), EPSILON_PRECISION);
        assertTrue(plane.isGreaterOrEqual());

        String expected = "Q - 0.001 * U + 0.200 * P ≥ -150.000";
        String actual = plane.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testZeroCoefficientsSimplifiedOutput() {
        // Pure Q limit: Q ≤ 50
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0).lessOrEqual(50.0);
        String expected = "Q ≤ 50.000";
        String actual = plane.toString();
        assertEquals(expected, actual);
    }
}


