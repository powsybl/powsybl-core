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
import java.util.HashSet;

import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.ONESHOT;
import static com.powsybl.iidm.modification.scalable.ScalingParameters.Priority.RESPECT_OF_VOLUME_ASKED;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ScalingParametersDeserializer extends StdDeserializer<ScalingParameters> {

    private static final String CONTEXT_NAME = "ScalingParameters";

    ScalingParametersDeserializer() {
        super(ScalingParameters.class);
    }

    @Override
    public ScalingParameters deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return deserialize(parser, context, new ScalingParameters());
    }

    @Override
    public ScalingParameters deserialize(JsonParser parser, DeserializationContext context, ScalingParameters parameters) throws IOException {
        String version = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> {
                    parser.nextToken(); // do nothing
                    version = parser.getValueAsString();
                }
                case "scalingConvention" -> {
                    parser.nextToken();
                    parameters.setScalingConvention(JsonUtil.readValue(context, parser, Scalable.ScalingConvention.class));
                }
                case "constantPowerFactor" -> {
                    parser.nextToken();
                    parameters.setConstantPowerFactor(parser.readValueAs(Boolean.class));
                }
                case "reconnect" -> {
                    parser.nextToken();
                    parameters.setReconnect(parser.readValueAs(Boolean.class));
                }
                case "iterative" -> {
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: iterative", version, "1.1");
                    parser.nextToken();
                    parameters.setPriority(Boolean.TRUE.equals(parser.readValueAs(Boolean.class)) ? RESPECT_OF_VOLUME_ASKED : ONESHOT);
                }
                case "priority" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: priority", version, "1.1");
                    parser.nextToken();
                    parameters.setPriority(JsonUtil.readValue(context, parser, ScalingParameters.Priority.class));
                }
                case "ignoredInjectionIds" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: ignoredInjectionIds", version, "1.2");
                    parameters.setIgnoredInjectionIds(new HashSet<>(JsonUtil.parseStringArray(parser)));
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        return parameters;
    }
}
