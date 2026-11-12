/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
class ReactiveCapabilityShapePlaneTest {

    private static final double DELTA = 1e-9;

    @Test
    void testBuildCreatesInstanceWithCoeffUAndCoeffP() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(1.5, -2.5);

        assertEquals(1.5, plane.getCoeffU(), DELTA);
        assertEquals(-2.5, plane.getCoeffP(), DELTA);
    }

    @Test
    void testLessOrEqualSetsRhsAndInequalityType() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.5, 0.8)
                .lessOrEqual(10.0);

        assertEquals(10.0, plane.getRhs(), DELTA);
        assertTrue(plane.isLessOrEqual());
        assertFalse(plane.isGreaterOrEqual());
    }

    @Test
    void testGreaterOrEqualSetsRhsAndInequalityType() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(1.0, 2.0)
                .greaterOrEqual(5.0);

        assertEquals(5.0, plane.getRhs(), DELTA);
        assertTrue(plane.isGreaterOrEqual());
        assertFalse(plane.isLessOrEqual());
    }

    @Test
    void testToStringWithPositiveCoeffUAndCoeffPLessOrEqual() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(1.23, 4.56)
                .lessOrEqual(7.89);

        String expected = "Q + 1.230 * U + 4.560 * P ≤ 7.890";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testToStringWithNegativeCoeffUAndCoeffPGreaterOrEqual() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(-2.5, -3.75)
                .greaterOrEqual(1.0);

        String expected = "Q - 2.500 * U - 3.750 * P ≥ 1.000";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testToStringWithZeroCoeffUAndCoeffP() {
        // EPSILON not defined in the class, assuming small threshold (no effect)
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.0, 0.0)
                .lessOrEqual(2.0);

        String expected = "Q ≤ 2.000";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testIsLessOrEqualAndIsGreaterOrEqualMutuallyExclusive() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(1.0, 1.0);
        plane.lessOrEqual(2.0);
        assertTrue(plane.isLessOrEqual());
        assertFalse(plane.isGreaterOrEqual());

        plane.greaterOrEqual(3.0);
        assertTrue(plane.isGreaterOrEqual());
        assertFalse(plane.isLessOrEqual());
    }

    @Test
    void testGettersReturnCorrectValues() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlaneImpl.build(0.9, -0.7)
                .greaterOrEqual(4.2);

        assertEquals(0.9, plane.getCoeffU(), DELTA);
        assertEquals(-0.7, plane.getCoeffP(), DELTA);
        assertEquals(4.2, plane.getRhs(), DELTA);
    }
}
