/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoSides;
import org.junit.jupiter.api.Test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class LineUpdateTest {

    private static final String DIR = "/update/line/";

    @Test
    void importEqTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml");
        assertEquals(3, network.getLineCount());

        assertEq(network);
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml", "line_SSH.xml");
        assertEquals(3, network.getLineCount());

        assertFirstSsh(network);
    }

    @Test
    void importEqTwoSshsAndSvTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml");
        assertEquals(3, network.getLineCount());

        assertEq(network);

        readCgmesResources(network, DIR, "line_SSH.xml");
        assertFirstSsh(network);

        readCgmesResources(network, DIR, "line_SSH_1.xml");
        assertSecondSsh(network);

        assertFlowsBeforeSv(network);
        readCgmesResources(network, DIR, "line_SV.xml");
        assertFlowsAfterSv(network);
    }

    private static void assertEq(Network network) {
        assertEq(network.getLine("ACLineSegment"));
        assertDefinedCurrentLimits(network.getLine("ACLineSegment"),
                new CurrentLimit(796.0, 900, 1990.0),
                new CurrentLimit(796.0, 900, 1990.0));

        assertEq(network.getLine("EquivalentBranch"));
        assertNotDefinedLimits(network.getLine("EquivalentBranch"));

        assertEq(network.getLine("SeriesCompensator"));
        assertNotDefinedLimits(network.getLine("SeriesCompensator"));
    }

    private static void assertFirstSsh(Network network) {
        assertSsh(network.getLine("ACLineSegment"));
        assertDefinedCurrentLimits(network.getLine("ACLineSegment"),
                new CurrentLimit(797.0, 900, 1991.0),
                new CurrentLimit(795.0, 900, 1989.0));

        assertSsh(network.getLine("EquivalentBranch"));
        assertNotDefinedLimits(network.getLine("EquivalentBranch"));

        assertSsh(network.getLine("SeriesCompensator"));
        assertNotDefinedLimits(network.getLine("SeriesCompensator"));
    }

    private static void assertSecondSsh(Network network) {
        assertSsh(network.getLine("ACLineSegment"));
        assertDefinedCurrentLimits(network.getLine("ACLineSegment"),
                new CurrentLimit(798.0, 900, 1992.0),
                new CurrentLimit(794.0, 900, 1988.0));

        assertSsh(network.getLine("EquivalentBranch"));
        assertNotDefinedLimits(network.getLine("EquivalentBranch"));

        assertSsh(network.getLine("SeriesCompensator"));
        assertNotDefinedLimits(network.getLine("SeriesCompensator"));
    }

    private static void assertFlowsBeforeSv(Network network) {
        assertFlows(network.getLine("ACLineSegment").getTerminal1(), Double.NaN, Double.NaN, network.getLine("ACLineSegment").getTerminal2(), Double.NaN, Double.NaN);
        assertFlows(network.getLine("EquivalentBranch").getTerminal1(), Double.NaN, Double.NaN, network.getLine("EquivalentBranch").getTerminal2(), Double.NaN, Double.NaN);
        assertFlows(network.getLine("SeriesCompensator").getTerminal1(), Double.NaN, Double.NaN, network.getLine("SeriesCompensator").getTerminal2(), Double.NaN, Double.NaN);
    }

    private static void assertFlowsAfterSv(Network network) {
        assertFlows(network.getLine("ACLineSegment").getTerminal1(), 275.0, 50.0, network.getLine("ACLineSegment").getTerminal2(), -275.1, -50.1);
        assertFlows(network.getLine("EquivalentBranch").getTerminal1(), 175.0, 40.0, network.getLine("EquivalentBranch").getTerminal2(), -175.1, -40.1);
        assertFlows(network.getLine("SeriesCompensator").getTerminal1(), 75.0, 30.0, network.getLine("SeriesCompensator").getTerminal2(), -75.1, -30.1);
    }

    private static void assertEq(Line line) {
        assertNotNull(line);
        assertNotNull(line.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
    }

    private static void assertSsh(Line line) {
        assertNotNull(line);
    }

    private static void assertDefinedCurrentLimits(Line line, CurrentLimit currentLimit1, CurrentLimit currentLimit2) {
        assertEquals(1, line.getOperationalLimitsGroups1().size());
        assertDefinedCurrentLimitsSide(line, TwoSides.ONE, currentLimit1);
        assertEquals(1, line.getOperationalLimitsGroups2().size());
        assertDefinedCurrentLimitsSide(line, TwoSides.TWO, currentLimit2);

        assertNotDefinedApparentPowerLimits(line);
        assertNotDefinedActivePowerLimits(line);
    }

    private static void assertDefinedCurrentLimitsSide(Line line, TwoSides side, CurrentLimit currentLimit) {
        assertNotNull(line.getCurrentLimits(side).orElse(null));
        if (line.getCurrentLimits(side).isPresent()) {
            assertEquals(currentLimit.ptalValue, line.getCurrentLimits(side).get().getPermanentLimit());
            assertEquals(1, line.getCurrentLimits(side).get().getTemporaryLimits().size());
            assertEquals(currentLimit.tatlDuration, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(currentLimit.tatlValue, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getValue());
        }
    }

    private static void assertNotDefinedLimits(Line line) {
        assertEquals(0, line.getOperationalLimitsGroups1().size());
        assertEquals(0, line.getOperationalLimitsGroups2().size());
        assertNotDefinedCurrentLimits(line);
        assertNotDefinedApparentPowerLimits(line);
        assertNotDefinedActivePowerLimits(line);
    }

    private static void assertNotDefinedCurrentLimits(Line line) {
        assertNull(line.getCurrentLimits1().orElse(null));
        assertNull(line.getCurrentLimits2().orElse(null));
    }

    private static void assertNotDefinedApparentPowerLimits(Line line) {
        assertNull(line.getApparentPowerLimits1().orElse(null));
        assertNull(line.getApparentPowerLimits2().orElse(null));
    }

    private static void assertNotDefinedActivePowerLimits(Line line) {
        assertNull(line.getActivePowerLimits1().orElse(null));
        assertNull(line.getActivePowerLimits2().orElse(null));
    }

    private record CurrentLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }

    private static void assertFlows(Terminal terminal1, double p1, double q1, Terminal terminal2, double p2, double q2) {
        double tol = 0.0000001;
        assertEquals(p1, terminal1.getP(), tol);
        assertEquals(q1, terminal1.getQ(), tol);
        assertEquals(p2, terminal2.getP(), tol);
        assertEquals(q2, terminal2.getQ(), tol);
    }
}
