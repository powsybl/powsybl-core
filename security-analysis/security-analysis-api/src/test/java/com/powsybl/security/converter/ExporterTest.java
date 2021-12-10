/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.security.*;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.extensions.VoltageExtension;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.security.results.BranchResult;
import com.powsybl.security.results.BusResults;
import com.powsybl.security.results.PostContingencyResult;
import com.powsybl.security.results.ThreeWindingsTransformerResult;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
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
        violation4.addExtension(VoltageExtension.class, new VoltageExtension(400.0));

        LimitViolation violation5 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.ACTIVE_POWER, "20'", 1200, 100, 1.0f, 110.0, Branch.Side.ONE);
        LimitViolation violation6 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.APPARENT_POWER, "20'", 1200, 100, 1.0f, 110.0, Branch.Side.TWO);

        Contingency contingency = Contingency.builder("contingency")
            .addBranch("NHV1_NHV2_2", "VLNHV1")
            .addBranch("NHV1_NHV2_1")
            .addGenerator("GEN")
            .addBusbarSection("BBS1")
            .build();

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(true, Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, true, Arrays.asList(violation2, violation3, violation4, violation5, violation6), Arrays.asList("action1", "action2"));

        List<BranchResult> preContingencyBranchResults = new ArrayList<>();
        preContingencyBranchResults.add(new BranchResult("branch1", 0, 0, 0, 0, 0, 0));
        preContingencyBranchResults.add(new BranchResult("branch2", 0, 0, 0, 0, 0, 0, 10));
        List<BusResults> preContingencyBusResults = new ArrayList<>();
        preContingencyBusResults.add(new BusResults("voltageLevelId", "busId", 400, 3.14));
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = new ArrayList<>();
        threeWindingsTransformerResults.add(new ThreeWindingsTransformerResult("threeWindingsTransformerId",
            0, 0, 0, 0, 0, 0, 0, 0, 0));

        SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, Collections.singletonList(postContingencyResult),
            preContingencyBranchResults, preContingencyBusResults, threeWindingsTransformerResults);
        result.setNetworkMetadata(new NetworkMetadata(NETWORK));
        return result;
    }

    @Test
    public void testCompatibilityV1Deserialization() {
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 100, 0.95f, 110.0, Branch.Side.ONE);
        violation1.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0));
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.json"));
        Assertions.assertThat(result.getPreContingencyLimitViolationsResult().getLimitViolations()).hasSize(1);
        assertEquals(0, LimitViolations.comparator().compare(violation1, result.getPreContingencyLimitViolationsResult().getLimitViolations().get(0)));

    }

    @Test
    public void roundTripJson() throws IOException {
        SecurityAnalysisResult result = create();

        roundTripTest(result, ExporterTest::writeJson, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        BiConsumer<SecurityAnalysisResult, Path> exporter = (res, path) -> {
            SecurityAnalysisResultExporters.export(res, path, "JSON");
        };
        roundTripTest(result, exporter, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        // Check invalid path
        assertThrows(UncheckedIOException.class, () -> SecurityAnalysisResultExporters.export(result, Paths.get(""), "JSON"));
        // Check invalid format
        assertThrows(PowsyblException.class, () -> SecurityAnalysisResultExporters.export(result, tmpDir.resolve("data"), "XXX"));
    }

    @Test
    public void roundTripJsonWithProperties() throws IOException {
        SecurityAnalysisResult result = create();

        roundTripTest(result, ExporterTest::writeJsonWithProperties, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        BiConsumer<SecurityAnalysisResult, Path> exporter = (res, path) -> {
            SecurityAnalysisResultExporters.export(res, null, path, "JSON");
        };
        roundTripTest(result, exporter, SecurityAnalysisResultDeserializer::read, "/SecurityAnalysisResult.json");

        // Check invalid path
        assertThrows(UncheckedIOException.class, () -> SecurityAnalysisResultExporters.export(result, null, Paths.get(""), "JSON"));
        // Check invalid format
        assertThrows(PowsyblException.class, () -> SecurityAnalysisResultExporters.export(result, null, tmpDir.resolve("data"), "XXX"));
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

    private static void writeJsonWithProperties(SecurityAnalysisResult result, Path path) {
        SecurityAnalysisResultExporter exporter = SecurityAnalysisResultExporters.getExporter("JSON");
        assertNotNull(exporter);
        assertEquals("JSON", exporter.getFormat());

        try (Writer writer = Files.newBufferedWriter(path)) {
            exporter.export(result, new Properties(), writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
