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

        Line acLineSegment = network.getLine("ACLineSegment");
        assertTrue(checkEq(acLineSegment));
        assertTrue(checkDefinedCurrentLimits(acLineSegment,
                new CurrentLimit(796.0, 900, 1990.0),
                new CurrentLimit(796.0, 900, 1990.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch));
        assertTrue(checkNotDefinedLimits(equivalentBranch));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator));
        assertTrue(checkNotDefinedLimits(seriesCompensator));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml", "line_SSH.xml");
        assertEquals(3, network.getLineCount());

        Line acLineSegment = network.getLine("ACLineSegment");
        assertTrue(checkSsh(acLineSegment));
        assertTrue(checkDefinedCurrentLimits(acLineSegment,
                new CurrentLimit(797.0, 900, 1991.0),
                new CurrentLimit(795.0, 900, 1989.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkSsh(equivalentBranch));
        assertTrue(checkNotDefinedLimits(equivalentBranch));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkSsh(seriesCompensator));
        assertTrue(checkNotDefinedLimits(seriesCompensator));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml");
        assertEquals(3, network.getLineCount());

        Line acLineSegment = network.getLine("ACLineSegment");
        assertTrue(checkEq(acLineSegment));
        assertTrue(checkDefinedCurrentLimits(acLineSegment,
                new CurrentLimit(796.0, 900, 1990.0),
                new CurrentLimit(796.0, 900, 1990.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch));
        assertTrue(checkNotDefinedLimits(equivalentBranch));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator));
        assertTrue(checkNotDefinedLimits(seriesCompensator));

        readCgmesResources(network, DIR, "line_SSH.xml");

        assertTrue(checkSsh(acLineSegment));
        assertTrue(checkDefinedCurrentLimits(acLineSegment,
                new CurrentLimit(797.0, 900, 1991.0),
                new CurrentLimit(795.0, 900, 1989.0)));

        assertTrue(checkSsh(equivalentBranch));
        assertTrue(checkNotDefinedLimits(equivalentBranch));

        assertTrue(checkSsh(seriesCompensator));
        assertTrue(checkNotDefinedLimits(seriesCompensator));

        readCgmesResources(network, DIR, "line_SSH_1.xml");

        assertTrue(checkSsh(acLineSegment));
        assertTrue(checkDefinedCurrentLimits(acLineSegment,
                new CurrentLimit(798.0, 900, 1992.0),
                new CurrentLimit(794.0, 900, 1988.0)));

        assertTrue(checkSsh(equivalentBranch));
        assertTrue(checkNotDefinedLimits(equivalentBranch));

        assertTrue(checkSsh(seriesCompensator));
        assertTrue(checkNotDefinedLimits(seriesCompensator));
    }

    private static boolean checkEq(Line line) {
        assertNotNull(line);
        assertNotNull(line.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));
        return true;
    }

    private static boolean checkSsh(Line line) {
        assertNotNull(line);
        return true;
    }

    private static boolean checkDefinedCurrentLimits(Line line, CurrentLimit currentLimit1, CurrentLimit currentLimit2) {
        assertEquals(1, line.getOperationalLimitsGroups1().size());
        assertTrue(checkDefinedCurrentLimitsSide(line, TwoSides.ONE, currentLimit1));
        assertEquals(1, line.getOperationalLimitsGroups2().size());
        assertTrue(checkDefinedCurrentLimitsSide(line, TwoSides.TWO, currentLimit2));

        assertTrue(checkNotDefinedApparentPowerLimits(line));
        assertTrue(checkNotDefinedActivePowerLimits(line));
        return true;
    }

    private static boolean checkDefinedCurrentLimitsSide(Line line, TwoSides side, CurrentLimit currentLimit) {
        assertNotNull(line.getCurrentLimits(side).orElse(null));
        if (line.getCurrentLimits(side).isPresent()) {
            assertEquals(currentLimit.ptalValue, line.getCurrentLimits(side).get().getPermanentLimit());
            assertEquals(1, line.getCurrentLimits(side).get().getTemporaryLimits().size());
            assertEquals(currentLimit.tatlDuration, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getAcceptableDuration());
            assertEquals(currentLimit.tatlValue, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getValue());
            return true;
        }
        return false;
    }

    private static boolean checkNotDefinedLimits(Line line) {
        assertEquals(0, line.getOperationalLimitsGroups1().size());
        assertEquals(0, line.getOperationalLimitsGroups2().size());
        assertTrue(checkNotDefinedCurrentLimits(line));
        assertTrue(checkNotDefinedApparentPowerLimits(line));
        assertTrue(checkNotDefinedActivePowerLimits(line));
        return true;
    }

    private static boolean checkNotDefinedCurrentLimits(Line line) {
        assertNull(line.getCurrentLimits1().orElse(null));
        assertNull(line.getCurrentLimits2().orElse(null));
        return true;
    }

    private static boolean checkNotDefinedApparentPowerLimits(Line line) {
        assertNull(line.getApparentPowerLimits1().orElse(null));
        assertNull(line.getApparentPowerLimits2().orElse(null));
        return true;
    }

    private static boolean checkNotDefinedActivePowerLimits(Line line) {
        assertNull(line.getActivePowerLimits1().orElse(null));
        assertNull(line.getActivePowerLimits2().orElse(null));
        return true;
    }

    private record CurrentLimit(double ptalValue, int tatlDuration, double tatlValue) {
    }
}
