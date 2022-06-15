/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.auto.service.AutoService;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
@AutoService(ShortCircuitAnalysisProvider.class)
public class ShortCircuitAnalysisMock implements ShortCircuitAnalysisProvider {

    @Override
    public String getName() {
        return "ShortCircuitAnalysisMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                             List<Fault> faults,
                                                             ShortCircuitParameters parameters,
                                                             ComputationManager computationManager,
                                                             List<FaultParameters> faultParameters) {
        return CompletableFuture.completedFuture(new ShortCircuitAnalysisResult(new ArrayList<>()));
    }

    @Override
    public CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                             List<Fault> faults,
                                                             ShortCircuitParameters parameters,
                                                             ComputationManager computationManager,
                                                             List<FaultParameters> faultParameters,
                                                             Reporter reporter) {
        reporter.createSubReporter("MockShortCircuit", "Running mock short circuit");
        return run(network, faults, parameters, computationManager, faultParameters);
    }

    public static ShortCircuitAnalysisResult runWithNonEmptyResult() {
        Fault fault = new BusFault("F1", "VLGEN", 0.0, 0.0, Fault.ConnectionType.SERIES, Fault.FaultType.THREE_PHASE);
        FeederResult feederResult = new FeederResult("GEN", 5);
        LimitViolation limitViolation = new LimitViolation("VLGEN", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 0, 0, 0);
        FaultResult faultResult = new FaultResult(fault, 10.0, Collections.singletonList(feederResult), Collections.singletonList(limitViolation),
                new FortescueValue(10.0), null, Collections.emptyList(), Duration.ofSeconds(1));
        return new ShortCircuitAnalysisResult(Collections.singletonList(faultResult));
    }
}
