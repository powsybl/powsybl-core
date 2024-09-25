/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit;

import com.google.auto.service.AutoService;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.com>}
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
                                                             ReportNode reportNode) {
        reportNode.newReportNode().withMessageTemplate("MockShortCircuit", "Running mock short circuit").add();
        return run(network, faults, parameters, computationManager, faultParameters);
    }

    public static ShortCircuitAnalysisResult runWithNonEmptyResult() {
        Fault fault = new BusFault("F1", "VLGEN", 0.0, 0.0, Fault.ConnectionType.SERIES, Fault.FaultType.THREE_PHASE);
        MagnitudeFeederResult feederResult = new MagnitudeFeederResult("GEN", 5);
        LimitViolation limitViolation = new LimitViolation("VLGEN", LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT, 0, 0, 0);
        FortescueFaultResult faultResult = new FortescueFaultResult(fault, 10.0, Collections.singletonList(feederResult), Collections.singletonList(limitViolation),
                new FortescueValue(10.0), null, Collections.emptyList(), Duration.ofSeconds(1), FortescueFaultResult.Status.SUCCESS);
        return new ShortCircuitAnalysisResult(Collections.singletonList(faultResult));
    }

    @Override
    public Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.of(new ShortCircuitParametersTest.DummySerializer());
    }

    @Override
    public Optional<Extension<ShortCircuitParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.of(new ShortCircuitParametersTest.DummyExtension());
    }

    @Override
    public Optional<ModuleConfig> getModuleConfig(PlatformConfig platformConfig) {
        return Optional.empty();
    }

}
