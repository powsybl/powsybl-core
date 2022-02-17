/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity;

import com.google.auto.service.AutoService;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@AutoService(SensitivityAnalysisProvider.class)
public class SensitivityAnalysisProviderMock implements SensitivityAnalysisProvider {

    @Override
    public CompletableFuture<Void> run(Network network, String workingVariantId, SensitivityFactorReader factorReader, SensitivityValueWriter valueWriter, List<Contingency> contingencies, List<SensitivityVariableSet> variableSets, SensitivityAnalysisParameters parameters, ComputationManager computationManager, Reporter reporter) {
        int[] factorIndex = new int[1];
        factorReader.read((functionType, functionId, variableType, variableId, variableSet, contingencyContext) -> {
            switch (contingencyContext.getContextType()) {
                case NONE:
                    valueWriter.write(factorIndex[0]++, -1, 0.0, 0.0);
                    break;

                case ALL:
                    for (int contingencyIndex = 0; contingencyIndex < contingencies.size(); contingencyIndex++) {
                        valueWriter.write(factorIndex[0]++, contingencyIndex, 0.0, 0.0);
                    }
                    break;

                case SPECIFIC:
                    int contingencyIndex = IntStream.range(0, contingencies.size())
                            .filter(i -> contingencies.get(i).getId().equals(contingencyContext.getContingencyId()))
                            .findFirst()
                            .orElseThrow();
                    valueWriter.write(factorIndex[0]++, contingencyIndex, 0.0, 0.0);
                    break;
            }
        });
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
}
