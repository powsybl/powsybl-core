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
import com.powsybl.security.results.ThreeWindingsTransformerResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class ThreeWindingsTransformerResultDeserializer extends StdDeserializer<ThreeWindingsTransformerResult> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));

    public ThreeWindingsTransformerResultDeserializer() {
        super(ThreeWindingsTransformerResult.class);
    }

    private static class ParsingContext {
        String threeWindingsTransformerId;

        double p1 = Double.NaN;

        double q1 = Double.NaN;

        double i1 = Double.NaN;

        double p2 = Double.NaN;

        double q2 = Double.NaN;

        double i2 = Double.NaN;

        double p3 = Double.NaN;

        double q3 = Double.NaN;

        double i3 = Double.NaN;

        List<Extension<ThreeWindingsTransformerResult>> extensions = Collections.emptyList();
    }

    @Override
    public ThreeWindingsTransformerResult deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(jsonParser, name -> {
            switch (name) {
                case "threeWindingsTransformerId":
                    context.threeWindingsTransformerId = jsonParser.nextTextValue();
                    return true;
                case "p1":
                    jsonParser.nextToken();
                    context.p1 = jsonParser.getValueAsDouble();
                    return true;
                case "q1":
                    jsonParser.nextToken();
                    context.q1 = jsonParser.getValueAsDouble();
                    return true;
                case "i1":
                    jsonParser.nextToken();
                    context.i1 = jsonParser.getValueAsDouble();
                    return true;
                case "p2":
                    jsonParser.nextToken();
                    context.p2 = jsonParser.getValueAsDouble();
                    return true;
                case "q2":
                    jsonParser.nextToken();
                    context.q2 = jsonParser.getValueAsDouble();
                    return true;
                case "i2":
                    jsonParser.nextToken();
                    context.i2 = jsonParser.getValueAsDouble();
                    return true;
                case "p3":
                    jsonParser.nextToken();
                    context.p3 = jsonParser.getValueAsDouble();
                    return true;
                case "q3":
                    jsonParser.nextToken();
                    context.q3 = jsonParser.getValueAsDouble();
                    return true;
                case "i3":
                    jsonParser.nextToken();
                    context.i3 = jsonParser.getValueAsDouble();
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
        var transfoResult = new ThreeWindingsTransformerResult(context.threeWindingsTransformerId,
                                                               context.p1, context.q1, context.i1,
                                                               context.p2, context.q2, context.i2,
                                                               context.p3, context.q3, context.i3);
        SUPPLIER.get().addExtensions(transfoResult, context.extensions);
        return transfoResult;
    }
}
