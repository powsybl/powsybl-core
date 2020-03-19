/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.mock;

import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SensitivityComputationFactoryMock implements SensitivityComputationFactory {
    @Override
    public SensitivityComputation create(Network network, ComputationManager computationManager, int priority) {
        return new SensitivityComputation() {
            @Override
            public CompletableFuture<SensitivityComputationResults> run(SensitivityFactorsProvider factorsProvider, ContingenciesProvider contingenciesProvider, String workingStateId, SensitivityComputationParameters sensiParameters) {
                List<SensitivityValue> sensitivityValuesN = factorsProvider.getFactors(network).stream().map(factor -> new SensitivityValue(factor, Math.random(), Math.random(), Math.random())).collect(Collectors.toList());
                List<SensitivityValue> sensitivityValuesContingency = factorsProvider.getFactors(network).stream().map(factor -> new SensitivityValue(factor, Math.random(), Math.random(), Math.random())).collect(Collectors.toList());

                Map<String, List<SensitivityValue>> sensitivityValuesContingencies = Collections.singletonMap(contingenciesProvider.getContingencies(network).get(0).getId(), sensitivityValuesContingency);
                SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", sensitivityValuesN, sensitivityValuesContingencies);
                return CompletableFuture.completedFuture(results);
            }

            @Override
            public CompletableFuture<SensitivityComputationResults> run(SensitivityFactorsProvider factorsProvider, String workingStateId, SensitivityComputationParameters sensiParameters) {
                List<SensitivityValue> sensitivityValuesN = factorsProvider.getFactors(network).stream().map(factor -> new SensitivityValue(factor, Math.random(), Math.random(), Math.random())).collect(Collectors.toList());
                SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", sensitivityValuesN, Collections.emptyMap());
                return CompletableFuture.completedFuture(results);
            }

            @Override
            public String getName() {
                return "Sensitivity computation mock";
            }

            @Override
            public String getVersion() {
                return null;
            }
        };
    }
}
