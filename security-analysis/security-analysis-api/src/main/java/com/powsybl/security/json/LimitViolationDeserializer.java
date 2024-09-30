/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.base.Suppliers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionJsonSerializer;
import com.powsybl.commons.extensions.ExtensionProviders;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.security.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LimitViolationDeserializer extends StdDeserializer<LimitViolation> {

    private static final Supplier<ExtensionProviders<ExtensionJsonSerializer>> SUPPLIER =
        Suppliers.memoize(() -> ExtensionProviders.createProvider(ExtensionJsonSerializer.class, "security-analysis"));
    public static final String SHORT_CIRCUIT_RESULT_VERSION_ATTRIBUTE = "shortCircuitResultVersion";
    private static final String CONTEXT_NAME = "limit-violation";

    public LimitViolationDeserializer() {
        super(LimitViolation.class);
    }

    @Override
    public LimitViolation deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String securityResultVersion = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        String shortCircuitResultVersion = JsonUtil.getSourceVersion(deserializationContext, SHORT_CIRCUIT_RESULT_VERSION_ATTRIBUTE);
        String subjectId = null;
        String subjectName = null;
        LimitViolationType limitType = null;
        String limitName = null;
        int acceptableDuration = Integer.MAX_VALUE;
        double limit = Double.NaN;
        double limitReduction = Double.NaN;
        double value = Double.NaN;
        ThreeSides side = null;
        ViolationLocation violationLocation = null;

        List<Extension<LimitViolation>> extensions = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "subjectId":
                    subjectId = parser.nextTextValue();
                    break;

                case "subjectName":
                    subjectName = parser.nextTextValue();
                    break;

                case "limitType":
                    parser.nextToken();
                    limitType = JsonUtil.readValue(deserializationContext, parser, LimitViolationType.class);
                    break;

                case "limitName":
                    limitName = parser.nextTextValue();
                    break;

                case "acceptableDuration":
                    parser.nextToken();
                    acceptableDuration = parser.readValueAs(Integer.class);
                    break;

                case "limit":
                    parser.nextToken();
                    limit = parser.readValueAs(Double.class);
                    break;

                case "limitReduction":
                    parser.nextToken();
                    limitReduction = parser.readValueAs(Float.class);
                    break;

                case "value":
                    parser.nextToken();
                    value = parser.readValueAs(Double.class);
                    break;

                case "side":
                    parser.nextToken();
                    side = JsonUtil.readValue(deserializationContext, parser, ThreeSides.class);
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.readExtensions(parser, deserializationContext, SUPPLIER.get());
                    break;

                case "violationLocation":
                    checkVersions(securityResultVersion, shortCircuitResultVersion, "violationLocation");
                    parser.nextToken();
                    violationLocation = JsonUtil.readValue(deserializationContext, parser, ViolationLocation.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        LimitViolation violation = new LimitViolation(subjectId, subjectName, limitType, limitName, acceptableDuration,
            limit, limitReduction, value, side, violationLocation);
        SUPPLIER.get().addExtensions(violation, extensions);

        return violation;
    }

    private void checkVersions(String securityResultVersion, String shortCircuitResultVersion, String fieldName) {
        if (securityResultVersion != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, fieldName,
                securityResultVersion, "1.7");
        }
        if (shortCircuitResultVersion != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, fieldName,
                shortCircuitResultVersion, "1.3");
        }
    }
}
