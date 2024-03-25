/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.results.BusResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class BusResultDeserializer extends StdDeserializer<BusResult> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public BusResultDeserializer() {
        super(BusResult.class);
    }

    private static class ParsingContext {
        String voltageLevelId;

        String busId;

        double v = Double.NaN;

        double angle = Double.NaN;

        List<Extension<BusResult>> extensions = Collections.emptyList();
    }

    @Override
    public BusResult deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(jsonParser, name -> {
            switch (name) {
                case "voltageLevelId":
                    context.voltageLevelId = jsonParser.nextTextValue();
                    return true;
                case "busId":
                    context.busId = jsonParser.nextTextValue();
                    return true;
                case "v":
                    jsonParser.nextToken();
                    context.v = jsonParser.getValueAsDouble();
                    return true;
                case "angle":
                    jsonParser.nextToken();
                    context.angle = jsonParser.getValueAsDouble();
                    return true;
                case "extensions":
                    jsonParser.nextToken();
                    context.extensions = JsonUtil.readExtensions(jsonParser, deserializationContext, SUPPLIER.get());
                    break;
                default:
                    return false;
            }
            return false;
        });
        BusResult busResult = new BusResult(context.voltageLevelId, context.busId, context.v, context.angle);
        SUPPLIER.get().addExtensions(busResult, context.extensions);
        return busResult;
    }
}
