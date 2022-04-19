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
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
@AutoService(MultiShortCircuitAnalysisProvider.class)
public class MultiShortCircuitAnalysisMock implements MultiShortCircuitAnalysisProvider {

    private final List<ShortCircuitAnalysisInterceptor> interceptors = new ArrayList<>();

    @Override
    public void addInterceptor(ShortCircuitAnalysisInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    @Override
    public boolean removeInterceptor(ShortCircuitAnalysisInterceptor interceptor) {
        return interceptors.remove(interceptor);
    }

    @Override
    public String getName() {
        return "ShortCircuitAnalysisMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public CompletableFuture<ShortCircuitAnalysisMultiResult> run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager) {
        return CompletableFuture.completedFuture(new ShortCircuitAnalysisMultiResult(new ArrayList<>(), new ArrayList<>()));
    }

    @Override
    public CompletableFuture<ShortCircuitAnalysisMultiResult> run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager, Reporter reporter) {
        reporter.createSubReporter("MockShortCircuit", "Running mock short circuit");
        return run(network, parameters, computationManager);
    }

    public static ShortCircuitAnalysisMultiResult runWithNonEmptyResult() {
        FeederResult feederResult = new FeederResult("GEN", 5);
        FaultResult faultResult = new FaultResult("VLGEN", 10, Collections.singletonList(feederResult));
        LimitViolation limitViolation = new LimitViolation("VLGEN", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 0, 0, 0);
        return new ShortCircuitAnalysisMultiResult(Collections.singletonList(faultResult), Collections.singletonList(limitViolation));
    }
}
