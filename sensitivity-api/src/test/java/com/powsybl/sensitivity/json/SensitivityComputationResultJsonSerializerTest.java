/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.powsybl.sensitivity.SensitivityComputationResults;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import static org.junit.Assert.*;

public class SensitivityComputationResultJsonSerializerTest {
    @Test
    public void testRoundTrip() throws IOException {
        byte[] inputBytes = IOUtils.toByteArray(getClass().getResourceAsStream("/resultsExport.json"));

        SensitivityComputationResults results = SensitivityComputationResultJsonSerializer.read(new InputStreamReader(new ByteArrayInputStream(inputBytes)));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SensitivityComputationResultJsonSerializer.write(results, new OutputStreamWriter(outputStream));
        byte[] outputBytes = outputStream.toByteArray();

        assertArrayEquals(inputBytes, outputBytes);
    }

}
