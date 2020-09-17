package com.powsybl.sensitivity.mocks;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.SensitivityComputationParameters;
import com.powsybl.sensitivity.SensitivityComputationProvider;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.SensitivityFactorsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * @author Joris Mancini {@literal <joris.mancini at rte-france.com>}
 */
@AutoService(SensitivityComputationProvider.class)
public class SensitivityComputationProviderMock implements SensitivityComputationProvider {

    @Override
    public CompletableFuture<SensitivityComputationResults> run(Network network, String workingStateId, SensitivityFactorsProvider factorsProvider, SensitivityComputationParameters parameters, ComputationManager computationManager) {
        return CompletableFuture.completedFuture(new SensitivityComputationResults(
            true,
            new HashMap<>(),
            "ok",
            new ArrayList<>(),
            new HashMap<>()));
    }

    @Override
    public CompletableFuture<SensitivityComputationResults> run(Network network, String workingStateId, SensitivityFactorsProvider factorsProvider, ContingenciesProvider contingenciesProvider, SensitivityComputationParameters parameters, ComputationManager computationManager) {
        return CompletableFuture.completedFuture(new SensitivityComputationResults(
            true,
            new HashMap<>(),
            "ok",
            new ArrayList<>(),
            new HashMap<>()));
    }

    @Override
    public String getName() {
        return "sensitivity-computation-mock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
