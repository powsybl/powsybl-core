/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation;

import java.util.concurrent.CompletableFuture;

import com.powsybl.commons.Versionable;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public interface DynamicSimulationProvider extends Versionable {

    CompletableFuture<DynamicSimulationResult> run(Network network, ComputationManager computationManager, String workingVariantId, DynamicSimulationParameters parameters);

}
