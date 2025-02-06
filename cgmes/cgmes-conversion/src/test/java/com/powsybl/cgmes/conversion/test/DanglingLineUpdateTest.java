/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class DanglingLineUpdateTest {

    private static final String DIR = "/update/dangling-line/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml");
        assertEquals(4, network.getDanglingLineCount());

        DanglingLine acLineSegment = network.getDanglingLine("ACLineSegment");
        assertTrue(checkEq(acLineSegment));

        DanglingLine equivalentBranch = network.getDanglingLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch));

        DanglingLine powerTransformer = network.getDanglingLine("PowerTransformer");
        assertTrue(checkEq(powerTransformer));

        DanglingLine breaker = network.getDanglingLine("Breaker");
        assertTrue(checkEq(breaker));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml", "danglingLine_SSH.xml");
        assertEquals(4, network.getDanglingLineCount());

        DanglingLine acLineSegment = network.getDanglingLine("ACLineSegment");
        assertTrue(checkSsh(acLineSegment, 284.5, 70.5, false, Double.NaN, Double.NaN, Double.NaN, false));

        DanglingLine equivalentBranch = network.getDanglingLine("EquivalentBranch");
        assertTrue(checkSsh(equivalentBranch, 0.0, 0.0, true, -275.0, -50.0, 405.0, true));

        DanglingLine powerTransformer = network.getDanglingLine("PowerTransformer");
        assertTrue(checkSsh(powerTransformer, 0.0, 0.0, true, -100.0, -25.0, 225.0, true));

        DanglingLine breaker = network.getDanglingLine("Breaker");
        assertTrue(checkSsh(breaker, 0.0, 0.0, true, -10.0, -5.0, 402.0, true));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "danglingLine_EQ.xml", "danglingLine_EQ_BD.xml");
        assertEquals(4, network.getDanglingLineCount());

        DanglingLine acLineSegment = network.getDanglingLine("ACLineSegment");
        assertTrue(checkEq(acLineSegment));
        DanglingLine equivalentBranch = network.getDanglingLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch));
        DanglingLine powerTransformer = network.getDanglingLine("PowerTransformer");
        assertTrue(checkEq(powerTransformer));
        DanglingLine breaker = network.getDanglingLine("Breaker");
        assertTrue(checkEq(breaker));

        readCgmesResources(network, DIR, "danglingLine_SSH.xml");

        assertTrue(checkSsh(acLineSegment, 284.5, 70.5, false, Double.NaN, Double.NaN, Double.NaN, false));
        assertTrue(checkSsh(equivalentBranch, 0.0, 0.0, true, -275.0, -50.0, 405.0, true));
        assertTrue(checkSsh(powerTransformer, 0.0, 0.0, true, -100.0, -25.0, 225.0, true));
        assertTrue(checkSsh(breaker, 0.0, 0.0, true, -10.0, -5.0, 402.0, true));

        readCgmesResources(network, DIR, "danglingLine_SSH_1.xml");

        assertTrue(checkSsh(acLineSegment, 280.0, 70.0, false, Double.NaN, Double.NaN, Double.NaN, false));
        assertTrue(checkSsh(equivalentBranch, 0.0, 0.0, true, -270.0, -55.0, 410.0, false));
        assertTrue(checkSsh(powerTransformer, 0.0, 0.0, true, -105.0, -20.0, 227.0, true));
        assertTrue(checkSsh(breaker, 0.0, 0.0, true, -15.0, -3.0, 403.0, true));
    }

    private static boolean checkEq(DanglingLine danglingLine) {
        assertNotNull(danglingLine);
        assertTrue(Double.isNaN(danglingLine.getP0()));
        assertTrue(Double.isNaN(danglingLine.getQ0()));
        if (danglingLine.getGeneration() != null) {
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetV()));
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetP()));
            assertTrue(Double.isNaN(danglingLine.getGeneration().getTargetQ()));
            assertFalse(danglingLine.getGeneration().isVoltageRegulationOn());
        }
        assertNotNull(danglingLine.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        return true;
    }

    private static boolean checkSsh(DanglingLine danglingLine, double p0, double q0, boolean withGeneration, double targetP, double targetQ, double targetV, boolean isRegulatingOn) {
        assertNotNull(danglingLine);
        double tol = 0.0000001;
        assertEquals(p0, danglingLine.getP0(), tol);
        assertEquals(q0, danglingLine.getQ0(), tol);
        if (withGeneration) {
            assertNotNull(danglingLine.getGeneration());
            assertEquals(targetP, danglingLine.getGeneration().getTargetP(), tol);
            assertEquals(targetQ, danglingLine.getGeneration().getTargetQ(), tol);
            assertEquals(targetV, danglingLine.getGeneration().getTargetV(), tol);
            assertEquals(isRegulatingOn, danglingLine.getGeneration().isVoltageRegulationOn());
        } else {
            assertNull(danglingLine.getGeneration());
        }
        return true;
    }
}
