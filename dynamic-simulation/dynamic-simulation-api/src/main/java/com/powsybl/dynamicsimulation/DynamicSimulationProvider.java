/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation;

import java.util.concurrent.CompletableFuture;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

/**
 * SPI for load implementations.
 *
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public interface DynamicSimulationProvider extends Versionable, PlatformConfigNamedProvider {

    /**
     * Run a dynamic simulation on variant {@code workingVariantId} of {@code network} delegating external program execution to
     * {@code computationManager} if necessary and using dynamic simulation execution {@code parameters}. This method is expected
     * to be stateless so that it can be call simultaneously with different arguments (a different network for instance)
     * without any concurrency issue.
     *
     * @param network the network
     * @param curvesSupplier the supplier of curves
     * @param workingVariantId variant id of the network
     * @param computationManager a computation manager to external program execution
     * @param parameters dynamic simulation execution parameters
     * @return a {@link CompletableFuture} on {@link DynamicSimulationResult]
     */
    CompletableFuture<DynamicSimulationResult> run(Network network, CurvesSupplier curvesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters);

}
