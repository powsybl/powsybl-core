/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.LoadAction;
import com.powsybl.action.LoadActionBuilder;
import com.powsybl.action.RatioTapChangerTapPositionAction;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionBuilderBuilderDeserializer extends AbstractLoadActionBuilderDeserializer<LoadActionBuilder> {

    public LoadActionBuilderBuilderDeserializer() {
        super(LoadActionBuilder.class);
    }

    @Override
    public LoadActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        LoadActionBuilder builder = new LoadActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, builder, name);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextStringValue();
                if (!LoadAction.NAME.equals(type)) {
                    throw DatabindException.from(jsonParser, "Expected type :" + RatioTapChangerTapPositionAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        return builder;
    }
}
