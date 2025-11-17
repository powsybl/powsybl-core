/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.config.SpecificParametersProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 *
 *  <p>Computation results are provided asynchronously as a {@link ShortCircuitAnalysisResult}.
 *
 *  <p>Implementations of that interface may typically rely on an external tool.
 *
 * @author Anne Tilloy {@literal <anne.tilloy at rte-france.com>}
 */
public interface ShortCircuitAnalysisProvider extends Versionable, PlatformConfigNamedProvider, SpecificParametersProvider<ShortCircuitParameters, Extension<ShortCircuitParameters>> {

    static List<ShortCircuitAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(ShortCircuitAnalysisProvider.class, ShortCircuitAnalysisProvider.class.getClassLoader()));
    }

    /**
     * Run an asynchronous single short circuit analysis job.
     *
     * @param faultParameters parameters that define the fault about which information will be written after short circuit analysis
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network,
                                                              List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<FaultParameters> faultParameters) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, faultParameters);
    }

    /**
     * Run an asynchronous single short circuit analysis job.
     */
    default CompletableFuture<ShortCircuitAnalysisResult> run(Network network, List<Fault> faults,
                                                              ShortCircuitParameters parameters,
                                                              ComputationManager computationManager,
                                                              List<FaultParameters> faultParameters,
                                                              ReportNode reportNode) {
        return ShortCircuitAnalysis.runAsync(network, faults, parameters, computationManager, faultParameters, reportNode);
    }

}
