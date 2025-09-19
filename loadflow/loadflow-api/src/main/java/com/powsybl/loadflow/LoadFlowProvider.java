/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.config.SpecificParametersProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service Provider Interface for loadflow implementations.
 *
 * <p>A loadflow provider is required to implement the main method {@link #run}, in charge of actually
 * running a loadflow and updating the network variant with computed physical values.
 *
 * <p>It may, additionally, provide methods to support implementation-specific parameters.
 * Specific parameters should be implemented as an implementation of {@link Extension<LoadFlowParameters>}.
 *
 * <ul>
 *     <li>In order to support JSON serialization for those specific parameters,
 *     implementing {@link #getSpecificParametersSerializer()} is required.</li>
 *     <li>In order to support loading specific parameters from platform configuration,
 *     implementing {@link #loadSpecificParameters(PlatformConfig)} is required.</li>
 * </ul>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface LoadFlowProvider extends Versionable, PlatformConfigNamedProvider, SpecificParametersProvider<LoadFlowParameters, Extension<LoadFlowParameters>> {

    static List<LoadFlowProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(LoadFlowProvider.class, LoadFlowProvider.class.getClassLoader()));
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
     * @param reportNode           the reportNode used for functional logs
     * @return a {@link CompletableFuture} on {@link LoadFlowResult]
     */
    CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingVariantId, LoadFlowParameters parameters, ReportNode reportNode);

    /**
     * Get specific parameters class.
     *
     * @return The specific parameters class
     */
    Optional<Class<? extends Extension<LoadFlowParameters>>> getSpecificParametersClass();

    /**
     * Create a `Map` of parameter name / `String` value from implementation-specific parameters.
     * If the implementation does not have any specific parameters, `Map` is empty.
     *
     * @param extension the specific parameters
     * @return A `Map` of parameter name / `String` value
     */
    Map<String, String> createMapFromSpecificParameters(Extension<LoadFlowParameters> extension);

    /**
     * Updates implementation-specific parameters from a PlatformConfig
     */
    void updateSpecificParameters(Extension<LoadFlowParameters> extension, PlatformConfig config);

}
