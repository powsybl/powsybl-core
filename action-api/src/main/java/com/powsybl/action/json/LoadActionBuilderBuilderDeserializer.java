/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.action.LoadAction;
import com.powsybl.action.LoadActionBuilder;
import com.powsybl.action.RatioTapChangerTapPositionAction;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionBuilderBuilderDeserializer extends AbstractLoadActionBuilderDeserializer<LoadActionBuilder> {

    public LoadActionBuilderBuilderDeserializer() {
        super(LoadActionBuilder.class);
    }

    @Override
    public LoadActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        LoadActionBuilder builder = new LoadActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, builder, name);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextTextValue();
                if (!LoadAction.NAME.equals(type)) {
                    throw JsonMappingException.from(jsonParser, "Expected type :" + RatioTapChangerTapPositionAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        return builder;
    }
}
