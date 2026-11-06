/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.powsybl.iidm.criteria.duration.IntervalTemporaryDurationCriterion;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * <p>Serializer for {@link IntervalTemporaryDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class IntervalTemporaryDurationCriterionSerializer extends StdSerializer<IntervalTemporaryDurationCriterion> {

    public IntervalTemporaryDurationCriterionSerializer() {
        super(IntervalTemporaryDurationCriterion.class);
    }

    @Override
    public void serialize(IntervalTemporaryDurationCriterion criterion, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        LimitDurationCriterionSerDeUtil.serializeCommonHeadAttributes(criterion, jsonGenerator);
        if (criterion.getLowBound().isPresent()) {
            jsonGenerator.writeNumberProperty("lowBound", criterion.getLowBound().orElseThrow());
            jsonGenerator.writeBooleanProperty("lowClosed", criterion.isLowClosed());
        }
        if (criterion.getHighBound().isPresent()) {
            jsonGenerator.writeNumberProperty("highBound", criterion.getHighBound().orElseThrow());
            jsonGenerator.writeBooleanProperty("highClosed", criterion.isHighClosed());
        }
        jsonGenerator.writeEndObject();
    }
}
