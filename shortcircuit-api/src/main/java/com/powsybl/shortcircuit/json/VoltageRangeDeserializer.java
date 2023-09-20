/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.shortcircuit.VoltageRange;

import java.io.IOException;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class VoltageRangeDeserializer extends StdDeserializer<VoltageRange> {

    public VoltageRangeDeserializer() {
        super(VoltageRange.class);
    }

    public VoltageRange deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        Double minimumVoltage = Double.NaN;
        Double maximumVoltage = Double.NaN;
        Double coefficient = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "minimumNominalVoltage" -> {
                    parser.nextToken();
                    minimumVoltage = parser.readValueAs(Double.class);
                }
                case "maximumNominalVoltage" -> {
                    parser.nextToken();
                    maximumVoltage = parser.readValueAs(Double.class);
                }
                case "voltageRangeCoefficient" -> {
                    parser.nextToken();
                    coefficient = parser.readValueAs(Double.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new VoltageRange(minimumVoltage, maximumVoltage, coefficient);
    }
}
