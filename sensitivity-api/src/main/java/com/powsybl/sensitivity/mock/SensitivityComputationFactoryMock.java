/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.mock;

import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.sensitivity.SensitivityComputation;
import com.powsybl.sensitivity.SensitivityComputationFactory;
import com.powsybl.sensitivity.SensitivityComputationParameters;
import com.powsybl.sensitivity.SensitivityComputationResults;
import com.powsybl.sensitivity.SensitivityFactorsProvider;
import com.powsybl.sensitivity.SensitivityValue;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SensitivityComputationFactoryMock implements SensitivityComputationFactory {
    @Override
    public SensitivityComputation create(Network network, ComputationManager computationManager, int priority) {
        return new SensitivityComputation() {
            @Override
            public CompletableFuture<SensitivityComputationResults> run(SensitivityFactorsProvider factorsProvider, String workingStateId, SensitivityComputationParameters sensiParameters) {
                List<SensitivityValue> sensitivityValues = factorsProvider.getFactors(network).stream().map(factor -> new SensitivityValue(factor, Math.random(), Math.random(), Math.random())).collect(Collectors.toList());
                SensitivityComputationResults results = new SensitivityComputationResults(true, Collections.emptyMap(), "", sensitivityValues);
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
