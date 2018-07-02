/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.powsybl.iidm.network.util.BranchData;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class BranchDataTest extends AbstractLoadFlowResultsCompletionTest {

    @Test
    public void testLine() {
        BranchData lineData = new BranchData(line, 0.1f, false);
        assertEquals(lineP1, lineData.getComputedP1(), 0.0001);
        assertEquals(lineQ1, lineData.getComputedQ1(), 0.0001);
        assertEquals(lineP2, lineData.getComputedP2(), 0.0001);
        assertEquals(lineQ2, lineData.getComputedQ2(), 0.0001);
    }

    @Test
    public void testTransformer() {
        BranchData twtData = new BranchData(transformer, 0.1f, false, true);
        assertEquals(twtP1, twtData.getComputedP1(), 0.0001);
        assertEquals(twtQ1, twtData.getComputedQ1(), 0.0001);
        assertEquals(twtP2, twtData.getComputedP2(), 0.0001);
        assertEquals(twtQ2, twtData.getComputedQ2(), 0.0001);
    }

    @Test
    public void testCAS2MicroBaseCasePhaseShifter() {
        double tolerance = 0.015;
        boolean specificCompatibility = true;
        double epsilonX = 0;
        boolean applyReactanceCorrection = false;

        // Input data from ENTSO-E CASv2.0 test configuration MicroGrid, BaseCase
        // Parameters of the transformer have been copied from CIMDesk tabular reports
        // Voltages and expected flows have been taken from Documentation, Excel MicroGrid.xls
        String id = "_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0";
        double ratedU1 = 400;
        double ratedU2 = 110;
        double r1 = 2.707692;
        double x1 = 14.518904;
        double r2 = 0;
        double x2 = 0;
        double g1 = 0;
        double b1 = 0;
        double g2 = 0;
        double b2 = 0;
        double u1 = 412.989001;
        double u2 = 115.5;
        double theta1 = Math.toRadians(-6.78071);
        double theta2 = Math.toRadians(-9.39133);
        double stepR = 0;
        double stepX = 0;
        double stepG = 0;
        double stepB = 0;
        boolean connected1 = true;
        boolean connected2 = true;
        boolean mainComponent1 = true;
        boolean mainComponent2 = true;
        double expectedP1 = 55.890119;
        double expectedQ1 = -217.56609;
        double expectedP2 = -55.08795;
        double expectedQ2 = 221.867401;

        // Values rho and alpha for the current tap position have been computed using formulas
        // documented in Technical Specification IEC TS 61970-600-1
        // (Edition 1.0 2017-07, section D.6 Asymmetrical Phase Shifter, page 55)
        double stepRho = 0.9992976157;
        double stepAlpha = Math.toRadians(2.1475854283);

        // To check final PI model values
        double expectedTheta1 = -0.1183457151;
        double expectedTheta2 = -0.1639096296;
        double expectedR = 0.2047692075;
        double expectedX = 1.0979921150;
        double expectedRho1 = 0.2748068443;
        double expectedAlpha1 = 0.0374824367;

        // Compute final PI model values
        assertEquals(expectedTheta1, theta1, tolerance);
        assertEquals(expectedTheta2, theta2, tolerance);
        double rho0 = ratedU2 / ratedU1;
        double rho0Square = rho0 * rho0;
        double r0 = r1 * rho0Square + r2;
        double x0 = x1 * rho0Square + x2;
        double g0 = g1 / rho0Square + g2;
        double b0 = b1 / rho0Square + b2;
        double r = r0 * (1 + stepR);
        double x = x0 * (1 + stepX);
        assertEquals(expectedR, r, tolerance);
        assertEquals(expectedX, x, tolerance);
        if (specificCompatibility) {
            g1 = g0 / 2 * (1 + stepG);
            b1 = b0 / 2 * (1 + stepB);
            g2 = b0 / 2;
            b2 = b0 / 2;
        } else {
            g1 = g0;
            b1 = b0;
            g2 = 0;
            b2 = 0;
        }
        double rho1 = rho0 * stepRho;
        assertEquals(expectedRho1, rho1, tolerance);
        double rho2 = 1;
        double alpha1 = stepAlpha;
        assertEquals(expectedAlpha1, alpha1, tolerance);
        double alpha2 = 0;

        // Compute flows from final PI model values
        BranchData b = new BranchData(id,
                r, x,
                rho1, rho2,
                u1, u2, theta1, theta2,
                alpha1, alpha2,
                g1, g2, b1, b2,
                expectedP1, expectedQ1, expectedP2, expectedQ2,
                connected1, connected2,
                mainComponent1, mainComponent2,
                epsilonX, applyReactanceCorrection);
        assertEquals(expectedP1, b.getComputedP1(), tolerance);
        assertEquals(expectedP2, b.getComputedP2(), tolerance);
        assertEquals(expectedQ1, b.getComputedQ1(), tolerance);
        assertEquals(expectedQ2, b.getComputedQ2(), tolerance);
    }
}
