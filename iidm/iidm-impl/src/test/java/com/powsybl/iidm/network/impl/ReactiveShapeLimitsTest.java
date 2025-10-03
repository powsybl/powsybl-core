/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.ReactiveShapeImpl;
import com.powsybl.iidm.network.ReactiveShapePlane;
import com.powsybl.iidm.network.ReactiveShapePolyhedron;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.ejml.UtilEjml.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReactiveShapeLimitsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveShapeImpl.class);
    public static final String INSIDE = "INSIDE";
    public static final String OUTSIDE = "OUTSIDE";
    public static final String TEST_POINT_P_Q_U_IS = "Test Point (P={}, Q={}, U={}) is {}";

    @Test
    void testReactiveLimits() {
        // --- 1. Define the Operating Envelope (Convex Polyhedron) ---
        // Example: A generator's operational envelope (simplified for demonstration)
        // Constraints in the P-Q-U space:
        //
        // 1. P_max: P <= 100 MW (This must be reformulated to Q + 0*U + 1*P < 100)
        // 2. P_min: P >= 20 MW   (Q + 0*U + 1*P > 20)
        // 3. Q_max: Q <= 50 MVaR (Q + 0*U + 0*P < 50)
        // 4. Q_min: Q >= -10 MVaR (Q + 0*U + 0*P > -10)
        // 5. U_max: U <= 1.05 Volts (Q + 1*U + 0*P < Q_base + 1.05) - Simplified to just U bound for now

        // Note on P and Q bounds: We must match the plane form: Q + alpha*U + beta*P relation gamma

        List<ReactiveShapePlane> envelopeConstraints = Arrays.asList(

                new ReactiveShapePlane(0.0, 0.0, 100.0, true), // Q < 100
                new ReactiveShapePlane(0.0, 0.0, 20.0, false),  // Q > 20

                new ReactiveShapePlane(0.0, 1.0, 80.0, true),  // Q + P < 80
                new ReactiveShapePlane(0.0, 1.0, 10.0, false),  // Q + P > 10

                new ReactiveShapePlane(1.0, 0.0, 51.1, true),   // Q + U < 51.1
                new ReactiveShapePlane(1.0, 0.0, 20, false)   // Q + U > 20


        );
        ReactiveShapePolyhedron operatingEnvelope = new ReactiveShapePolyhedron(envelopeConstraints);
        // ... (Previous print statements) ...
        LOGGER.info("--- Operating Envelope Constraints ---");
        LOGGER.info("{}",operatingEnvelope);


        // --- 2. Test Points ---

        // Test Point 1: INSIDE (well within limits)
        double p1 = 25.0;  // MW
        double q1 = 20.0;  // MVaR
        double u1 = 1.0;   // KV

        boolean isInside1 = operatingEnvelope.isInside(p1, q1, u1);
        LOGGER.info(TEST_POINT_P_Q_U_IS, p1, q1, u1, isInside1 ? INSIDE : OUTSIDE); // Expected: INSIDE
        assertTrue(isInside1);

        double p2 = 30.0;
        double q2 = 120.0;
        double u2 = 1.0;

        boolean isInside2 = operatingEnvelope.isInside(p2, q2, u2);
        LOGGER.info(TEST_POINT_P_Q_U_IS, p2, q2, u2, isInside2 ? INSIDE : OUTSIDE); // Expected: OUTSIDE
        assertFalse(isInside2);

        double p3 = 130.0;
        double q3 = 50.0;
        double u3 = 2.0;

        boolean isInside3 = operatingEnvelope.isInside(p3, q3, u3);
        LOGGER.info(TEST_POINT_P_Q_U_IS, p3, q3, u3, isInside3 ? INSIDE : OUTSIDE); // Expected: OUTSIDE
        assertFalse(isInside3);

        LOGGER.info("------------------------------------");
        LOGGER.info("\n--- Min/Max Q Tests (Fixed P, U) ---");

        // Test 4: Feasible (P=30, U=1.0)
        double p4 = 30.0;
        double u4 = 1.0;
        double minQ4 = operatingEnvelope.getMinQ(p4, u4);
        double maxQ4 = operatingEnvelope.getMaxQ(p4, u4);


        assertFalse(true);

        // FIX THIS CODE

        // Expected limits for (P=30, U=1.0)
        // Q > 20.0
        // Q < 100.0
        // Q + 1*P > 10.0 -> Q > 10 - 30 = -20.0
        // Q + 1*P < 50.0 -> Q < 50 - 30 = 20.0
        // Q + 10*U + 1*P < 150 -> Q < 150 - 10*1.0 - 30 = 110.0

        // Resulting Min Q: max(20.0, -20.0) = 20.0
        // Resulting Max Q: min(100.0, 20.0, 110.0) = 20.0



        LOGGER.info("For (P={}, U={}: Min Q = {}, Max Q = {}",p4, u4, minQ4, maxQ4); // Expected: Min Q=20.0, Max Q=20.0


        // Test 5: Infeasible P-U point (U=0.5 violates U > 0.9 constraint)
        double p5 = 30.0;
        double u5 = 0.5;
        double minQ5 = operatingEnvelope.getMinQ(p5, u5);
        double maxQ5 = operatingEnvelope.getMaxQ(p5, u5);

        LOGGER.info("For (P={}, U={}): Min Q = {}, Max Q = {}",p5, u5, minQ5, maxQ5); // Expected: Min Q=0.4, Max Q=0.6 (But P-U is infeasible)


        // Test 6: Demonstration of the P-only methods
        double p6 = 30.0;
        assertThrows(PowsyblException.class,() -> {
            operatingEnvelope.getMinQ(p6);
        });

        assertThrows(PowsyblException.class,() -> {
            operatingEnvelope.getMaxQ(p6);
        });


    }

}
