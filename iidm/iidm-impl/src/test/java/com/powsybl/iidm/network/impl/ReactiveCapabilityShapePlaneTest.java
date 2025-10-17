package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReactiveCapabilityShapePlaneTest {

    private static final double DELTA = 1e-6;

    @Test
    void testBuildCreatesInstanceWithAlphaAndBeta() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(1.5, -2.5);

        assertEquals(1.5, plane.getAlpha(), DELTA);
        assertEquals(-2.5, plane.getBeta(), DELTA);
    }

    @Test
    void testLessOrEqualSetsGammaAndInequalityType() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(0.5, 0.8)
                .lessOrEqual(10.0);

        assertEquals(10.0, plane.getGamma(), DELTA);
        assertTrue(plane.isLessOrEqual());
        assertFalse(plane.isGreaterOrEqual());
    }

    @Test
    void testGreaterOrEqualSetsGammaAndInequalityType() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(1.0, 2.0)
                .greaterOrEqual(5.0);

        assertEquals(5.0, plane.getGamma(), DELTA);
        assertTrue(plane.isGreaterOrEqual());
        assertFalse(plane.isLessOrEqual());
    }

    @Test
    void testToStringWithPositiveAlphaAndBetaLessOrEqual() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(1.23, 4.56)
                .lessOrEqual(7.89);

        String expected = "Q + 1.230 * U + 4.560 * P ≤ 7.890";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testToStringWithNegativeAlphaAndBetaGreaterOrEqual() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(-2.5, -3.75)
                .greaterOrEqual(1.0);

        String expected = "Q - 2.500 * U - 3.750 * P ≥ 1.000";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testToStringWithZeroAlphaAndBeta() {
        // EPSILON not defined in the class, assuming small threshold (no effect)
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(0.0, 0.0)
                .lessOrEqual(2.0);

        String expected = "Q ≤ 2.000";
        assertEquals(expected, plane.toString());
    }

    @Test
    void testIsLessOrEqualAndIsGreaterOrEqualMutuallyExclusive() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(1.0, 1.0);
        plane.lessOrEqual(2.0);
        assertTrue(plane.isLessOrEqual());
        assertFalse(plane.isGreaterOrEqual());

        plane.greaterOrEqual(3.0);
        assertTrue(plane.isGreaterOrEqual());
        assertFalse(plane.isLessOrEqual());
    }

    @Test
    void testGettersReturnCorrectValues() {
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(0.9, -0.7)
                .greaterOrEqual(4.2);

        assertEquals(0.9, plane.getAlpha(), DELTA);
        assertEquals(-0.7, plane.getBeta(), DELTA);
        assertEquals(4.2, plane.getGamma(), DELTA);
    }
}
