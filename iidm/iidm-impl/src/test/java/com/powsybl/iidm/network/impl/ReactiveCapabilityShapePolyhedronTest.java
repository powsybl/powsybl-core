package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveCapabilityShapePlane;
import com.powsybl.iidm.network.ReactiveCapabilityShapePolyhedron;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ReactiveCapabilityShapePolyhedronTest {

    @Test
    void testBuildAndToStringEmptyPlanes() {
        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Collections.emptyList());
        String s = poly.toString();
        assertNotNull(s);
        assertTrue(s.contains("unbounded") || s.toLowerCase().contains("undefined"),
                "Expected message about unbounded/undefined polyhedron when no planes are provided");
    }

    @Test
    void testBuildAndToStringEmptyPlanesWithBounds() {
        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Collections.emptyList())
                .withActivePowerBounds(0, 100)
                .withVoltageBounds(200, 400)
                .withReactivePowerBounds(-20, 40);
        String s = poly.toString();
        assertNotNull(s);
        assertTrue(s.contains("unbounded") || s.toLowerCase().contains("undefined"),
                "Expected message about unbounded/undefined polyhedron when no planes are provided");
        assertTrue(s.contains("Q ≥ -20"));
        assertTrue(s.contains("Q ≤ 40"));
        assertTrue(s.contains("P ≥ 0"));
        assertTrue(s.contains("P ≤ 100"));
        assertTrue(s.contains("U ≥ 200"));
        assertTrue(s.contains("U ≤ 400"));
    }

    @Test
    void testBoundsSettersAppearInToString() {
        // Create a simple plane so toString lists something beyond the bounds section
        ReactiveCapabilityShapePlane plane = ReactiveCapabilityShapePlane.build(0.0, 0.0).lessOrEqual(5.0);

        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron
                .build(Collections.singletonList(plane))
                .withReactivePowerBounds(-2.5, 5.0)
                .withActivePowerBounds(0.0, 10.0)
                .withVoltageBounds(0.9, 1.1);

        String out = poly.toString();

        // Bounds presence (we check substrings rather than full format to avoid tight coupling
        // with exact formatting of Plane.toString)
        assertTrue(out.contains("Q ≥"), "Expected min Q to appear");
        assertTrue(out.contains("Q ≤"), "Expected max Q to appear");
        assertTrue(out.contains("P ≥"), "Expected min P to appear");
        assertTrue(out.contains("P ≤"), "Expected max P to appear");
        assertTrue(out.contains("U ≥"), "Expected min U to appear");
        assertTrue(out.contains("U ≤"), "Expected max U to appear");

        // The plane should appear in the constraints list
        assertTrue(out.contains("Q") || out.contains("P") || out.contains("U"));
    }

    @Test
    void testIsInsideWithPlaneConstraintsOnly() {
        // Plane: Q ≤ 5  (alpha=0,beta=0)
        ReactiveCapabilityShapePlane p1 = ReactiveCapabilityShapePlane.build(0.0, 0.0).lessOrEqual(5.0);
        // Plane: Q ≥ -5
        ReactiveCapabilityShapePlane p2 = ReactiveCapabilityShapePlane.build(0.0, 0.0).greaterOrEqual(-5.0);

        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Arrays.asList(p1, p2));

        // Points with q inside [-5,5] should be inside (no P/U bounds set)
        assertTrue(poly.isInside(0.0, 0.0, 0.0));
        assertTrue(poly.isInside(1.0, 4.999, 2.0));
        assertTrue(poly.isInside(-3.0, -4.9999, 5.0));

        // Points outside Q bounds
        assertFalse(poly.isInside(0.0, 5.1, 0.0));
        assertFalse(poly.isInside(0.0, -5.1, 0.0));
    }

    @Test
    void testIsInsideRespectsActiveAndVoltageBounds() {
        // Simple plane that doesn't constrain P or U (only Q)
        ReactiveCapabilityShapePlane p = ReactiveCapabilityShapePlane.build(0.0, 0.0).lessOrEqual(1000.0);

        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron
                .build(Collections.singletonList(p))
                .withActivePowerBounds(0.0, 10.0)   // p must be in [0,10]
                .withVoltageBounds(0.9, 1.1)        // u must be in [0.9,1.1]
                .withReactivePowerBounds(Double.NaN, Double.NaN); // no restrictions on Q

        // Inside: p and u inside bounds
        assertTrue(poly.isInside(5.0, 0.0, 1.0));

        // Outside due to P
        assertFalse(poly.isInside(10.1, 0.0, 1.0));
        // Outside due to U
        assertFalse(poly.isInside(5.0, 0.0, 1.2));
    }

    @Test
    void testIsInsideWithMixedPlaneTypesAlphaBetaEffect() {
        // plane1: Q + 1.0*U + 0.0*P ≤ 3  -> Q ≤ 3 - U
        ReactiveCapabilityShapePlane plane1 = ReactiveCapabilityShapePlane.build(1.0, 0.0).lessOrEqual(3.0);
        // plane2: Q + 0.0*U + 2.0*P ≥ -4 -> Q ≥ -4 - 2P
        ReactiveCapabilityShapePlane plane2 = ReactiveCapabilityShapePlane.build(0.0, 2.0).greaterOrEqual(-4.0);

        ReactiveCapabilityShapePolyhedron poly = ReactiveCapabilityShapePolyhedron.build(Arrays.asList(plane1, plane2));

        // pick p=1.0, u=1.0: plane1 says Q ≤ 2, plane2 says Q ≥ -6 => Q in [-6,2]
        assertTrue(poly.isInside(1.0, 2.0, 1.0));   // equal to upper bound
        assertTrue(poly.isInside(1.0, -6.0, 1.0));  // equal to lower bound
        assertFalse(poly.isInside(1.0, 2.1, 1.0));  // just above upper bound
        assertFalse(poly.isInside(1.0, -6.1, 1.0)); // just below lower bound
    }
}
