/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.topology.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;
import java.io.UncheckedIOException;

import static com.powsybl.iidm.modification.topology.data.CreateGeneratorBayData.VERSION;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CreateGeneratorBayDataSerializer extends StdSerializer<CreateGeneratorBayData> {

    public CreateGeneratorBayDataSerializer() {
        super(CreateGeneratorBayData.class);
    }

    @Override
    public void serialize(CreateGeneratorBayData data, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
        data.checks();
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("version", VERSION);

            jsonGenerator.writeFieldName("generator");
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("id", data.getGeneratorId());
            if (data.getGeneratorName() != null) {
                jsonGenerator.writeStringField("name", data.getGeneratorName());
            }
            JsonUtil.writeOptionalBooleanField(jsonGenerator, "fictitious", data.isGeneratorFictitious(), false);
            jsonGenerator.writeStringField("energySource", data.getGeneratorEnergySource().name());
            jsonGenerator.writeNumberField("minP", data.getGeneratorMinP());
            jsonGenerator.writeNumberField("maxP", data.getGeneratorMaxP());
            String regulatingConnectableId = data.getGeneratorRegulatingConnectableId();
            if (regulatingConnectableId != null) {
                jsonGenerator.writeArrayFieldStart("regulatingTerminal");
                jsonGenerator.writeString(regulatingConnectableId);
                String regulatingSide = data.getGeneratorRegulatingSide();
                if (regulatingSide != null) {
                    jsonGenerator.writeString(regulatingSide);
                }
                jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeBooleanField("voltageRegulatorOn", data.isGeneratorVoltageRegulatorOn());
            jsonGenerator.writeNumberField("targetP", data.getGeneratorTargetP());
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "targetQ", data.getGeneratorTargetQ());
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "targetV", data.getGeneratorTargetV());
            JsonUtil.writeOptionalDoubleField(jsonGenerator, "ratedS", data.getGeneratorRatedS());
            jsonGenerator.writeEndObject();

            jsonGenerator.writeStringField("busbarSectionId", data.getBusbarSectionId());
            jsonGenerator.writeNumberField("positionOrder", data.getPositionOrder());
            JsonUtil.writeOptionalStringField(jsonGenerator, "feederName", data.getFeederName());
            jsonGenerator.writeStringField("direction", data.getDirection().name());

            jsonGenerator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
