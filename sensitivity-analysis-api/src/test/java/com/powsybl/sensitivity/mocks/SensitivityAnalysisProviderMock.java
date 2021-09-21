/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.mocks;

import com.google.auto.service.AutoService;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@AutoService(SensitivityAnalysisProvider.class)
public class SensitivityAnalysisProviderMock implements SensitivityAnalysisProvider {

    @Override
    public CompletableFuture<Void> run(Network network, String workingStateId, SensitivityFactorReader factorReader, SensitivityValueWriter valueWriter, List<Contingency> contingencies, List<SensitivityVariableSet> variableSets, SensitivityAnalysisParameters parameters, ComputationManager computationManager, Reporter reporter) {
        SensitivityFactor factor = new SensitivityFactor(SensitivityFunctionType.BUS_VOLTAGE,
                "dummy",
                SensitivityVariableType.BUS_TARGET_VOLTAGE,
                "dummy",
                false,
                ContingencyContext.none());
        valueWriter.write(factor, "dummy", 0, 0.0, 0.0);
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
