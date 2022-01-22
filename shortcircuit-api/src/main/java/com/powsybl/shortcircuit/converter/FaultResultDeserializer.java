/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FaultResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
class FaultResultDeserializer extends StdDeserializer<FaultResult> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "short-circuit-analysis"));

    FaultResultDeserializer() {
        super(FaultResult.class);
    }

    @Override
    public FaultResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String id = "";
        float threePhaseFaultCurrent = 0;
        List<Extension<FaultResult>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "id":
                    parser.nextToken();
                    id = parser.readValueAs(String.class);
                    break;

                case "threePhaseFaultCurrent":
                    parser.nextToken();
                    threePhaseFaultCurrent = parser.readValueAs(Float.class);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        FaultResult faultResult = new FaultResult(id, threePhaseFaultCurrent);
        SUPPLIER.get().addExtensions(faultResult, extensions);

        return faultResult;
    }
}
