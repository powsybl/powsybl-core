/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
class JsonDynamicSecurityAnalysisParametersTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.getDynamicSimulationParameters().setStopTime(20.5);
        parameters.getDynamicContingenciesParameters().setContingenciesStartTime(5.5);
        roundTripTest(parameters, JsonDynamicSecurityAnalysisParameters::write, JsonDynamicSecurityAnalysisParameters::read, "/DynamicSecurityAnalysisParametersV1.json");
    }

    @Test
    void writeExtension() throws IOException {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.addExtension(DynamicSecurityDummyExtension.class, new DynamicSecurityDummyExtension());
        writeTest(parameters, JsonDynamicSecurityAnalysisParameters::write, ComparisonUtils::assertTxtEquals, "/DynamicSecurityAnalysisParametersWithExtension.json");
    }

    @Test
    void updateDynamicSimulationParameters() {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        parameters.getDynamicSimulationParameters().setStopTime(8);
        JsonDynamicSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersIncomplete.json"));
        assertEquals(8, parameters.getDynamicSimulationParameters().getStopTime());
    }

    @Test
    void readExtension() {
        DynamicSecurityAnalysisParameters parameters = JsonDynamicSecurityAnalysisParameters.read(getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DynamicSecurityDummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    void readError() {
        InputStream inputStream = getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersInvalid.json");
        assertThrows(IllegalStateException.class, () -> JsonDynamicSecurityAnalysisParameters.read(inputStream), "Unexpected field: unexpected");
    }

    @Test
    void updateExtensions() {
        DynamicSecurityAnalysisParameters parameters = new DynamicSecurityAnalysisParameters();
        DynamicSecurityDummyExtension extension = new DynamicSecurityDummyExtension();
        extension.setParameterBoolean(false);
        extension.setParameterString("test");
        extension.setParameterDouble(2.8);
        DynamicSecurityDummyExtension oldExtension = new DynamicSecurityDummyExtension(extension);
        parameters.addExtension(DynamicSecurityDummyExtension.class, extension);
        JsonDynamicSecurityAnalysisParameters.update(parameters, getClass().getResourceAsStream("/DynamicSecurityAnalysisParametersExtensionUpdate.json"));
        DynamicSecurityDummyExtension updatedExtension = parameters.getExtension(DynamicSecurityDummyExtension.class);
        Assertions.assertEquals(oldExtension.isParameterBoolean(), updatedExtension.isParameterBoolean());
        Assertions.assertEquals(oldExtension.getParameterDouble(), updatedExtension.getParameterDouble(), 0.01);
        Assertions.assertNotEquals(oldExtension.getParameterString(), updatedExtension.getParameterString());
    }
}
