/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.powsybl.action.DanglingLineAction;
import com.powsybl.action.DanglingLineActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class DanglingLineActionBuilderBuilderDeserializer extends AbstractLoadActionBuilderDeserializer<DanglingLineActionBuilder> {

    public DanglingLineActionBuilderBuilderDeserializer() {
        super(DanglingLineActionBuilder.class);
    }

    @Override
    public DanglingLineActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        DanglingLineActionBuilder builder = new DanglingLineActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, builder, name);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextStringValue();
                if (!DanglingLineAction.NAME.equals(type)) {
                    throw DatabindException.from(jsonParser, "Expected type :" + DanglingLineAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        return builder;
    }
}
