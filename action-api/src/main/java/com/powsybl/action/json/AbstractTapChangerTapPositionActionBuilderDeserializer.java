/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.action.AbstractTapChangerTapPositionActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public abstract class AbstractTapChangerTapPositionActionBuilderDeserializer<T extends AbstractTapChangerTapPositionActionBuilder> extends StdDeserializer<T> {

    protected AbstractTapChangerTapPositionActionBuilderDeserializer(Class<T> vc) {
        super(vc);
    }

    protected boolean deserializeCommonAttributes(JsonParser jsonParser, T builder, String name, String version) throws IOException {
        switch (name) {
            case "id":
                builder.withId(jsonParser.nextTextValue());
                return true;
            case "transformerId":
                builder.withTransformerId(jsonParser.nextTextValue());
                return true;
            case "value":
                JsonUtil.assertLessThanOrEqualToReferenceVersion("actions", "Tag: value", version, "1.0");
                jsonParser.nextToken();
                builder.withTapPosition(jsonParser.getValueAsInt());
                return true;
            case "tapPosition":
                JsonUtil.assertGreaterOrEqualThanReferenceVersion("actions", "Tag: tapPosition", version, "1.1");
                jsonParser.nextToken();
                builder.withTapPosition(jsonParser.getValueAsInt());
                return true;
            case "relativeValue":
                jsonParser.nextToken();
                builder.withRelativeValue(jsonParser.getValueAsBoolean());
                return true;
            case "side":
                builder.withSide(ThreeSides.valueOf(jsonParser.nextTextValue()));
                return true;
            default:
                return false;
        }
    }

    protected void checkFields(T builder, JsonParser jsonParser) throws JsonMappingException {
        if (builder.isRelativeValue() == null) {
            throw JsonMappingException.from(jsonParser, "for phase tap changer tap position action relative value field can't be null");
        }
    }
}
