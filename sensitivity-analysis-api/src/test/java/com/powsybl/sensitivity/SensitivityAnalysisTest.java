/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.powsybl.commons.report.ReportNode;
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
                contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP)
                .join();
        assertEquals(1, resultWriter.getValues().size());
    }

    @Test
    void testRunAsync() {
        SensitivityAnalysisResult result = SensitivityAnalysis.runAsync(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
                        contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP)
                .join();
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRun() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
                        contingencies, variableSets, parameters, computationManager, ReportNode.NO_OP);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort1() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, VariantManagerConstants.INITIAL_VARIANT_ID, List.of(factor),
                contingencies, variableSets, parameters);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort2() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor), contingencies, variableSets, parameters);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort3() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor), contingencies, parameters);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort4() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor), contingencies);
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort5() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor));
        assertEquals(1, result.getValues().size());
    }

    @Test
    void testRunShort6() {
        SensitivityAnalysisResult result = SensitivityAnalysis.run(network, List.of(factor), parameters);
        assertEquals(1, result.getValues().size());
    }
}
