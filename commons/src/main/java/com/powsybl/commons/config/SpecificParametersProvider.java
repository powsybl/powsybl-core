/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;

public interface SpecificParametersProvider<T extends Extendable<T>, E extends Extension<T>> {

    /**
     * The serializer for implementation-specific parameters, or {@link Optional#empty()} if the implementation
     * does not have any specific parameters, or does not support JSON serialization.
     *
     * <p>Note that the actual serializer type should be {@code ExtensionJsonSerializer<Parameters, MyParametersExtension>}
     * where {@code MyParametersExtension} is the specific extension of Parameters.
     */
    default Optional<ExtensionJsonSerializer> getSpecificParametersSerializer() {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from platform config, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     */
    default Optional<E> loadSpecificParameters(PlatformConfig config) {
        return Optional.empty();
    }

    /**
     * Reads implementation-specific parameters from a Map, or return {@link Optional#empty()}
     * if the implementation does not have any specific parameters, or does not support loading from config.
     */
    default Optional<E> loadSpecificParameters(Map<String, String> properties) {
        return Optional.empty();
    }

    /**
     * Get the parameters of the parameters extension associated with this provider.
     *
     * @return the parameters of the parameters extension associated with this provider.
     */
    default List<Parameter> getSpecificParameters() {
        return Collections.emptyList();
    }

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
     * Updates implementation-specific parameters from a Map.
     */
    default void updateSpecificParameters(Extension<T> extension, Map<String, String> properties) {
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
