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
import com.powsybl.iidm.network.ReactiveLimitsKind;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for ReactiveShapeImpl using actual ReactiveShapePlane instances.
 * Simulates a generator or battery PQV envelope at 400 kV.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class ReactiveCapabilityShapeImplTest {

    public static final double DELTA = 1e-9;
    private ReactiveCapabilityShapeImpl shape;

    @BeforeEach
    void setUp() {
        // Define convex PQV region with six bounding planes.
        // Q + 0*U + 0*P ≤ 80     → Q ≤ 80
        ReactiveCapabilityShapePlane qUpper = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0).lessOrEqual(80.0);

        // Q + 0*U + 0*P ≥ -60    → Q ≥ -60
        ReactiveCapabilityShapePlane qLower = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0).greaterOrEqual(-60.0);

        // Q + 0*U + 1*P ≤ 120
        ReactiveCapabilityShapePlane pUpper = ReactiveCapabilityShapePlaneImpl.build(0.0, 1.0).lessOrEqual(120.0);

        // Q + 0*U + 1*P ≥ -50
        ReactiveCapabilityShapePlane pLower = ReactiveCapabilityShapePlaneImpl.build(0.0, 1.0).greaterOrEqual(-50.0);

        // Q + 1*U + 0*P ≤ 410
        ReactiveCapabilityShapePlane uUpper = ReactiveCapabilityShapePlaneImpl.build(1.0, 0.0).lessOrEqual(410.0);

        // Q + 1*U + 0*P ≥ 390    → U ≥ 390
        ReactiveCapabilityShapePlane uLower = ReactiveCapabilityShapePlaneImpl.build(1.0, 0.0).greaterOrEqual(390.0);

        List<ReactiveCapabilityShapePlane> planes = Arrays.asList(qUpper, qLower, pUpper, pLower, uUpper, uLower);
        ReactiveCapabilityShapePolyhedron polyhedron = ReactiveCapabilityShapePolyhedron.build(planes);

        shape = ReactiveCapabilityShapeImpl.build(polyhedron);
    }

    @Test
    void testKindIsShape() {
        assertEquals(ReactiveLimitsKind.SHAPE, shape.getKind(),
                "ReactiveShapeImpl must return ReactiveLimitsKind.SHAPE");
    }

    @Test
    void testNominalOperatingPoint() {
        double p = 50.0;
        double u = 400.0;
        assertEquals(-10.0, shape.getMinQ(p, u), DELTA);
        assertEquals(10.0, shape.getMaxQ(p, u), DELTA);
    }

    @Test
    void testAtBoundaryConditionsWithPVFixed() {

        assertEquals(-10.0, shape.getMinQ(120.0, 400.0), DELTA);
        assertEquals(0, shape.getMaxQ(120.0, 400.0), DELTA);

        assertEquals(0.0, shape.getMinQ(-50.0, 400.0), DELTA);
        assertEquals(10, shape.getMaxQ(-50.0, 400.0), DELTA);

        assertEquals(0, shape.getMinQ(0.0, 390.0), DELTA);
        assertEquals(0, shape.getMaxQ(0.0, 410.0), DELTA);
    }

    @Test
    void testAtBoundaryConditionsWithPFixed() {

        assertEquals(-60.0, shape.getMinQ(120.0), DELTA);
        assertEquals(0, shape.getMaxQ(120.0), DELTA);

        assertEquals(0.0, shape.getMinQ(-50.0), DELTA);
        assertEquals(80, shape.getMaxQ(-50.0), DELTA);

        assertEquals(-50, shape.getMinQ(0.0), DELTA);
        assertEquals(80, shape.getMaxQ(0.0), DELTA);
    }

    @Test
    void testOutsideEnvelopeClamping() {
        // P beyond max
        assertThrows(PowsyblException.class, () -> shape.getMinQ(200.0, 400.0));
        assertThrows(PowsyblException.class, () -> shape.getMaxQ(200.0, 400.0));

        // Voltage beyond range
        assertEquals(10.0, shape.getMinQ(0.0, 380.0));
        assertEquals(-10.0, shape.getMaxQ(0.0, 420.0));
    }

    @Test
    void testToStringFormatting() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.5, -0.2).lessOrEqual(100.0);
        String s = plane.toString();
        assertTrue(s.contains("Q"));
        assertTrue(s.contains("U"));
        assertTrue(s.contains("P"));
        assertTrue(s.contains("≤ 100.000"));
    }

    @Test
    void testPlanesBoundQWithFixedPAndU() {
        // Planes: Q + 0.1*U + 0.2*P <= 100  -> Q <= 100 - 0.1U - 0.2P
        ReactiveCapabilityShapePlane upper = ReactiveCapabilityShapePlaneImpl.build(0.1, 0.2).lessOrEqual(100.0);

        // Q + 0.05*U + 0.1*P >= -50    -> Q >= -50 - 0.05U - 0.1P
        ReactiveCapabilityShapePlane lower = ReactiveCapabilityShapePlaneImpl.build(0.05, 0.1).greaterOrEqual(-50.0);

        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Arrays.asList(upper, lower));

        double p = 50.0;
        double u = 400.0;

        // Fix U = 400 (vector [Q,U,P] -> [0,1,0])
        List<LinearConstraint> additional = Collections.singletonList(
                new LinearConstraint(new double[]{0.0, 1.0, 0.0}, Relationship.EQ, u)
        );

        // Expected Qmax = 100 - 0.1*U - 0.2*P = 100 - 40 - 10 = 50
        double qMax = poly.getOptimalQ(p, GoalType.MAXIMIZE, additional);
        assertEquals(50.0, qMax, DELTA);

        // Expected Qmin = -50 - 0.05*U - 0.1*P = -50 -20 -5 = -75
        double qMin = poly.getOptimalQ(p, GoalType.MINIMIZE, additional);
        assertEquals(-75.0, qMin, DELTA);
    }

    @Test
    void testWrongUseOfPlaneForPBound() {
        // Warning : wrong way to express P ≤ 120 : -> Q + P ≤ 120
        ReactiveCapabilityShapePlane wrong = ReactiveCapabilityShapePlaneImpl.build(0.0, 1.0).lessOrEqual(120.0); // -> Q + P ≤ 120
        ReactiveCapabilityShapePlane qUpper = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0).lessOrEqual(80.0); // Q ≤ 80
        ReactiveCapabilityShapePlane qLower = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0).greaterOrEqual(-60.0); // Q >= -60
        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Arrays.asList(wrong, qUpper, qLower));
        // If P = 130, contrainte wrong => Q + 130 ≤ 120 -> Q ≤ -10.
        double q = poly.getOptimalQ(130.0, GoalType.MAXIMIZE, null);
        assertEquals(-10.0, q, DELTA);
    }

}

