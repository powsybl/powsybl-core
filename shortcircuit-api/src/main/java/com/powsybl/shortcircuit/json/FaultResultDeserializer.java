/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
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
import java.util.function.Supplier;

/**
 * @author Teofil-Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
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
            double currentMagnitude = Double.NaN;
            double voltageMagnitude = Double.NaN;
            List<ShortCircuitBusResults> shortCircuitBusResults = Collections.emptyList();

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.getCurrentName()) {
                    case "fault" -> {
                        parser.nextToken();
                        fault = JsonUtil.readValue(deserializationContext, parser, Fault.class);
                    }
                    case "shortCircuitPower" -> {
                        parser.nextToken();
                        shortCircuitPower = parser.readValueAs(Double.class);
                    }
                    case "timeConstant" -> {
                        parser.nextToken();
                        timeConstant = Duration.parse(parser.readValueAs(String.class));
                    }
                    case "feederResult" -> {
                        parser.nextToken();
                        feederResults = JsonUtil.readList(deserializationContext, parser, FeederResult.class);
                    }
                    case "limitViolations" -> {
                        parser.nextToken();
                        limitViolations = JsonUtil.readList(deserializationContext, parser, LimitViolation.class);
                    }
                    case "current" -> {
                        parser.nextToken();
                        current = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                    }
                    case "voltage" -> {
                        parser.nextToken();
                        voltage = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                    }
                    case "currentMagnitude" -> {
                        parser.nextToken();
                        currentMagnitude = parser.readValueAs(Double.class);
                    }
                    case "voltageMagnitude" -> {
                        parser.nextToken();
                        voltageMagnitude = parser.readValueAs(Double.class);
                    }
                    case "shortCircuitBusResults" ->
                            shortCircuitBusResults = new ShortCircuitBusResultsDeserializer().deserialize(parser, version);
                    case "status" -> {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: " + parser.getCurrentName(), version, "1.1");
                        parser.nextToken();
                        status = FaultResult.Status.valueOf(parser.getValueAsString());
                    }
                    case "extensions" -> {
                        parser.nextToken();
                        extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    }
                    default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
                }
            }

            FaultResult faultResult;
            if (status == null) {
                JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "No status", version, "1.0");
                if (current == null && Double.isNaN(currentMagnitude)) {
                    faultResult = new FailedFaultResult(fault, FaultResult.Status.FAILURE);
                } else {
                    if (!Double.isNaN(currentMagnitude)) {
                        faultResult = new MagnitudeFaultResult(fault, shortCircuitPower, feederResults, limitViolations, currentMagnitude, voltageMagnitude, shortCircuitBusResults, timeConstant, FaultResult.Status.SUCCESS);
                    } else {
                        faultResult = new FortescueFaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, FaultResult.Status.SUCCESS);
                    }
                }
            } else {
                if (status == FaultResult.Status.FAILURE) {
                    faultResult = new FailedFaultResult(fault, status);
                } else if (!Double.isNaN(currentMagnitude)) {
                    faultResult = new MagnitudeFaultResult(fault, shortCircuitPower, feederResults, limitViolations, currentMagnitude, voltageMagnitude, shortCircuitBusResults, timeConstant, status);
                } else {
                    faultResult = new FortescueFaultResult(fault, shortCircuitPower, feederResults, limitViolations, current, voltage, shortCircuitBusResults, timeConstant, status);
                }
            }

            SUPPLIER.get().addExtensions(faultResult, extensions);

            faultResults.add(faultResult);
        }
        return faultResults;
    }
}
