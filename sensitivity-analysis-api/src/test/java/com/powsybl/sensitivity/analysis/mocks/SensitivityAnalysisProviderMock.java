/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.analysis.mocks;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.analysis.SensitivityAnalysisParameters;
import com.powsybl.sensitivity.analysis.SensitivityAnalysisProvider;
import com.powsybl.sensitivity.analysis.SensitivityAnalysisResult;
import com.powsybl.sensitivity.analysis.SensitivityFactorsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@AutoService(SensitivityAnalysisProvider.class)
public class SensitivityAnalysisProviderMock implements SensitivityAnalysisProvider {

    @Override
    public CompletableFuture<SensitivityAnalysisResult> run(Network network, String workingStateId, SensitivityFactorsProvider factorsProvider, ContingenciesProvider contingenciesProvider, SensitivityAnalysisParameters parameters, ComputationManager computationManager) {
        return CompletableFuture.completedFuture(new SensitivityAnalysisResult(
            true,
            new HashMap<>(),
            "ok",
            new ArrayList<>(),
            new HashMap<>()));
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
