/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.shortcircuit.FortescueValue;

import java.io.IOException;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class FortescueValuesDeserializer extends StdDeserializer<FortescueValue> {

    FortescueValuesDeserializer() {
        super(FortescueValue.class);
    }

    @Override
    public FortescueValue deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        // Fortescue components.
        double directMagnitude = Double.NaN;
        double zeroMagnitude = Double.NaN;
        double inverseMagnitude = Double.NaN;
        double directPhase = Double.NaN;
        double zeroPhase = Double.NaN;
        double inversePhase = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "directMagnitude" -> {
                    parser.nextToken();
                    directMagnitude = parser.readValueAs(Double.class);
                }
                case "zeroMagnitude" -> {
                    parser.nextToken();
                    zeroMagnitude = parser.readValueAs(Double.class);
                }
                case "inverseMagnitude" -> {
                    parser.nextToken();
                    inverseMagnitude = parser.readValueAs(Double.class);
                }
                case "directAngle" -> {
                    parser.nextToken();
                    directPhase = parser.readValueAs(Double.class);
                }
                case "zeroAngle" -> {
                    parser.nextToken();
                    zeroPhase = parser.readValueAs(Double.class);
                }
                case "inverseAngle" -> {
                    parser.nextToken();
                    inversePhase = parser.readValueAs(Double.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new FortescueValue(directMagnitude, zeroMagnitude, inverseMagnitude, directPhase, zeroPhase, inversePhase);
    }
}
