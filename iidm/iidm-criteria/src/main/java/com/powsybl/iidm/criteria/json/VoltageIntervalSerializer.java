/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.criteria.VoltageInterval;

import java.io.IOException;
import java.util.Optional;

/**
 * <p>Serializer for {@link VoltageInterval} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class VoltageIntervalSerializer extends StdSerializer<VoltageInterval> {

    public VoltageIntervalSerializer() {
        super(VoltageInterval.class);
    }

    @Override
    public void serialize(VoltageInterval voltageInterval, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        Optional<Double> optNominalVoltageLowBound = voltageInterval.getNominalVoltageLowBound();
        if (optNominalVoltageLowBound.isPresent()) {
            jsonGenerator.writeNumberField("nominalVoltageLowBound", optNominalVoltageLowBound.get());
            jsonGenerator.writeBooleanField("lowClosed", voltageInterval.isLowClosed());
        }
        Optional<Double> optNominalVoltageHighBound = voltageInterval.getNominalVoltageHighBound();
        if (optNominalVoltageHighBound.isPresent()) {
            jsonGenerator.writeNumberField("nominalVoltageHighBound", optNominalVoltageHighBound.get());
            jsonGenerator.writeBooleanField("highClosed", voltageInterval.isHighClosed());
        }
        jsonGenerator.writeEndObject();
    }
}
