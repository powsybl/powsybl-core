/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology.data;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataDeserializer extends StdDeserializer<CreateGeneratorBayData> {

    public CreateGeneratorBayDataDeserializer() {
        super(CreateGeneratorBayData.class);
    }

    @Override
    public CreateGeneratorBayData deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        CreateGeneratorBayData data = new CreateGeneratorBayData();
        try {
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.getCurrentName()) {

                    case "version":
                        parser.nextToken();
                        break;

                    case "generator":
                        parser.nextToken();
                        readGenerator(data, parser);
                        break;

                    case "busbarSectionId":
                        parser.nextToken();
                        data.setBusbarSectionId(parser.readValueAs(String.class));
                        break;

                    case "positionOrder":
                        parser.nextToken();
                        data.setPositionOrder(parser.getIntValue());
                        break;

                    case "feederName":
                        parser.nextToken();
                        data.setFeederName(parser.readValueAs(String.class));
                        break;

                    case "direction":
                        parser.nextToken();
                        data.setDirection(parser.readValueAs(ConnectablePosition.Direction.class));
                        break;

                    default:
                        throw new AssertionError("Unexpected field: " + parser.getCurrentName());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return data;
    }

    private static void readGenerator(CreateGeneratorBayData data, JsonParser parser) throws IOException {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {

                case "id":
                    parser.nextToken();
                    data.setGeneratorId(parser.readValueAs(String.class));
                    break;

                case "name":
                    parser.nextToken();
                    data.setGeneratorName(parser.readValueAs(String.class));
                    break;

                case "fictitious":
                    parser.nextToken();
                    data.setGeneratorFictitious(parser.getBooleanValue());
                    break;

                case "energySource":
                    parser.nextToken();
                    data.setGeneratorEnergySource(parser.readValueAs(EnergySource.class));
                    break;

                case "minP":
                    parser.nextToken();
                    data.setGeneratorMinP(parser.getDoubleValue());
                    break;

                case "maxP":
                    parser.nextToken();
                    data.setGeneratorMaxP(parser.getDoubleValue());
                    break;

                case "regulatingTerminal":
                    parser.nextToken();
                    List<String> regTermCharacteristics = parser.readValueAs(new TypeReference<List<String>>() {
                    });
                    if (regTermCharacteristics.size() == 1) {
                        data.setGeneratorRegulatingConnectableId(regTermCharacteristics.get(0));
                    } else if (regTermCharacteristics.size() == 2) {
                        data.setGeneratorRegulatingConnectableId(regTermCharacteristics.get(0)).setGeneratorRegulatingSide(regTermCharacteristics.get(1));
                    } else {
                        throw new AssertionError("Exactly one or two attributes are necessary to define a regulating terminal");
                    }
                    break;

                case "voltageRegulatorOn":
                    parser.nextToken();
                    data.setGeneratorVoltageRegulatorOn(parser.getBooleanValue());
                    break;

                case "targetP":
                    parser.nextToken();
                    data.setGeneratorTargetP(parser.getDoubleValue());
                    break;

                case "targetQ":
                    parser.nextToken();
                    data.setGeneratorTargetQ(parser.getDoubleValue());
                    break;

                case "targetV":
                    parser.nextToken();
                    data.setGeneratorTargetV(parser.getDoubleValue());
                    break;

                case "ratedS":
                    parser.nextToken();
                    data.setGeneratorRatedS(parser.getDoubleValue());
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
    }
}
