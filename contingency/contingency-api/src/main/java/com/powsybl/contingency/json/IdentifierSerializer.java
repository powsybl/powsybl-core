/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.contingency.contingency.list.identifiant.Identifier;
import com.powsybl.contingency.contingency.list.identifiant.IdentifierList;
import com.powsybl.contingency.contingency.list.identifiant.SimpleIdentifier;
import com.powsybl.contingency.contingency.list.identifiant.UcteIdentifier;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierSerializer extends StdSerializer<Identifier> {

    public IdentifierSerializer() {
        super(Identifier.class);
    }

    @Override
    public void serialize(Identifier identifier, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("type", identifier.getType().toString());
        switch (identifier.getType()) {
            case SIMPLE:
                jsonGenerator.writeStringField("identifier", ((SimpleIdentifier) identifier).getIdentifier());
                break;
            case LIST:
                jsonGenerator.writeObjectField("identifierList", ((IdentifierList) identifier).getIdentifiers());
                break;
            case UCTE:
                UcteIdentifier ucteIdentifier = (UcteIdentifier) identifier;
                jsonGenerator.writeStringField("voltageLevelId1", ucteIdentifier.getVoltageLevelId1());
                jsonGenerator.writeStringField("voltageLevelId2", ucteIdentifier.getVoltageLevelId2());
                jsonGenerator.writeNumberField("order", ucteIdentifier.getOrder());
                break;
        }
        jsonGenerator.writeEndObject();
    }
}
