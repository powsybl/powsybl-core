/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.LimitViolation;
import com.powsybl.shortcircuit.*;

import java.io.IOException;
import java.time.Duration;
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
        Fault fault = null;
        double shortCircuitPower = Double.NaN;
        Duration timeConstant = null;
        List<FeederResult> feederResults = Collections.emptyList();
        List<LimitViolation> limitViolations = Collections.emptyList();
        List<Extension<FaultResult>> extensions = Collections.emptyList();
        FortescueValue current = null;
        FortescueValue voltage = null;
        List<ShortCircuitBusResults> shortCircuitBusResults = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "fault":
                    parser.nextToken();
                    fault = parser.readValueAs(Fault.class);
                    break;

                case "shortCircuitPower":
                    parser.nextToken();
                    shortCircuitPower = parser.readValueAs(Double.class);
                    break;

                case "timeConstant":
                    parser.nextToken();
                    timeConstant = Duration.parse(parser.readValueAs(String.class));
                    break;

                case "feederResult":
                    parser.nextToken();
                    feederResults = parser.readValueAs(new TypeReference<List<FeederResult>>() { });
                    break;

                case "limitViolations":
                    parser.nextToken();
                    limitViolations = parser.readValueAs(new TypeReference<List<LimitViolation>>() { });
                    break;

                case "current":
                    parser.nextToken();
                    current = parser.readValueAs(FortescueValue.class);
                    break;

                case "voltage":
                    parser.nextToken();
                    voltage = parser.readValueAs(FortescueValue.class);
                    break;

                case "shortCircuitBusResults":
                    parser.nextToken();
                    shortCircuitBusResults = parser.readValueAs(new TypeReference<List<ShortCircuitBusResults>>() { });
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        FaultResult faultResult = new FaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant);
        SUPPLIER.get().addExtensions(faultResult, extensions);

        return faultResult;
    }
}
