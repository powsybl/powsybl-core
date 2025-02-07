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
        assertTrue(checkEq(acLineSegment, 1, new LimitR(true, 796.0, 900, 1990.0),
                1, new LimitR(true, 796.0, 900, 1990.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));
    }

    @Test
    void importEqAndSshTogetherTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml", "line_SSH.xml");
        assertEquals(3, network.getLineCount());

        Line acLineSegment = network.getLine("ACLineSegment");
        assertTrue(checkSsh(acLineSegment, 1, new LimitR(true, 797.0, 900, 1991.0),
                1, new LimitR(true, 795.0, 900, 1989.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkSsh(equivalentBranch, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkSsh(seriesCompensator, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));
    }

    @Test
    void importEqAndTwoSshsTest() {
        Network network = readCgmesResources(DIR, "line_EQ.xml");
        assertEquals(3, network.getLineCount());

        Line acLineSegment = network.getLine("ACLineSegment");
        assertTrue(checkEq(acLineSegment, 1, new LimitR(true, 796.0, 900, 1990.0),
                1, new LimitR(true, 796.0, 900, 1990.0)));

        Line equivalentBranch = network.getLine("EquivalentBranch");
        assertTrue(checkEq(equivalentBranch, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));

        Line seriesCompensator = network.getLine("SeriesCompensator");
        assertTrue(checkEq(seriesCompensator, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));

        readCgmesResources(network, DIR, "line_SSH.xml");

        assertTrue(checkSsh(acLineSegment, 1, new LimitR(true, 797.0, 900, 1991.0),
                1, new LimitR(true, 795.0, 900, 1989.0)));
        assertTrue(checkSsh(equivalentBranch, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));
        assertTrue(checkSsh(seriesCompensator, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));

        readCgmesResources(network, DIR, "line_SSH_1.xml");

        assertTrue(checkSsh(acLineSegment, 1, new LimitR(true, 798.0, 900, 1992.0),
                1, new LimitR(true, 794.0, 900, 1988.0)));
        assertTrue(checkSsh(equivalentBranch, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));
        assertTrue(checkSsh(seriesCompensator, 0, new LimitR(false, Double.NaN, 0, Double.NaN),
                0, new LimitR(false, Double.NaN, 0, Double.NaN)));
    }

    private static boolean checkEq(Line line, int numberOfOperationalGroups1, LimitR current1, int numberOfOperationalGroups2, LimitR current2) {
        assertNotNull(line);
        assertNotNull(line.getProperty(Conversion.PROPERTY_CGMES_ORIGINAL_CLASS));

        assertEquals(numberOfOperationalGroups1, line.getOperationalLimitsGroups1().size());
        assertTrue(checkOperationalLimitsEnd(line, TwoSides.ONE, current1));
        assertEquals(numberOfOperationalGroups2, line.getOperationalLimitsGroups2().size());
        assertTrue(checkOperationalLimitsEnd(line, TwoSides.TWO, current2));

        return true;
    }

    private static boolean checkOperationalLimitsEnd(Line line, TwoSides side, LimitR current) {
        if (current.isDefined) {
            assertNotNull(line.getCurrentLimits(side).orElse(null));
            if (line.getCurrentLimits(side).isPresent()) {
                assertEquals(current.ptalValue, line.getCurrentLimits(side).get().getPermanentLimit());
                assertEquals(1, line.getCurrentLimits(side).get().getTemporaryLimits().size());
                assertEquals(current.tatlDuration, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getAcceptableDuration());
                assertEquals(current.tatlValue, line.getCurrentLimits(side).get().getTemporaryLimits().iterator().next().getValue());
            }
        } else {
            assertNull(line.getCurrentLimits(side).orElse(null));
        }
        assertNull(line.getApparentPowerLimits(side).orElse(null));
        assertNull(line.getActivePowerLimits(side).orElse(null));
        return true;
    }

    private static boolean checkSsh(Line line, int numberOfOperationalGroups1, LimitR current1, int numberOfOperationalGroups2, LimitR current2) {
        assertNotNull(line);

        assertEquals(numberOfOperationalGroups1, line.getOperationalLimitsGroups1().size());
        assertTrue(checkOperationalLimitsEnd(line, TwoSides.ONE, current1));
        assertEquals(numberOfOperationalGroups2, line.getOperationalLimitsGroups2().size());
        assertTrue(checkOperationalLimitsEnd(line, TwoSides.TWO, current2));

        return true;
    }

    private record LimitR(boolean isDefined, double ptalValue, int tatlDuration, double tatlValue) {
    }

}
