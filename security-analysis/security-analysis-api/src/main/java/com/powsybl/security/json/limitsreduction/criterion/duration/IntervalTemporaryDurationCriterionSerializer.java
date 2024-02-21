/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction.criterion.duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.security.limitsreduction.criterion.duration.IntervalTemporaryDurationCriterion;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IntervalTemporaryDurationCriterionSerializer extends StdSerializer<IntervalTemporaryDurationCriterion> {

    public IntervalTemporaryDurationCriterionSerializer() {
        super(IntervalTemporaryDurationCriterion.class);
    }

    @Override
    public void serialize(IntervalTemporaryDurationCriterion criterion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        LimitDurationCriterionSerDeUtil.serializeCommonHeadAttributes(criterion, jsonGenerator);
        LimitDurationCriterionSerDeUtil.serializeComparisonType(criterion, jsonGenerator);
        if (criterion.getLowBound() != null) {
            jsonGenerator.writeNumberField("lowBound", criterion.getLowBound());
            jsonGenerator.writeBooleanField("lowClosed", criterion.isLowClosed());
        }
        if (criterion.getHighBound() != null) {
            jsonGenerator.writeNumberField("highBound", criterion.getHighBound());
            jsonGenerator.writeBooleanField("highClosed", criterion.isHighClosed());
        }
        jsonGenerator.writeEndObject();
    }
}
