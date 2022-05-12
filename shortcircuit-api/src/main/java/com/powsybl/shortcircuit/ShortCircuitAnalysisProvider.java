/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.shortcircuit.interceptors.ShortCircuitAnalysisInterceptor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 *  <p>Computation results are provided asynchronously as a {@link ShortCircuitAnalysisResult}.
 *
 *  <p>Implementations of that interface may typically rely on an external tool.
 *
 *  <p>{@link ShortCircuitAnalysisInterceptor Interceptors} might be used to execute client user-specific code
 *  on events such as the availability of N-situation results, for example to further customize the results content
 *  through {@link com.powsybl.commons.extensions.Extension Extensions}.
 *
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public interface ShortCircuitAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    void addInterceptor(ShortCircuitAnalysisInterceptor interceptor);

    boolean removeInterceptor(ShortCircuitAnalysisInterceptor interceptor);

    /**
     * Run an asynchronous single short circuit analysis job.
     *
     * @param monitors stateMonitor that defines the fault about which information will be written after short circuit analysis
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                              List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<StateMonitor> monitors) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, monitors);
    }

    /**
     * Run an asynchronous single short circuit analysis job.
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network, List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<StateMonitor> monitors,
                                                              Reporter reporter) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, monitors, reporter);
    }
}
