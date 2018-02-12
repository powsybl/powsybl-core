/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtension;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class SecurityAnalysisParametersTest {

    @Test
    public void testExtensions() {
        SecurityAnalysisParameters parameters = new SecurityAnalysisParameters();
        ExtensionSecurityAnalysisParameters extensionSecurityAnalysisParameters = new ExtensionSecurityAnalysisParameters();
        parameters.addExtension(ExtensionSecurityAnalysisParameters.class, extensionSecurityAnalysisParameters);

        assertEquals(parameters.getExtensions().size(), 1);
        assertEquals(parameters.getExtensions().contains(extensionSecurityAnalysisParameters), true);
        assertEquals(parameters.getExtensionByName("extensionSecurityAnalysisParameters") instanceof ExtensionSecurityAnalysisParameters, true);
        assertEquals(parameters.getExtension(ExtensionSecurityAnalysisParameters.class) instanceof ExtensionSecurityAnalysisParameters, true);
    }

    @Test
    public void testNoExtensions() {
        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();

        assertEquals(parameters.getExtensions().size(), 0);
        assertEquals(parameters.getExtensions().contains(new ExtensionSecurityAnalysisParameters()), false);
        assertEquals(parameters.getExtensionByName("extensionLoadFlowParameters") instanceof ExtensionSecurityAnalysisParameters, false);
        assertEquals(parameters.getExtension(ExtensionSecurityAnalysisParameters.class) instanceof ExtensionSecurityAnalysisParameters, false);
    }

    class ExtensionSecurityAnalysisParameters extends AbstractExtension<SecurityAnalysisParameters> {

        @Override
        public String getName() {
            return "extensionSecurityAnalysisParameters";
        }

        public ExtensionSecurityAnalysisParameters() {
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
