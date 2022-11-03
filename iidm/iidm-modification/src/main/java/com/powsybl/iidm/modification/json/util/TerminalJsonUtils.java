/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.powsybl.iidm.modification.data.util.TerminalData;

import java.io.IOException;
import java.util.List;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class TerminalJsonUtils {

    public static void serialize(TerminalData terminalData, JsonGenerator jsonGenerator) throws IOException {
        if (terminalData.getConnectableId() != null) {
            jsonGenerator.writeArrayFieldStart("regulatingTerminal");
            jsonGenerator.writeString(terminalData.getConnectableId());
            String regulatingSide = terminalData.getSide();
            if (regulatingSide != null) {
                jsonGenerator.writeString(regulatingSide);
            }
            jsonGenerator.writeEndArray();
        }
    }

    public static TerminalData deserialize(JsonParser parser) throws IOException {
        List<String> regTermCharacteristics = parser.readValueAs(new TypeReference<List<String>>() { });
        if (regTermCharacteristics.size() == 1) {
            return new TerminalData(regTermCharacteristics.get(0), null);
        } else if (regTermCharacteristics.size() == 2) {
            return new TerminalData(regTermCharacteristics.get(0), regTermCharacteristics.get(1));
        } else {
            throw new AssertionError("Exactly one or two attributes are necessary to define a regulating terminal");
        }
    }

    private TerminalJsonUtils() {
    }
}
