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
import com.powsybl.shortcircuit.ConfiguredInitialVoltageProfileCoefficient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class ConfiguredInitialVoltageProfileCoefficientDeserializer {

    public List<ConfiguredInitialVoltageProfileCoefficient> deserialize(JsonParser parser) throws IOException {
        List<ConfiguredInitialVoltageProfileCoefficient> coefficientList = new ArrayList<>();
        parser.nextToken();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            Double minimumVoltage = Double.NaN;
            Double maximumVoltage = Double.NaN;
            Double coefficient = Double.NaN;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.getCurrentName()) {
                    case "minimumVoltage":
                        parser.nextToken();
                        minimumVoltage = parser.readValueAs(Double.class);
                        break;
                    case "maximumVoltage":
                        parser.nextToken();
                        maximumVoltage = parser.readValueAs(Double.class);
                        break;
                    case "voltageRangeCoefficient":
                        parser.nextToken();
                        coefficient = parser.readValueAs(Double.class);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
                }
            }
            coefficientList.add(new ConfiguredInitialVoltageProfileCoefficient(minimumVoltage, maximumVoltage, coefficient));
        }
        return coefficientList;
    }
}
