/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.hybrid.security.analysis.parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/** * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}*/

class HybridSecurityAnalysisParametersTest {
    private HybridSecurityAnalysisParameters hybridSecurityAnalysisParameters;

    @BeforeEach
    void setUp() {
        hybridSecurityAnalysisParameters = new HybridSecurityAnalysisParameters();
    }

    @Test
    void testDefaultValues() {
        assertNull(hybridSecurityAnalysisParameters.getFirstProviderName());
        assertNull(hybridSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testSetGetFirstProviderName() {
        hybridSecurityAnalysisParameters.setFirstProviderName("OpenLoadFlow");
        assertEquals("OpenLoadFlow", hybridSecurityAnalysisParameters.getFirstProviderName());
    }

    @Test
    void testSetGetSecondProviderName() {
        hybridSecurityAnalysisParameters.setSecondProviderName("Dynaflow");
        assertEquals("Dynaflow", hybridSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testCompleteConfiguration() {
        hybridSecurityAnalysisParameters.setFirstProviderName("OpenLoadFlow");
        hybridSecurityAnalysisParameters.setSecondProviderName("Dynaflow");
        assertEquals("OpenLoadFlow", hybridSecurityAnalysisParameters.getFirstProviderName());
        assertEquals("Dynaflow", hybridSecurityAnalysisParameters.getSecondProviderName());
    }
}
