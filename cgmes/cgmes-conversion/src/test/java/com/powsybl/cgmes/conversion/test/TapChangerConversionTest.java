/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class TapChangerConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/tap-changer/";

    @Test
    void invalidLtcFlagTest() throws IOException {
        // CGMES network:
        //   A RatioTapChanger RTC on PowerTransformer PT_1 without load tap changing capability, but with control enabled.
        //   A PhaseTapChanger PTC on PowerTransformer PT_2 without load tap changing capability, but with control enabled.
        // IIDM network:
        //   RatioTapChanger RTC and PhaseTapChanger PTC load tap changing capability have been fixed to true.
        ReportNode reportNode = ReportNode.newRootReportNode()
                .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("testFunctionalLogs")
                .withUntypedValue("name", "invalidLtcFlagTest")
                .build();

        Network network = readCgmesResources(reportNode, DIR, "invalidLtcFlag_EQ.xml", "invalidLtcFlag_SSH.xml");
        assertNotNull(network);

        String logs = "";
        try (StringWriter sw = new StringWriter()) {
            reportNode.print(sw);
            logs = sw.toString();
        }

        // Check that RatioTapChanger RTC has load tap changing capabilities.
        RatioTapChanger rtc = network.getTwoWindingsTransformer("PT_1").getRatioTapChanger();
        assertTrue(rtc.hasLoadTapChangingCapabilities());
        assertTrue(rtc.isRegulating());
        assertTrue(logs.contains("TapChanger RTC has regulation enabled but has no load tap changing capability. Fixed ltcFlag to true."));

        // Check that PhaseTapChanger PTC has load tap changing capabilities.
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PT_2").getPhaseTapChanger();
        assertTrue(ptc.hasLoadTapChangingCapabilities());
        assertTrue(ptc.isRegulating());
        assertTrue(logs.contains("TapChanger PTC has regulation enabled but has no load tap changing capability. Fixed ltcFlag to true."));
    }

    @Test
    void currentLimiterExportTest() throws IOException {
        // IIDM network:
        //   A PhaseShifter with current limiter regulation mode.
        // CGMES network:
        //   A PhaseShifter with active power regulation mode, off regulation and the current limit saved as an operational limit.
        Network network = PhaseShifterTestCaseFactory.createRegulatingWithoutMode();

        // PhaseShifter PS1 has a PhaseTapChanger in current limiter regulation mode,
        // with a current limit value of 200A at PS1 secondary terminal.
        PhaseTapChanger ptc = network.getTwoWindingsTransformer("PS1").getPhaseTapChanger();
        assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, ptc.getRegulationMode());
        assertEquals(200, ptc.getRegulationValue());
        assertTrue(ptc.isRegulating());
        assertEquals("PS1", ptc.getRegulationTerminal().getConnectable().getId());
        assertEquals(2, ptc.getRegulationTerminal().getSide().getNum());
        assertTrue(network.getTwoWindingsTransformer("PS1").getOperationalLimitsGroups2().isEmpty());

        // PhaseTapChanger is in active power regulation mode and the regulation is disabled.
        // A CurentLimit with the value 200A has been created.
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir);
        String tapChangerControl = getElement(eqFile, "TapChangerControl", "PS1_PTC_RC");
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#RegulatingControlModeKind.activePower", getResource(tapChangerControl, "RegulatingControl.mode"));
        String limitSet = getElement(eqFile, "OperationalLimitSet", "PS1_PT_T_2_CURRENT_LIMITER_OLS");
        assertEquals("PS1_PT_T_2", getResource(limitSet, "OperationalLimitSet.Terminal"));
        String currentLimit = getElement(eqFile, "CurrentLimit", "PS1_PT_T_2_CURRENT_LIMITER_OLS_CurrentLimit_PATL_OLV");
        assertEquals("200", getAttribute(currentLimit, "CurrentLimit.value"));

        String sshFile = writeCgmesProfile(network, "SSH", tmpDir);
        tapChangerControl = getElement(sshFile, "TapChangerControl", "PS1_PTC_RC");
        assertEquals("false", getAttribute(tapChangerControl, "RegulatingControl.enabled"));
        assertEquals("0", getAttribute(tapChangerControl, "RegulatingControl.targetDeadband"));
        assertEquals("0", getAttribute(tapChangerControl, "RegulatingControl.targetValue"));
    }

    @Test
    void phaseTapChangerLinearTest() {
        // CGMES network:
        //   PowerTransformer PT_1 has a PhaseTapChangerLinear associated to its primary winding.
        //   PowerTransformer PT_2 has a PhaseTapChangerLinear associated to its secondary winding.
        //   The two TapChangers have different xMin and xMax values, but they represent in both cases
        //   the x value and x + 10% value with x the branch reactance seen from the tap changer side.
        // IIDM network:
        //   Both tap changers induce a 0% to 10% reactance deviation.
        Network network = readCgmesResources(DIR, "phaseTapChangerLinear.xml");

        // Check phase tap changer on primary winding.
        Map<Integer, PhaseTapChangerStep> steps1 = network.getTwoWindingsTransformer("PT_1").getPhaseTapChanger().getAllSteps();
        assertTapStep(steps1.get(-2), 2.0, 10.0);
        assertTapStep(steps1.get(-1), 1.0, 2.5);
        assertTapStep(steps1.get(-0), 0.0, 0.0);
        assertTapStep(steps1.get(1), -1.0, 2.5);
        assertTapStep(steps1.get(2), -2.0, 10.0);

        // Check phase tap changer on secondary winding.
        Map<Integer, PhaseTapChangerStep> steps2 = network.getTwoWindingsTransformer("PT_2").getPhaseTapChanger().getAllSteps();
        assertTapStep(steps2.get(-2), -2.0, 10.0);
        assertTapStep(steps2.get(-1), -1.0, 2.5);
        assertTapStep(steps2.get(-0), 0.0, 0.0);
        assertTapStep(steps2.get(1), 1.0, 2.5);
        assertTapStep(steps2.get(2), 2.0, 10.0);
    }

    private void assertTapStep(PhaseTapChangerStep step, double alpha, double x) {
        double tolerance = 1e-3;
        assertEquals(alpha, step.getAlpha(), tolerance);
        assertEquals(x, step.getX(), tolerance);
    }

}
