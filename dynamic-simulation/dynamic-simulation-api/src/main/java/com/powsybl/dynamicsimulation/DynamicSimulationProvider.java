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
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public interface DynamicSimulationProvider extends Versionable, PlatformConfigNamedProvider {

    static List<DynamicSimulationProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(DynamicSimulationProvider.class, DynamicSimulationProvider.class.getClassLoader()));
    }

    CompletableFuture<DynamicSimulationResult> run(Network network, DynamicModelsSupplier dynamicModelsSupplier, EventModelsSupplier eventModelsSupplier,
                                                   OutputVariablesSupplier outputVariablesSupplier, String workingVariantId, ComputationManager computationManager, DynamicSimulationParameters parameters, ReportNode reportNode);

    /**
     * Get specific parameters class.
     *
     * @return The specific parameters class
     */
    Optional<Class<? extends Extension<DynamicSimulationParameters>>> getSpecificParametersClass();

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<DynamicSimulationParameters, MyParametersExtension>}
     * where {@code MyParametersExtension} is the specific parameters class.
     *
     * @return The serializer for implementation-specific parameters.
     */
    Optional<ExtensionJsonSerializer> getSpecificParametersSerializer();

    /**
     * Reads implementation-specific parameters from platform config, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from platform config.
     */
    Optional<Extension<DynamicSimulationParameters>> loadSpecificParameters(PlatformConfig config);

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     *
     * @return The specific parameters read from Map.
     */
    Optional<Extension<DynamicSimulationParameters>> loadSpecificParameters(Map<String, String> properties);

    /**
     * Create a `Map` of parameter name / `String` value from implementation-specific parameters.
     * If the implementation does not have any specific parameters, `Map` is empty.
     *
     * @param extension the specific parameters
     * @return A `Map` of parameter name / `String` value
     */
    Map<String, String> createMapFromSpecificParameters(Extension<DynamicSimulationParameters> extension);

    /**
     * Updates implementation-specific parameters from a Map.
     */
    void updateSpecificParameters(Extension<DynamicSimulationParameters> extension, Map<String, String> properties);

    /**
     * Get the parameters of the parameters extension associated with this provider.
     *
     * @return the parameters of the parameters extension associated with this provider.
     */
    List<Parameter> getSpecificParameters();

    /**
     * Retrieves the parameters of the extension associated with this provider,
     * incorporating any overrides from the PlatformConfig.
     *
     * @return The parameters of the associated extension with overrides applied from PlatformConfig.
     */
    default List<Parameter> getSpecificParameters(PlatformConfig platformConfigConfig) {
        return ConfiguredParameter.load(getSpecificParameters(), getModuleConfig(platformConfigConfig).orElse(null));
    }

    /**
     * Retrieves the configuration of the specific module for this provider from the provided PlatformConfig.
     *
     * @param platformConfig The platform configuration containing potential module configurations.
     * @return An Optional containing the ModuleConfig if present, otherwise an empty Optional.
     */
    default Optional<ModuleConfig> getModuleConfig(PlatformConfig platformConfig) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
