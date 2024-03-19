/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.converter;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.contingency.*;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.security.*;
import com.powsybl.security.condition.AtLeastOneViolationCondition;
import com.powsybl.security.extensions.ActivePowerExtension;
import com.powsybl.security.extensions.CurrentExtension;
import com.powsybl.security.extensions.VoltageExtension;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.results.*;
import com.powsybl.security.strategy.ConditionalActions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class ExporterTest extends AbstractSerDeTest {

    private static final Network NETWORK = EurostagTutorialExample1Factory.createWithFixedCurrentLimits();

    private static SecurityAnalysisResult create() {
        // Create a LimitViolation(CURRENT) to ensure backward compatibility works
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 100, 0.95f, 110.0, TwoSides.ONE);
        violation1.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0));

        LimitViolation violation2 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.CURRENT, "20'", 1200, 100, 1.0f, 110.0, TwoSides.TWO);
        violation2.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0, 230.0));
        violation2.addExtension(CurrentExtension.class, new CurrentExtension(95.0));

        LimitViolation violation3 = new LimitViolation("GEN", LimitViolationType.HIGH_VOLTAGE, 100, 0.9f, 110);
        LimitViolation violation4 = new LimitViolation("GEN2", LimitViolationType.LOW_VOLTAGE, 100, 0.7f, 115);
        violation4.addExtension(VoltageExtension.class, new VoltageExtension(400.0));

        LimitViolation violation5 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.ACTIVE_POWER, "20'", 1200, 100, 1.0f, 110.0, TwoSides.ONE);
        LimitViolation violation6 = new LimitViolation("NHV1_NHV2_2", LimitViolationType.APPARENT_POWER, "20'", 1200, 100, 1.0f, 110.0, TwoSides.TWO);

        Contingency contingency = Contingency.builder("contingency")
                .addBranch("NHV1_NHV2_2", "VLNHV1")
                .addBranch("NHV1_NHV2_1")
                .addGenerator("GEN")
                .addBusbarSection("BBS1")
                .build();

        LimitViolationsResult preContingencyResult = new LimitViolationsResult(Collections.singletonList(violation1));
        PostContingencyResult postContingencyResult = new PostContingencyResult(contingency, PostContingencyComputationStatus.CONVERGED, Arrays.asList(violation2, violation3, violation4, violation5, violation6), Arrays.asList("action1", "action2"));
        List<BranchResult> preContingencyBranchResults = List.of(new BranchResult("branch1", 1, 2, 3, 1.1, 2.2, 3.3),
                new BranchResult("branch2", 0, 0, 0, 0, 0, 0, 10));
        List<BusResult> preContingencyBusResults = List.of(new BusResult("voltageLevelId", "busId", 400, 3.14));
        List<ThreeWindingsTransformerResult> threeWindingsTransformerResults = List.of(new ThreeWindingsTransformerResult("threeWindingsTransformerId", 1, 2, 3, 1.1, 2.1, 3.1, 1.2, 2.2, 3.2));
        List<OperatorStrategyResult> operatorStrategyResults = new ArrayList<>();
        operatorStrategyResults.add(
                new OperatorStrategyResult(
                        new OperatorStrategy("strategyId", ContingencyContext.specificContingency("contingency1"),
                                List.of(new ConditionalActions("stage1", new AtLeastOneViolationCondition(Collections.singletonList("violationId1")), Collections.singletonList("actionId1")))),
                        PostContingencyComputationStatus.CONVERGED,
                        new LimitViolationsResult(Collections.emptyList()),
                        new NetworkResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList())));
        SecurityAnalysisResult result = new SecurityAnalysisResult(preContingencyResult, LoadFlowResult.ComponentResult.Status.CONVERGED,
                Collections.singletonList(postContingencyResult),
                preContingencyBranchResults, preContingencyBusResults, threeWindingsTransformerResults, operatorStrategyResults);
        result.setNetworkMetadata(new NetworkMetadata(NETWORK));
        return result;
    }

    @Test
    void testCompatibilityV1Deserialization() {
        LimitViolation violation1 = new LimitViolation("NHV1_NHV2_1", LimitViolationType.CURRENT, null, Integer.MAX_VALUE, 100, 0.95f, 110.0, TwoSides.ONE);
        violation1.addExtension(ActivePowerExtension.class, new ActivePowerExtension(220.0));
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.json"));
        Assertions.assertThat(result.getPreContingencyLimitViolationsResult().getLimitViolations()).hasSize(1);
        assertEquals(0, LimitViolations.comparator().compare(violation1, result.getPreContingencyLimitViolationsResult().getLimitViolations().get(0)));
    }

    @Test
    void testCompatibilityV12DeserializationFail() {
        InputStream inputStream = getClass().getResourceAsStream("/SecurityAnalysisResultV1.2fail.json");
        assertNotNull(inputStream);
        Assertions.assertThatThrownBy(() -> SecurityAnalysisResultDeserializer.read(inputStream))
                .isInstanceOf(PowsyblException.class)
                .hasMessageContaining("PreContingencyResult. Tag: branchResults is not valid for version 1.2. Version should be <= 1.1");
    }

    @Test
    void testCompatibilityV11Deserialization() {
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.1.json"));
        assertEquals(0, result.getOperatorStrategyResults().size());
        assertEquals(3.3, result.getPreContingencyResult().getNetworkResult().getBranchResult("branch1").getI2(), 0.01);
    }

    @Test
    void testCompatibilityV12Deserialization() {
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.2.json"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, result.getPostContingencyResults().get(0).getStatus());
    }

    @Test
    void testCompatibilityV13Deserialization() {
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.3.json"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, result.getPostContingencyResults().get(0).getStatus());
    }

    @Test
    void testCompatibilityV14Deserialization() {
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.4.json"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, result.getPostContingencyResults().get(0).getStatus());
    }

    @Test
    void testCompatibilityV15Deserialization() {
        SecurityAnalysisResult result = SecurityAnalysisResultDeserializer.read(getClass().getResourceAsStream("/SecurityAnalysisResultV1.5.json"));
        assertEquals(PostContingencyComputationStatus.CONVERGED, result.getPostContingencyResults().get(0).getStatus());
        assertEquals(PostContingencyComputationStatus.CONVERGED, result.getOperatorStrategyResults().get(0).getConditionalActionsResult().get(0).getStatus());
    }

    @Test
    void roundTripJson() throws IOException {
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
    void roundTripJsonWithProperties() throws IOException {
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
