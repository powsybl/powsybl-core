/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.AbstractDynamicSimulationParameters;

import java.io.IOException;
import java.util.List;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractDynamicSimulationParametersDeserializer<T extends AbstractDynamicSimulationParameters<T>> extends StdDeserializer<T> {

    protected AbstractDynamicSimulationParametersDeserializer(Class<T> ds) {
        super(ds);
    }

    protected boolean deserializeCommons(JsonParser parser, DeserializationContext deserializationContext,
                                                   T parameters, List<Extension<T>> extensions) throws IOException {

        switch (parser.getCurrentName()) {
            case "version":
                parser.nextToken();
                return true;
            case "startTime":
                parser.nextToken();
                parameters.setStartTime(parser.readValueAs(Integer.class));
                return true;
            case "stopTime":
                parser.nextToken();
                parameters.setStopTime(parser.readValueAs(Integer.class));
                return true;
            case "extensions":
                parser.nextToken();
                extensions.addAll(JsonUtil.readExtensions(parser, deserializationContext, JsonDynamicSimulationParameters.getExtensionSerializers()));
                return true;
            default:
                return false;
        }
    }

}
