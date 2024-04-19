/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic;

import com.google.common.collect.Lists;
import com.powsybl.action.Action;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.EventModelsSupplier;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.LimitViolationDetector;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import com.powsybl.security.monitor.StateMonitor;
import com.powsybl.security.strategy.OperatorStrategy;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSecurityAnalysisProvider extends Versionable, PlatformConfigNamedProvider {

    static List<DynamicSecurityAnalysisProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(DynamicSecurityAnalysisProvider.class, DynamicSecurityAnalysisProvider.class.getClassLoader()));
    }

    CompletableFuture<SecurityAnalysisReport> run(Network network,
                                                  DynamicModelsSupplier dynamicModelsSupplier,
                                                  EventModelsSupplier eventModelsSupplier,
                                                  String workingVariantId,
                                                  LimitViolationDetector detector,
                                                  LimitViolationFilter filter,
                                                  ComputationManager computationManager,
                                                  DynamicSecurityAnalysisParameters parameters,
                                                  ContingenciesProvider contingenciesProvider,
                                                  List<SecurityAnalysisInterceptor> interceptors,
                                                  List<OperatorStrategy> operatorStrategies,
                                                  List<Action> actions,
                                                  List<StateMonitor> monitors,
                                                  ReportNode reportNode);

    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    default Optional<Extension<DynamicSecurityAnalysisParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    default Optional<Extension<DynamicSecurityAnalysisParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    default void updateSpecificParameters(Extension<DynamicSecurityAnalysisParameters> extension, Map<String, String> properties) {
    }

    default Optional<String> getDynamicSimulationProviderName() {
        return Optional.empty();
    }

    default List<String> getSpecificParametersNames() {
        return Collections.emptyList();
    }
}
