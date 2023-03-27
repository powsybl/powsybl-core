/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.scalable.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.modification.scalable.Scalable;
import com.powsybl.iidm.modification.scalable.ScalingParameters;

import java.io.IOException;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class ScalingParametersDeserializer extends StdDeserializer<ScalingParameters> {

    ScalingParametersDeserializer() {
        super(ScalingParameters.class);
    }

    @Override
    public ScalingParameters deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return deserialize(parser, context, new ScalingParameters());
    }

    @Override
    public ScalingParameters deserialize(JsonParser parser, DeserializationContext context, ScalingParameters parameters) throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // do nothing
                    break;

                case "scalingConvention":
                    parser.nextToken();
                    parameters.setScalingConvention(JsonUtil.readValue(context, parser, Scalable.ScalingConvention.class));
                    break;

                case "constantPowerFactor":
                    parser.nextToken();
                    parameters.setConstantPowerFactor(parser.readValueAs(Boolean.class));
                    break;

                case "reconnect":
                    parser.nextToken();
                    parameters.setReconnect(parser.readValueAs(Boolean.class));
                    break;

                case "iterative":
                    parser.nextToken();
                    parameters.setIterative(parser.readValueAs(Boolean.class));
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return parameters;
    }
}
