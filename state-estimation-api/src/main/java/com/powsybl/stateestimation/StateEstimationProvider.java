/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.stateestimation;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.iidm.network.Network;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * What a state estimation implementation provides. Implementations are discovered with {@link ServiceLoader}.
 *
 * <p>Measurements are read from the network rather than passed in, through the IIDM measurement
 * extensions carried by its equipment. A second measurement snapshot is a second network variant,
 * which is why every entry point names the variant it runs against.</p>
 *
 * @author Šime Pavlić {@literal <sime.pavlic at kickstage.com>}
 */
public interface StateEstimationProvider extends Versionable, PlatformConfigNamedProvider {

    static List<StateEstimationProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(StateEstimationProvider.class, StateEstimationProvider.class.getClassLoader()));
    }

    /** Which parts of variant {@code workingVariantId} of {@code network} its measurements determine. */
    CompletableFuture<ObservabilityResult> analyseObservability(Network network, String workingVariantId,
                                                                StateEstimationRunParameters runParameters);

    /** The state of variant {@code workingVariantId} of {@code network} that best fits its measurements. */
    CompletableFuture<StateEstimationResult> estimate(Network network, String workingVariantId,
                                                      StateEstimationRunParameters runParameters);

    /**
     * Tests {@code estimate} for measurements that disagree with it. The estimate is used as given
     * and is not recomputed. {@code observability} tells which measurements can be tested.
     */
    CompletableFuture<BadDataResult> detectBadData(Network network, StateEstimationResult estimate,
                                                   ObservabilityResult observability,
                                                   StateEstimationRunParameters runParameters);

    /**
     * The settings this implementation adds on top of {@link StateEstimationParameters}, as an
     * extension class. An empty {@code Optional} means the implementation adds none.
     */
    default Optional<Class<? extends Extension<StateEstimationParameters>>> getSpecificParametersClass() {
        return Optional.empty();
    }

    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    default Map<String, String> createMapFromSpecificParameters(Extension<StateEstimationParameters> extension) {
        return Collections.emptyMap();
    }

    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, Map<String, String> properties) {
        // nothing to update when the implementation has no specific parameters
    }

    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, PlatformConfig config) {
        // nothing to update when the implementation has no specific parameters
    }

    default List<Parameter> getRawSpecificParameters() {
        return Collections.emptyList();
    }

    default List<Parameter> getSpecificParameters() {
        return getRawSpecificParameters();
    }

    default List<Parameter> getSpecificParameters(PlatformConfig config) {
        return getRawSpecificParameters();
    }

    default Optional<ModuleConfig> getModuleConfig(PlatformConfig config) {
        return config.getOptionalModuleConfig(getName() + "-state-estimation-parameters");
    }

    /**
     * Whether this implementation can honor the given parameters. An implementation that cannot
     * returns false instead of failing later in the run.
     */
    default boolean checkParameters(StateEstimationRunParameters runParameters) {
        return true;
    }
}
