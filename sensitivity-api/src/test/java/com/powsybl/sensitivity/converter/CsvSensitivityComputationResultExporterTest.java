package com.powsybl.sensitivity.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.SensitivityFactor;
import com.powsybl.sensitivity.SensitivityValue;
import com.powsybl.sensitivity.json.SensitivityFactorsJsonSerializer;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CsvSensitivityComputationResultExporterTest extends AbstractConverterTest {

    private static SensitivityComputationResults createSensitivityResult() {
        // read sensitivity factors
        List<SensitivityFactor> factors = Collections.emptyList();
        try {
            factors = SensitivityFactorsJsonSerializer.read(new InputStreamReader(SensitivityComputationResultExportersTest.class.getResourceAsStream("/sensitivityFactorsExample.json")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<SensitivityValue> sensitivityValues = new ArrayList<>(Collections.emptyList());
        factors.forEach(factor -> {
            SensitivityValue value = new SensitivityValue(factor, 0, 0, 0);
            sensitivityValues.add(value);
        });
        // create result
        SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", sensitivityValues);
        return results;
    }

    public void writeCsv(SensitivityComputationResults results, Path path) {
        SensitivityComputationResultExporters.export(results, path, "CSV");
    }

    @Test
    public void testWriteCsv() throws IOException {
        SensitivityComputationResults result = createSensitivityResult();
        writeTest(result, this::writeCsv, AbstractConverterTest::compareTxt, "/sensitivity-results.csv");
    }
}
