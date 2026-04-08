/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.ConnectivityResult;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConnectivityResultDeserializer extends StdDeserializer<ConnectivityResult> {

    public ConnectivityResultDeserializer() {
        super(ConnectivityResult.class);
    }

    @Override
    public ConnectivityResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {

        int createdSynchronousComponentCount = 0;
        int createdConnectedComponentCount = 0;
        double disconnectedLoadActivePower = 0.0;
        double disconnectedGenerationActivePower = 0.0;
        Set<String> disconnectedElements = Collections.emptySet();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "createdSynchronousComponentCount":
                    parser.nextToken();
                    createdSynchronousComponentCount = parser.getIntValue();
                    break;
                case "createdConnectedComponentCount":
                    parser.nextToken();
                    createdConnectedComponentCount = parser.getIntValue();
                    break;
                case "disconnectedLoadActivePower":
                    parser.nextToken();
                    disconnectedLoadActivePower = parser.getDoubleValue();
                    break;
                case "disconnectedGenerationActivePower":
                    parser.nextToken();
                    disconnectedGenerationActivePower = parser.getDoubleValue();
                    break;
                case "disconnectedElements":
                    parser.nextToken();
                    disconnectedElements = JsonUtil.readSet(deserializationContext, parser, String.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        return new ConnectivityResult(createdSynchronousComponentCount, createdConnectedComponentCount,
                disconnectedLoadActivePower, disconnectedGenerationActivePower, disconnectedElements);
    }
}


