/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coline Piloquet <coline.piloquet@rte-france.com>
 */
@AutoService(ShortCircuitAnalysisProvider.class)
public class ShortCircuitAnalysisMock implements ShortCircuitAnalysisProvider {

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
    public CompletableFuture<ShortCircuitAnalysisResult> run(Network network, ShortCircuitParameters parameters, ComputationManager computationManager) {
        return CompletableFuture.completedFuture(new ShortCircuitAnalysisResult(new ArrayList<>(), new ArrayList<>()));
    }

    public static ShortCircuitAnalysisResult runAsync(Network network) {
        FaultResult faultResult = new FaultResult("VLGEN", 10);
        ShortCircuitAnalysisResult result = new ShortCircuitAnalysisResult(Collections.singletonList(faultResult), Collections.emptyList());
        return result;
    }
}
