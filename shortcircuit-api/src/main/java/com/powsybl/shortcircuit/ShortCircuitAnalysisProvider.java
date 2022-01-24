/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;

import java.util.concurrent.CompletableFuture;

/**
 * @author Bertrand Rix <bertrand.rix at artelys.com>
 */
public interface ShortCircuitAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    void addInterceptor(ShortCircuitAnalysisInterceptor interceptor);

    boolean removeInterceptor(ShortCircuitAnalysisInterceptor interceptor);

    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network, ShortCircuitParameters parameters,
                                                              ComputationManager computationManager) {
        return ShortCircuitAnalysis.runAsync(network, parameters, computationManager);
    }
}
