/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public interface DynamicSimulationProvider extends Versionable, PlatformConfigNamedProvider {

    static List<DynamicSimulationProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(DynamicSimulationProvider.class, DynamicSimulationProvider.class.getClassLoader()));
    }

    CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                                   OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode);
}
