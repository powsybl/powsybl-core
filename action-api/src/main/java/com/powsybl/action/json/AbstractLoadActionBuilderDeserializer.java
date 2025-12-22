/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.AbstractLoadActionBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionBuilderDeserializer<T extends AbstractLoadActionBuilder> extends StdDeserializer<T> {

    protected AbstractLoadActionBuilderDeserializer(Class<T> vc) {
        super(vc);
    }

    protected boolean deserializeCommonAttributes(JsonParser jsonParser, T builder, String name) throws JacksonException {
        switch (name) {
            case "id":
                builder.withId(jsonParser.nextStringValue());
                return true;
            case "loadId", "danglingLineId":
                builder.withNetworkElementId(jsonParser.nextStringValue());
                return true;
            case "relativeValue":
                jsonParser.nextToken();
                builder.withRelativeValue(jsonParser.getValueAsBoolean());
                return true;
            case "activePowerValue":
                jsonParser.nextToken();
                builder.withActivePowerValue(jsonParser.getValueAsDouble());
                return true;
            case "reactivePowerValue":
                jsonParser.nextToken();
                builder.withReactivePowerValue(jsonParser.getValueAsDouble());
                return true;
            default:
                return false;
        }
    }
}
