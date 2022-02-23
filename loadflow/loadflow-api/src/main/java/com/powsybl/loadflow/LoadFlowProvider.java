/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * SPI for load implementations.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface LoadFlowProvider extends Versionable, PlatformConfigNamedProvider {

    /**
     * Run a loadflow on variant {@code workingVariantId} of {@code network} delegating external program execution to
     * {@code computationManager} if necessary and using loadflow execution {@code parameters}. This method is expected
     * to be stateless so that it can be call simultaneously with different arguments (a different network for instance)
     * without any concurrency issue.
     *
     * @param network            the network
     * @param computationManager a computation manager to external program execution
     * @param workingVariantId   variant id of the network
     * @param parameters         load flow execution parameters
     * @return a {@link CompletableFuture} on {@link LoadFlowResult]
     */
    default CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingVariantId, LoadFlowParameters parameters) {
        return run(network, computationManager, workingVariantId, parameters, Reporter.NO_OP);
    }

    /**
     * Run a loadflow on variant {@code workingVariantId} of {@code network} delegating external program execution to
     * {@code computationManager} if necessary and using loadflow execution {@code parameters}. This method is expected
     * to be stateless so that it can be call simultaneously with different arguments (a different network for instance)
     * without any concurrency issue.
     *
     * @param network            the network
     * @param computationManager a computation manager to external program execution
     * @param workingVariantId   variant id of the network
     * @param parameters         load flow execution parameters
     * @param reporter           the reporter used for functional logs
     * @return a {@link CompletableFuture} on {@link LoadFlowResult]
     */
    default CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingVariantId, LoadFlowParameters parameters, Reporter reporter) {
        return run(network, computationManager, workingVariantId, parameters);
    }

    // Defines the JSON serializer/deserializer to use
    Optional<ExtensionJsonSerializer> getParametersExtensionSerializer();

    // Load from platform config : will probably share code with the one above
    <E extends Extension<LoadFlowParameters>> Optional<E> loadSpecificParameters(PlatformConfig config);
}
