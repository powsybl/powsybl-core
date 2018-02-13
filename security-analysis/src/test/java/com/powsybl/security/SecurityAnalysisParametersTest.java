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

        assertEquals(1, parameters.getExtensions().size());
        assertEquals(true, parameters.getExtensions().contains(extensionSecurityAnalysisParameters));
        assertEquals(true, parameters.getExtensionByName("extensionSecurityAnalysisParameters") instanceof ExtensionSecurityAnalysisParameters);
        assertEquals(true, parameters.getExtension(ExtensionSecurityAnalysisParameters.class) instanceof ExtensionSecurityAnalysisParameters);
    }

    @Test
    public void testNoExtensions() {
        SecurityAnalysisParameters parameters = SecurityAnalysisParameters.load();

        assertEquals(0, parameters.getExtensions().size());
        assertEquals(false, parameters.getExtensions().contains(new ExtensionSecurityAnalysisParameters()));
        assertEquals(false, parameters.getExtensionByName("extensionLoadFlowParameters") instanceof ExtensionSecurityAnalysisParameters);
        assertEquals(false, parameters.getExtension(ExtensionSecurityAnalysisParameters.class) instanceof ExtensionSecurityAnalysisParameters);
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
