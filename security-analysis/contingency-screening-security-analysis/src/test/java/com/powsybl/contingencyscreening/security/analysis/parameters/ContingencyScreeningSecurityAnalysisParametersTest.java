/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingencyscreening.security.analysis.parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/** * @author Riad Benradi {@literal <riad.benradi_externe at rte-france.com>}*/

class ContingencyScreeningSecurityAnalysisParametersTest {
    private com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters contingencyScreeningSecurityAnalysisParameters;

    @BeforeEach
    void setUp() {
        contingencyScreeningSecurityAnalysisParameters = new com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters();
    }

    @Test
    void testDefaultValues() {
        assertNull(contingencyScreeningSecurityAnalysisParameters.getFirstProviderName());
        assertNull(contingencyScreeningSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testSetBothProviders() {
        contingencyScreeningSecurityAnalysisParameters.setFirstProviderName("OpenLoadFlow");
        contingencyScreeningSecurityAnalysisParameters.setSecondProviderName("DynaFlow");

        assertEquals("OpenLoadFlow", contingencyScreeningSecurityAnalysisParameters.getFirstProviderName());
        assertEquals("DynaFlow", contingencyScreeningSecurityAnalysisParameters.getSecondProviderName());
    }

    @Test
    void testGetName() {
        assertEquals("contingency-screening-security-analysis-parameters", contingencyScreeningSecurityAnalysisParameters.getName());
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

    @Test
    void testUpdate() {
        com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters parameters = new com.powsybl.contingencyscreening.security.analysis.parameters.ContingencyScreeningSecurityAnalysisParameters();

        // Initial state
        assertNull(parameters.getFirstProviderName());
        assertNull(parameters.getSecondProviderName());

        // Update both
        Map<String, String> properties = new HashMap<>();
        properties.put("firstProviderName", "static-flow");
        properties.put("secondProviderName", "dynamic-sim");

        parameters.update(properties);

        assertEquals("static-flow", parameters.getFirstProviderName());
        assertEquals("dynamic-sim", parameters.getSecondProviderName());

        // Partial update: only second provider
        Map<String, String> partialProperties = new HashMap<>();
        partialProperties.put("secondProviderName", "new-dynamic-sim");

        parameters.update(partialProperties);

        // First one should remain unchanged
        assertEquals("static-flow", parameters.getFirstProviderName());
        assertEquals("new-dynamic-sim", parameters.getSecondProviderName());
    }
}
