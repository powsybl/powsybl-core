/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.powsybl.action.*;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */

// TODO: Manage versioning to chang DanglingLine to BoundaryLine

public class DanglingLineActionBuilderBuilderDeserializer extends AbstractLoadActionBuilderDeserializer<BoundaryLineActionBuilder> {

    public DanglingLineActionBuilderBuilderDeserializer() {
        super(BoundaryLineActionBuilder.class);
    }

    @Override
    public BoundaryLineActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        BoundaryLineActionBuilder builder = new BoundaryLineActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            boolean found = deserializeCommonAttributes(jsonParser, builder, name);
            if (found) {
                return true;
            }
            if (name.equals("type")) {
                String type = jsonParser.nextTextValue();
                if (!BoundaryLineAction.NAME.equals(type)) {
                    throw JsonMappingException.from(jsonParser, "Expected type :" + BoundaryLineAction.NAME + " got : " + type);
                }
                return true;
            }
            return false;
        });
        return builder;
    }
}
