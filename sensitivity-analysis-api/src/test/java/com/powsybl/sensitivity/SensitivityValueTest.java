/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.sensitivity.json.JsonSensitivityAnalysisParameters;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SensitivityValueTest extends AbstractConverterTest {

    @Test
    public void test() {
        SensitivityValue value = new SensitivityValue(0, -1, 1d, 2d);
        assertEquals(0, value.getFactorIndex());
        assertEquals(-1, value.getContingencyIndex());
        assertEquals(1d, value.getValue(), 0d);
        assertEquals(2d, value.getFunctionReference(), 0d);
        assertEquals("SensitivityValue(factorIndex=0, contingencyIndex='-1', value=1.0, functionReference=2.0)", value.toString());
    }

    @Test
    public void testJson() throws IOException {
        SensitivityValue value = new SensitivityValue(0, 0, 1d, 2d);
        ObjectMapper objectMapper = JsonSensitivityAnalysisParameters.createObjectMapper();
        roundTripTest(value, (value2, jsonFile) -> JsonUtil.writeJson(jsonFile, value, objectMapper),
            jsonFile -> JsonUtil.readJson(jsonFile, SensitivityValue.class, objectMapper), "/valueRef.json");
    }
}
