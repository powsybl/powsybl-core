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
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class FaultDeserializer extends StdDeserializer<Fault> {

    FaultDeserializer() {
        super(Fault.class);
    }

    @Override
    public Fault deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String dataType = null;
        String id = "";
        double r = Double.NaN;
        double x = Double.NaN;
        Fault.ConnectionType connection = Fault.ConnectionType.SERIES;
        Fault.FaultType faultType = Fault.FaultType.THREE_PHASE;
        boolean withLimitViolations = false;
        boolean withVoltageMap = false;
        double proportionalLocation = Double.NaN;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    break;
                case "dataType":
                    parser.nextToken();
                    dataType = parser.readValueAs(String.class);
                    break;
                case "id":
                    parser.nextToken();
                    id = parser.readValueAs(String.class);
                    break;
                case "r":
                    parser.nextToken();
                    r = parser.readValueAs(Double.class);
                    break;
                case "x":
                    parser.nextToken();
                    x = parser.readValueAs(Double.class);
                    break;
                case "connection":
                    parser.nextToken();
                    connection = Fault.ConnectionType.valueOf(parser.readValueAs(String.class));
                    break;
                case "faultType":
                    parser.nextToken();
                    faultType = Fault.FaultType.valueOf(parser.readValueAs(String.class));
                    break;
                case "withLimitViolations":
                    parser.nextToken();
                    withLimitViolations = parser.readValueAs(Boolean.class);
                    break;
                case "withVoltageMap":
                    parser.nextToken();
                    withVoltageMap = parser.readValueAs(Boolean.class);
                    break;
                case "proportionalLocation":
                    parser.nextToken();
                    proportionalLocation = parser.readValueAs(Double.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (null == dataType) {
            throw new AssertionError("Required datatype field is missing");
        }

        Fault fault;
        switch (dataType) {
            case "BusFault":
                fault = new BusFault(id, r, x, connection, faultType, withLimitViolations, withVoltageMap);
                break;
            case "BranchFault":
                fault = new BranchFault(id, r, x, connection, faultType, withLimitViolations, withVoltageMap, proportionalLocation);
                break;
            default:
                throw new AssertionError("Unexpected datatype: " + dataType);
        }
        return fault;
    }
}
