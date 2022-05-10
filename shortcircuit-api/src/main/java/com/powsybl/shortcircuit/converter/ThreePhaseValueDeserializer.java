/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.shortcircuit.ThreePhaseValue;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class ThreePhaseValueDeserializer extends StdDeserializer<ThreePhaseValue> {

    ThreePhaseValueDeserializer() {
        super(ThreePhaseValue.class);
    }

    @Override
    public ThreePhaseValue deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        // Results on three phases.
        double magnitude1 = Double.NaN;
        double magnitude2 = Double.NaN;
        double magnitude3 = Double.NaN;
        double phase1 = Double.NaN;
        double phase2 = Double.NaN;
        double phase3 = Double.NaN;
        // Fortescue results.
        double directMagnitude = Double.NaN;
        double zeroMagnitude = Double.NaN;
        double inverseMagnitude = Double.NaN;
        double directPhase = Double.NaN;
        double zeroPhase = Double.NaN;
        double inversePhase = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "magnitude1":
                    parser.nextToken();
                    magnitude1 = parser.readValueAs(Double.class);
                    break;
                case "magnitude2":
                    parser.nextToken();
                    magnitude2 = parser.readValueAs(Double.class);
                    break;
                case "magnitude3":
                    parser.nextToken();
                    magnitude3 = parser.readValueAs(Double.class);
                    break;
                case "phase1":
                    parser.nextToken();
                    phase1 = parser.readValueAs(Double.class);
                    break;
                case "phase2":
                    parser.nextToken();
                    phase2 = parser.readValueAs(Double.class);
                    break;
                case "phase3":
                    parser.nextToken();
                    phase3 = parser.readValueAs(Double.class);
                    break;
                case "directMagnitude":
                    parser.nextToken();
                    directMagnitude = parser.readValueAs(Double.class);
                    break;
                case "zeroMagnitude":
                    parser.nextToken();
                    zeroMagnitude = parser.readValueAs(Double.class);
                    break;
                case "inverseMagnitude":
                    parser.nextToken();
                    inverseMagnitude = parser.readValueAs(Double.class);
                    break;
                case "directPhase":
                    parser.nextToken();
                    directPhase = parser.readValueAs(Double.class);
                    break;
                case "zeroPhase":
                    parser.nextToken();
                    zeroPhase = parser.readValueAs(Double.class);
                    break;
                case "inversePhase":
                    parser.nextToken();
                    inversePhase = parser.readValueAs(Double.class);
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new ThreePhaseValue(magnitude1, magnitude2, magnitude3, phase1, phase2, phase3,
                directMagnitude, zeroMagnitude, inverseMagnitude, directPhase, zeroPhase, inversePhase);
    }
}
