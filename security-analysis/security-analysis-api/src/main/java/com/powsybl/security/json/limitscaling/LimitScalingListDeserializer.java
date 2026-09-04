/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitscaling;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.limitscaling.LimitScaling;
import com.powsybl.security.limitscaling.LimitScalingList;

import java.io.IOException;
import java.util.List;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitScalingListDeserializer extends StdDeserializer<LimitScalingList> {

    private static final String CONTEXT_NAME = "limit-scaling-list";

    public LimitScalingListDeserializer() {
        super(LimitScalingList.class);
    }

    private static final class ParsingContext {
        String version;
        List<LimitScaling> limitScalings;
    }

    @Override
    public LimitScalingList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    context.version = parser.nextTextValue();
                    break;
                case "limitScalings":
                    if (context.version != null && JsonUtil.compareVersions(context.version, "1.2") <= 0) {
                        throwInvalidFieldForVersion("limitScalings");
                    }
                    parser.nextToken(); // skip
                    context.limitScalings = JsonUtil.readList(deserializationContext, parser, LimitScaling.class);
                    break;
                // limitReductions for retro-compatibility with versions < 1.3
                case "limitReductions":
                    if (context.version != null && JsonUtil.compareVersions(context.version, "1.3") >= 0) {
                        throwInvalidFieldForVersion("limitReductions");
                    }
                    parser.nextToken(); // skip
                    context.limitScalings = JsonUtil.readList(deserializationContext, parser, LimitScaling.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        if (context.version == null) {
            throw new JsonMappingException(parser, "version is missing");
        }
        return new LimitScalingList(context.limitScalings);
    }

    private static void throwInvalidFieldForVersion(String fieldName) {
        throw new PowsyblException(String.format("%s. %s is not valid for this version", CONTEXT_NAME, fieldName));
    }
}
