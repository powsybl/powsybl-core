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
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

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
                .withResourceBundles(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
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

}
