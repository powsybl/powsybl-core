/**
 * Copyright (c) 2026, Kick Stage d.o.o.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.LineNonReciprocalImpedance;
import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pascal Ehlert {@literal <pascal.ehlert at kickstage.com>}
 */
class EquivalentBranchNonReciprocalTest {

    private static final String DIR = "/equivalent-branch/";
    private static final Complex Z12 = new Complex(10, 40);
    private static final Complex Z21 = new Complex(-5, 42);

    @Test
    void sameNominalVoltages() {
        Network network = readCgmesResources(DIR, "equivalentBranch_EQ.xml");
        Line line = network.getLine("NonReciprocal");
        assertEquals(10, line.getR(), 0);
        assertEquals(40, line.getX(), 0);
        assertEquals(0, line.getG1(), 0);
        assertEquals(0, line.getB1(), 0);
        assertEquals(0, line.getG2(), 0);
        assertEquals(0, line.getB2(), 0);
        LineNonReciprocalImpedance extension = line.getExtension(LineNonReciprocalImpedance.class);
        assertNotNull(extension);
        assertEquals(-5, extension.getR21(), 0);
        assertEquals(42, extension.getX21(), 0);
    }

    @Test
    void differentNominalVoltages() {
        Network network = readCgmesResources(DIR, "equivalentBranch_EQ.xml");
        Line line = network.getLine("NonReciprocalDifferentNominalVoltages");
        LineNonReciprocalImpedance extension = line.getExtension(LineNonReciprocalImpedance.class);
        assertNotNull(extension);

        // The ideal ratio t = 220 / 400 at end 2 is folded into the line: the admittance matrix of the line with its
        // extension must be the one of the equivalent branch on its base voltage followed by the ideal transformer,
        // I1 = V1 / z12 - V2 / (z12 t), I2 = -V1 / (z21 t) + V2 / (z21 t^2).
        double t = 220. / 400.;
        Complex y12 = new Complex(line.getR(), line.getX()).reciprocal();
        Complex y21 = new Complex(extension.getR21(), extension.getX21()).reciprocal();
        assertComplexEquals(Z12.reciprocal(), y12.add(new Complex(line.getG1(), line.getB1())));
        assertComplexEquals(Z12.reciprocal().divide(t).negate(), y12.negate());
        assertComplexEquals(Z21.reciprocal().divide(t).negate(), y21.negate());
        assertComplexEquals(Z21.reciprocal().divide(t * t), y21.add(new Complex(line.getG2(), line.getB2())));
    }

    @Test
    void reciprocalBranchesHaveNoExtension() {
        Network network = readCgmesResources(DIR, "equivalentBranch_EQ.xml");
        for (String id : new String[] {"Reciprocal", "WithoutImpedance21", "ZeroImpedance21"}) {
            Line line = network.getLine(id);
            assertEquals(10, line.getR(), 0, id);
            assertEquals(40, line.getX(), 0, id);
            assertNull(line.getExtension(LineNonReciprocalImpedance.class), id);
        }
    }

    private static void assertComplexEquals(Complex expected, Complex actual) {
        assertEquals(expected.getReal(), actual.getReal(), 1e-9);
        assertEquals(expected.getImaginary(), actual.getImaginary(), 1e-9);
    }
}
