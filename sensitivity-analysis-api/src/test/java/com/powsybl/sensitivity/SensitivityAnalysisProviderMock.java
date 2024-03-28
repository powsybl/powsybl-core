/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@AutoService(SensitivityAnalysisProvider.class)
public class SensitivityAnalysisProviderMock implements SensitivityAnalysisProvider {

    @Override
    public CompletableFuture<Void> run(Network network, String workingVariantId, SensitivityFactorReader factorReader, SensitivityResultWriter resultWriter, List<Contingency> contingencies, List<SensitivityVariableSet> variableSets, SensitivityAnalysisParameters parameters, ComputationManager computationManager, ReportNode reportNode) {
        int[] factorIndex = new int[1];
        factorReader.read((functionType, functionId, variableType, variableId, variableSet, contingencyContext) -> {
            switch (contingencyContext.getContextType()) {
                case NONE:
                    resultWriter.writeSensitivityValue(factorIndex[0]++, -1, 0.0, 0.0);
                    break;

                case ALL:
                    for (int contingencyIndex = 0; contingencyIndex < contingencies.size(); contingencyIndex++) {
                        resultWriter.writeSensitivityValue(factorIndex[0]++, contingencyIndex, 0.0, 0.0);
                    }
                    break;

                case SPECIFIC:
                    int contingencyIndex = IntStream.range(0, contingencies.size())
                            .filter(i -> contingencies.get(i).getId().equals(contingencyContext.getContingencyId()))
                            .findFirst()
                            .orElseThrow();
                    resultWriter.writeSensitivityValue(factorIndex[0]++, contingencyIndex, 0.0, 0.0);
                    break;
            }
        });
        for (int contingencyIndex = 0; contingencyIndex < contingencies.size(); contingencyIndex++) {
            resultWriter.writeContingencyStatus(contingencyIndex, SensitivityAnalysisResult.Status.SUCCESS);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "SensitivityAnalysisMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public Optional<Extension<SensitivityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.of(new SensitivityAnalysisParametersTest.DummyExtension());
    }

    @Override
    public List<String> getSpecificParametersNames() {
        return Collections.singletonList("dummy-extension");
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new SensitivityAnalysisParametersTest.DummySerializer());
    }

    @Override
    public Optional<Extension<SensitivityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.of(new SensitivityAnalysisParametersTest.DummyExtension());
    }
}
