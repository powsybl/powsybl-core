/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.powsybl.iidm.criteria.VoltageInterval;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

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
    public void serialize(VoltageInterval voltageInterval, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        Optional<Double> optNominalVoltageLowBound = voltageInterval.getNominalVoltageLowBound();
        if (optNominalVoltageLowBound.isPresent()) {
            jsonGenerator.writeNumberProperty("nominalVoltageLowBound", optNominalVoltageLowBound.get());
            jsonGenerator.writeBooleanProperty("lowClosed", voltageInterval.isLowClosed());
        }
        Optional<Double> optNominalVoltageHighBound = voltageInterval.getNominalVoltageHighBound();
        if (optNominalVoltageHighBound.isPresent()) {
            jsonGenerator.writeNumberProperty("nominalVoltageHighBound", optNominalVoltageHighBound.get());
            jsonGenerator.writeBooleanProperty("highClosed", voltageInterval.isHighClosed());
        }
        jsonGenerator.writeEndObject();
    }
}
