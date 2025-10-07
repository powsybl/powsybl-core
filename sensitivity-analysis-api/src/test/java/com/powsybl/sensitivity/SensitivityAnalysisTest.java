/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundle;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.PowsyblTestReportResourceBundle;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
class SensitivityAnalysisTest {

    private static final String DEFAULT_PROVIDER_NAME = "SensitivityAnalysisMock";

    private Network network;
    private ComputationManager computationManager;
    private SensitivityFactor factor;
    private SensitivityFactorReader factorReader;
    private SensitivityResultModelWriter resultWriter;
    private List<Contingency> contingencies;
    private List<SensitivityVariableSet> variableSets;
    private SensitivityAnalysisParameters parameters;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.create();
        computationManager = Mockito.mock(ComputationManager.class);
        factor = new SensitivityFactor(SensitivityFunctionType.BRANCH_ACTIVE_POWER_1,
                "NHV1_NHV2_1",
                SensitivityVariableType.INJECTION_ACTIVE_POWER,
                "GEN",
                false,
                ContingencyContext.none());
        factorReader = handler -> handler.onFactor(factor.getFunctionType(), factor.getFunctionId(), factor.getVariableType(), factor.getVariableId(), factor.isVariableSet(), factor.getContingencyContext());
        contingencies = Collections.emptyList();
        resultWriter = new SensitivityResultModelWriter(contingencies);
        variableSets = Collections.emptyList();
        parameters = Mockito.mock(SensitivityAnalysisParameters.class);
    }

    @Test
    void testDefaultProvider() {
        SensitivityAnalysis.Runner runner = SensitivityAnalysis.find();
        assertEquals(DEFAULT_PROVIDER_NAME, runner.getName());
        assertEquals("1.0", runner.getVersion());
    }

    @Test
    void testRunAsyncWithReaderAndWriter() {
        SensitivityAnalysis.runAsync(network, VariantManagerConstants.INITIAL_VARIANT_ID, factorReader, resultWriter,
                new SensitivityAnalysisRunParameters()
                    .setContingencies(contingencies)
                    .setVariableSets(variableSets)
                    .setParameters(parameters)
                    .setComputationManager(computationManager)
                    .setReportNode(ReportNode.NO_OP))
                .join();
        assertEquals(1, resultWriter.getValues().size());
    }

    @Test
    void testDeprecatedRunAsync() {
        assertEquals(1, SensitivityAnalysis.runAsync(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP).join().getValues().size());
        SensitivityAnalysis.runAsync(network, VariantManagerConstants.INITIAL_VARIANT_ID, factorReader, resultWriter,
            contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP).join();
        assertEquals(1, resultWriter.getValues().size());
    }

    @Test
    void testRunAsync() {
        assertEquals(1, SensitivityAnalysis.runAsync(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)
                .setComputationManager(computationManager)
                .setReportNode(ReportNode.NO_OP)).join().getValues().size());
        assertEquals(1, SensitivityAnalysis.runAsync(network, List.of(factor)).join().getValues().size());
    }

    @Test
    void testDeprecatedRun() {
        assertEquals(1, SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP).getValues().size());
        assertEquals(1, SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            contingencies, variableSets, parameters).getValues().size());
        assertEquals(1, SensitivityAnalysis.run(network, List.of(factor), contingencies, variableSets, parameters).getValues().size());
        assertEquals(1, SensitivityAnalysis.run(network, List.of(factor), contingencies, parameters).getValues().size());
        assertEquals(1, SensitivityAnalysis.run(network, List.of(factor), contingencies).getValues().size());
    }

    @Test
    void testRunWithReaderAndWriter() {
        SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, factorReader, resultWriter,
            new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)
                .setComputationManager(computationManager)
                .setReportNode(ReportNode.NO_OP));
        assertEquals(1, resultWriter.getValues().size());
    }

    @Test
    void testRun() {
        assertEquals(1, SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)
                .setComputationManager(computationManager)
                .setReportNode(ReportNode.NO_OP)).getValues().size());
    }

    @Test
    void testRunShort1() {
        assertEquals(1, SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
            new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)).getValues().size());
    }

    @Test
    void testRunShort2() {
        assertEquals(1, SensitivityAnalysis.run(network, List.of(factor),
            new SensitivityAnalysisRunParameters()
                .setContingencies(contingencies)
                .setVariableSets(variableSets)
                .setParameters(parameters)).getValues().size());
    }

    @Test
    void testRunShort3() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor));
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort4() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor), parameters);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunWithDefaultParameters() {
        SensitivityAnalysis.Runner runner = SensitivityAnalysis.find();
        SensitivityAnalysisResult result = runner.run(network, List.of(factor));
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunWithFluentParams() {
        SensitivityAnalysisParameters sensitivityParameters = new SensitivityAnalysisParameters();
        WeightedSensitivityVariable variable = new WeightedSensitivityVariable("v1", 3.4);
        List<SensitivityVariableSet> sensitivityVariableSets = List.of(new SensitivityVariableSet("id", List.of(variable)));
        ReportNode reportRoot = ReportNode.newRootReportNode()
            .withResourceBundles(PowsyblTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
            .withMessageTemplate("testSensitivityAnalysis")
            .build();
        SensitivityAnalysisRunParameters runParameters = new SensitivityAnalysisRunParameters()
            .setVariableSets(sensitivityVariableSets)
            .setComputationManager(computationManager)
            .setReportNode(reportRoot)
            .setContingencies(List.of(new Contingency("contingency1")))
            .setParameters(sensitivityParameters.setVoltageVoltageSensitivityValueThreshold(0.1d));

        SensitivityAnalysisResult result = SensitivityAnalysis.find().run(network, "VariantId", List.of(factor), runParameters);
        assertEquals(5, reportRoot.getChildren().size());
        assertEquals("Test sensitivity factor functionId = " + factor.getFunctionId(), reportRoot.getChildren().get(0).getMessage());
        assertEquals("Test sensititity analysis variant = VariantId", reportRoot.getChildren().get(1).getMessage());
        assertEquals("Test contingencies size = 1", reportRoot.getChildren().get(2).getMessage());
        assertEquals("Test sensitivity parameters voltageVoltageSensitivityValueThreshold = 0.1", reportRoot.getChildren().get(3).getMessage());
        assertEquals("Test variable sets size = 1", reportRoot.getChildren().get(4).getMessage());
        assertEquals(1, Mockito.mockingDetails(computationManager).getInvocations().size());
        assertEquals(1, result.getValues().size());
    }

}
