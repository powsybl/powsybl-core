/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.sensitivity.SensitivityAnalysisResult;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;
import com.powsybl.sensitivity.json.SensitivityFactorsJsonSerializer;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Agnes Leroy <agnes.leroy@rte-france.com>
 */
public class CsvSensitivityAnalysisResultExporterTest extends AbstractConverterTest {

    private static SensitivityAnalysisResult createSensitivityResult() {
        // read sensitivity factors
        List<SensitivityFactor> factors;
        try {
            factors = SensitivityFactorsJsonSerializer.read(new InputStreamReader(SensitivityAnalysisResultExportersTest.class.getResourceAsStream("/sensitivityFactorsExample.json")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        List<SensitivityValue> sensitivityValues = new ArrayList<>(Collections.emptyList());
        factors.forEach(factor -> {
            sensitivityValues.add(new SensitivityValue(factor, "c1", 0, 0));
        });
        // create result
        return new SensitivityAnalysisResult(true, Collections.emptyMap(), "", sensitivityValues);
    }

    public void writeCsv(SensitivityAnalysisResult results, Path path) {
        SensitivityAnalysisResultExporters.export(results, path, "CSV");
    }

    @Test
    public void testWriteCsv() throws IOException {
        SensitivityAnalysisResult result = createSensitivityResult();
        writeTest(result, this::writeCsv, AbstractConverterTest::compareTxt, "/sensitivity-results.csv");
    }

    @Test
    public void testComment() {
        SensitivityAnalysisResultExporter exporter = Objects.requireNonNull(SensitivityAnalysisResultExporters.getExporter("CSV"));
        assertEquals("Export a sensitivity analysis result in CSV format", exporter.getComment());
    }
}
