/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.*;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class ExporterTest extends AbstractConverterTest {

    private static final Network NETWORK = EurostagTutorialExample1Factory.createWithCurrentLimits();

    private static SecurityAnalysisResult create() {
        // Create a LimitViolation(CURRENT) to ensure backward compatibility works
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 100, 0.95f, 110.0, Branch.Side.ONE);
        violation1.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0));

        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "20'", 1200, 100, 1.0f, 110.0, Branch.Side.TWO);
        violation2.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0, 230.0));
        violation2.addExtension(CurrentExtension.class, new CurrentExtension(95.0));

        LimitViolation violation3 = new LimitViolation("GEN", LimitViolationType.HIGH_VOLTAGE, 100, 0.9f, 110);
        LimitViolation violation4 = new LimitViolation("GEN2", LimitViolationType.LOW_VOLTAGE, 100, 0.7f, 115);

        List<ContingencyElement> elements = Arrays.asList(
            new BranchContingency("NHV1_NHV2_2", "VLNHV1"),
            new BranchContingency("NHV1_NHV2_1"),
            new GeneratorContingency("GEN"),
            new BusbarSectionContingency("BBS1")
        );
        Contingency contingency = new Contingency("contingency", elements);

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, true, Arrays.asList(violation2, violation3, violation4), Arrays.asList("action1", "action2"));

        SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult));
        result.setNetworkMetadata(new NetworkMetadata(NETWORK));

        return result;
    }

    @Test
    public void roundTripJson() throws IOException {
        SecurityAnalysisResult result = create();

        roundTripTest(result, ExporterTest::writeJson, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        BiConsumer<SecurityAnalysisResult, Path> exporter = (res, path) -> {
            SecurityAnalysisResultExporters.export(res, path, "JSON");
        };
        roundTripTest(result, exporter, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");
    }

    private static void writeJson(SecurityAnalysisResult result, Path path) {
        SecurityAnalysisResultExporter exporter = SecurityAnalysisResultExporters.getExporter("JSON");
        assertNotNull(exporter);
        assertEquals("JSON", exporter.getFormat());

        try (Writer writer = Files.newBufferedWriter(path)) {
            exporter.export(result, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
