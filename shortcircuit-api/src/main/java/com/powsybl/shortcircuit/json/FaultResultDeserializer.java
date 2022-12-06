/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Teofil-Calin BANC <teofil-calin.banc at rte-france.com>
 */
class FaultResultDeserializer {

    private static final String CONTEXT_NAME = "FaultResult";

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
            Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "short-circuit-analysis"));

    public List<FaultResult> deserialize(JsonParser parser, DeserializationContext deserializationContext, String version) throws IOException {
        List<FaultResult> faultResults = new ArrayList<>();
        parser.nextToken();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            FaultResult.Status status = null;
            Fault fault = null;
            double shortCircuitPower = Double.NaN;
            Duration timeConstant = null;
            List<FeederResult> feederResults = Collections.emptyList();
            List<LimitViolation> limitViolations = Collections.emptyList();
            List<Extension<FaultResult>> extensions = Collections.emptyList();
            FortescueValue current = null;
            FortescueValue voltage = null;
            List<ShortCircuitBusResults> shortCircuitBusResults = Collections.emptyList();
            List<SimpleShortCircuitBusResults> simpleShortCircuitBusResults = Collections.emptyList();

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.getCurrentName()) {
                    case "fault":
                        parser.nextToken();
                        fault = JsonUtil.readValue(deserializationContext, parser, Fault.class);
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
                        feederResults = JsonUtil.readList(deserializationContext, parser, FeederResult.class);
                        break;

                    case "limitViolations":
                        parser.nextToken();
                        limitViolations = JsonUtil.readList(deserializationContext, parser, LimitViolation.class);
                        break;

                    case "current":
                        parser.nextToken();
                        current = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                        break;

                    case "voltage":
                        parser.nextToken();
                        voltage = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                        break;

                    case "shortCircuitBusResults":
                        parser.nextToken();
                        shortCircuitBusResults = JsonUtil.readList(deserializationContext, parser, ShortCircuitBusResults.class);
                        break;

                    case "status":
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + parser.getCurrentName(), version, "1.1");
                        parser.nextToken();
                        status = FaultResult.Status.valueOf(parser.getValueAsString());
                        break;

                    case "simpleShortCircuitBusResults":
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: simpleShortCircuitBusResults", version, "1.1");
                        parser.nextToken();
                        simpleShortCircuitBusResults = parser.readValueAs(new TypeReference<List<SimpleShortCircuitBusResults>>() { });
                        break;

                    case "extensions":
                        parser.nextToken();
                        extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + parser.getCurrentName());
                }
            }

            FaultResult faultResult;
            if (status == null) {
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "No status", version, "1.0");
                if (current == null) {
                    faultResult = new FaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, FaultResult.Status.FAILURE, simpleShortCircuitBusResults);
                } else {
                    faultResult = new FaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, FaultResult.Status.SUCCESS, simpleShortCircuitBusResults);
                }
            } else {
                faultResult = new FaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, status, simpleShortCircuitBusResults);
            }

            SUPPLIER.get().addExtensions(faultResult, extensions);

            faultResults.add(faultResult);
        }
        return faultResults;
    }
}
