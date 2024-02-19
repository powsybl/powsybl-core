/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationParametersDeserializer extends AbstractDynamicSimulationParametersDeserializer<DynamicSimulationParameters> {

    DynamicSimulationParametersDeserializer() {
        super(DynamicSimulationParameters.class);
    }

    @Override
    public DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new DynamicSimulationParameters());
    }

    @Override
    public DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext deserializationContext,
        DynamicSimulationParameters parameters) throws IOException {

        List<Extension<DynamicSimulationParameters>> extensions = new ArrayList<>();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            boolean found = deserializeCommons(parser, deserializationContext, parameters, extensions);
            if (!found) {
                throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        JsonDynamicSimulationParameters.getExtensionSerializers().addExtensions(parameters, extensions);
        return parameters;
    }
}
