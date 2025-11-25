/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.loadflow;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Didier Vidal {@literal <didier.vidal_externe at rte-france.com>}
 */
public final class LoadFlowProviderUtil {

    private LoadFlowProviderUtil() {

    }

    private static Object getValueFromString(ParameterType type, String value) {
        if (value == null || value.isEmpty()) {
            return type == ParameterType.STRING ? value : null;
        }
        return switch (type) {
            case DOUBLE -> Double.parseDouble(value);
            case STRING -> value;
            case BOOLEAN -> Boolean.parseBoolean(value);
            case INTEGER -> Integer.parseInt(value);
            case STRING_LIST -> Arrays.asList(value.trim().split("[:,]"));
        };
    }

    /**
     * Retrieves the parameters of the extension associated with this provider,
     * incorporating any overrides from the default loadFlowParametersLoader if it exists.
     * The platform config is used to determine the default loadflow parameter loader to use in case
     * the class path contains more than one.
     *
     * @return The parameters of the associated extension with overrides applied from default loadflow parameter loader.
     */
    public static List<Parameter> getSpecificParameters(LoadFlowProvider provider, PlatformConfig platformConfig) {
        return LoadFlowParameters.getDefaultLoadFlowParameterLoader(platformConfig)
                .map(loader -> getSpecificParameters(provider, loader))
                .orElseGet(provider::getRawSpecificParameters);
    }

    /**
     * Retrieves the parameters of the extension associated with this provider,
     * incorporating any overrides from the loadFlowParametersLoader.
     *
     * @return The parameters of the associated extension with overrides applied from default loadflow parameter loader.
     */
    public static List<Parameter> getSpecificParameters(LoadFlowProvider provider, LoadFlowDefaultParametersLoader loadFlowDefaultParametersLoader) {
        List<Parameter> result = provider.getRawSpecificParameters();
        if (!result.isEmpty() && provider.getSpecificParametersClass().isPresent()) {
            LoadFlowParameters loadFlowParameters = JsonLoadFlowParameters.read(loadFlowDefaultParametersLoader.loadDefaultParametersFromFile());
            Extension<LoadFlowParameters> ext = loadFlowParameters.getExtension(provider.getSpecificParametersClass().orElseThrow());
            // default parameter loader may not contain the extension in the JSON file
            if (ext != null) {
                Map<String, String> values = provider.createMapFromSpecificParameters(ext);
                result = result.stream()
                        .map(p -> new Parameter(p.getName(), p.getType(), p.getDescription(),
                                getValueFromString(p.getType(), values.get(p.getName())), p.getPossibleValues(),
                                p.getScope(), p.getCategoryKey()))
                        .toList();
            }

        }
        return result;
    }
}
