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
import com.powsybl.iidm.criteria.SingleNominalVoltageCriterion;

import java.io.IOException;

/**
 * <p>Serializer for {@link com.powsybl.iidm.criteria.SingleNominalVoltageCriterion.VoltageInterval} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class VoltageIntervalSerializer extends StdSerializer<SingleNominalVoltageCriterion.VoltageInterval> {

    public VoltageIntervalSerializer() {
        super(SingleNominalVoltageCriterion.VoltageInterval.class);
    }

    @Override
    public void serialize(SingleNominalVoltageCriterion.VoltageInterval voltageInterval, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("nominalVoltageLowBound", voltageInterval.getNominalVoltageLowBound());
        jsonGenerator.writeNumberField("nominalVoltageHighBound", voltageInterval.getNominalVoltageHighBound());
        jsonGenerator.writeBooleanField("lowClosed", voltageInterval.getLowClosed());
        jsonGenerator.writeBooleanField("highClosed", voltageInterval.getHighClosed());
        jsonGenerator.writeEndObject();
    }
}
