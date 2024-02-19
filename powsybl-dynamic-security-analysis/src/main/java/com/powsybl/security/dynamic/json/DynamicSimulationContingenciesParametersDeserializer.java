/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.dynamicsimulation.json.AbstractDynamicSimulationParametersDeserializer;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.security.dynamic.DynamicSimulationContingenciesParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationContingenciesParametersDeserializer extends AbstractDynamicSimulationParametersDeserializer<DynamicSimulationContingenciesParameters> {

    DynamicSimulationContingenciesParametersDeserializer() {
        super(DynamicSimulationContingenciesParameters.class);
    }

    @Override
    public DynamicSimulationContingenciesParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new DynamicSimulationContingenciesParameters());
    }

    @Override
    public DynamicSimulationContingenciesParameters deserialize(JsonParser parser, DeserializationContext deserializationContext,
                                                                DynamicSimulationContingenciesParameters parameters) throws IOException {

        List<Extension<DynamicSimulationContingenciesParameters>> extensions = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            boolean found = deserializeCommons(parser, deserializationContext, parameters, extensions);
            if (!found) {
                if (parser.getCurrentName().equalsIgnoreCase("contingenciesStartTime")) {
                    parser.nextToken();
                    parameters.setContingenciesStartTime(parser.readValueAs(Integer.class));
                } else {
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
                }
            }
        }
        JsonDynamicSimulationParameters.getExtensionSerializers().addExtensions(parameters, extensions);
        return parameters;
    }
}
