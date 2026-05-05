/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyScreening.security.analysis.parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/** * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}*/

class ContingencyScreeningSecurityAnalysisParametersTest {
    private ContingencyScreeningSecurityAnalysisParameters contingencyScreeningSecurityAnalysisParameters;

    @BeforeEach
    void setUp() {
        contingencyScreeningSecurityAnalysisParameters = new ContingencyScreeningSecurityAnalysisParameters();
    }

    @Test
    void testDefaultValues() {
        assertNull(contingencyScreeningSecurityAnalysisParameters.getFirstProviderName());
        assertNull(contingencyScreeningSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testSetGetFirstProviderName() {
        contingencyScreeningSecurityAnalysisParameters.setFirstProviderName("OpenLoadFlow");
        assertEquals("OpenLoadFlow", contingencyScreeningSecurityAnalysisParameters.getFirstProviderName());
    }

    @Test
    void testSetGetSecondProviderName() {
        contingencyScreeningSecurityAnalysisParameters.setSecondProviderName("Dynaflow");
        assertEquals("Dynaflow", contingencyScreeningSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testCompleteConfiguration() {
        contingencyScreeningSecurityAnalysisParameters.setFirstProviderName("OpenLoadFlow");
        contingencyScreeningSecurityAnalysisParameters.setSecondProviderName("Dynaflow");
        assertEquals("OpenLoadFlow", contingencyScreeningSecurityAnalysisParameters.getFirstProviderName());
        assertEquals("Dynaflow", contingencyScreeningSecurityAnalysisParameters.getSecondProviderName());
    }
}
