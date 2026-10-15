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
import com.powsybl.commons.parameters.ConfiguredParameter;
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

    /** Every implementation found on the classpath. */
    static List<StateEstimationProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(StateEstimationProvider.class, StateEstimationProvider.class.getClassLoader()));
    }

    /**
     * Which parts of variant {@code workingVariantId} of {@code network} its measurements determine.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return which parts of the network its measurements determine
     */
    CompletableFuture<ObservabilityResult> analyseObservability(Network network, String workingVariantId,
                                                                StateEstimationRunParameters runParameters);

    /**
     * The state of variant {@code workingVariantId} of {@code network} that best fits its measurements.
     *
     * @param network the network
     * @param workingVariantId the identifier of the network variant to run against
     * @param runParameters the run parameters
     * @return the estimated state
     */
    CompletableFuture<StateEstimationResult> estimate(Network network, String workingVariantId,
                                                      StateEstimationRunParameters runParameters);

    /**
     * Tests {@code estimate} for measurements that disagree with it. The estimate is used as given
     * and is not recomputed. {@code observability} tells which measurements can be tested.
     *
     * @param network the network
     * @param estimate the estimate to test
     * @param observability the observability result, telling which measurements can be tested
     * @param runParameters the run parameters
     * @return the outcome of the test
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

    /**
     * The serializer for this implementation's specific parameters. An empty {@code Optional} means
     * the implementation has none, or does not support JSON serialization for them.
     *
     * <p>The serializer type is {@code ExtensionJsonSerializer<StateEstimationParameters, X>}, where
     * {@code X} is the class returned by {@link #getSpecificParametersClass()}.</p>
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads this implementation's specific parameters from the platform configuration. An empty
     * {@code Optional} means the implementation has none, or does not support loading them from
     * configuration.
     *
     * @param config the platform configuration to read from
     * @return the specific parameters read from the platform configuration
     */
    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads this implementation's specific parameters from a map of property names to values. An
     * empty {@code Optional} means the implementation has none, or does not support loading them
     * this way.
     *
     * @param properties property names and values
     * @return the specific parameters read from the map
     */
    default Optional<Extension<StateEstimationParameters>> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Writes this implementation's specific parameters out as property names and values. Empty when
     * the implementation has no specific parameters.
     *
     * @param extension this implementation's specific parameters
     * @return the property names and values
     */
    default Map<String, String> createMapFromSpecificParameters(Extension<StateEstimationParameters> extension) {
        return Collections.emptyMap();
    }

    /**
     * Applies the properties that are present to this implementation's specific parameters, leaving
     * the others as they are.
     *
     * @param extension this implementation's specific parameters
     * @param properties property names and values
     */
    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, Map<String, String> properties) {
        // nothing to update when the implementation has no specific parameters
    }

    /**
     * Applies the platform configuration to this implementation's specific parameters, leaving the
     * settings it does not mention as they are.
     *
     * @param extension this implementation's specific parameters
     * @param config the platform configuration to read from
     */
    default void updateSpecificParameters(Extension<StateEstimationParameters> extension, PlatformConfig config) {
        // nothing to update when the implementation has no specific parameters
    }

    /**
     * This implementation's specific parameters with the default values the implementation itself
     * declares, before any configuration is applied.
     */
    default List<Parameter> getRawSpecificParameters() {
        return Collections.emptyList();
    }

    /**
     * This implementation's specific parameters with the defaults from the default platform
     * configuration applied.
     */
    default List<Parameter> getSpecificParameters() {
        return getSpecificParameters(PlatformConfig.defaultConfig());
    }

    /**
     * This implementation's specific parameters with the defaults from the given platform
     * configuration applied.
     *
     * @param config the platform configuration to read from
     * @return this implementation's specific parameters with the defaults from the given platform configuration applied
     */
    default List<Parameter> getSpecificParameters(PlatformConfig config) {
        return ConfiguredParameter.load(getRawSpecificParameters(), getModuleConfig(config).orElse(null));
    }

    /**
     * The configuration module holding this implementation's specific parameters. An empty
     * {@code Optional} means the configuration does not define one.
     *
     * @param config the platform configuration to read from
     * @return the configuration module holding this implementation's specific parameters
     */
    default Optional<ModuleConfig> getModuleConfig(PlatformConfig config) {
        return config.getOptionalModuleConfig(getName() + "-state-estimation-parameters");
    }

    /**
     * Whether this implementation can honor the given parameters. An implementation that cannot
     * returns false instead of failing later in the run.
     *
     * @param runParameters the run parameters
     * @return true if the implementation can honor the parameters, false otherwise
     */
    default boolean checkParameters(StateEstimationRunParameters runParameters) {
        return true;
    }
}
