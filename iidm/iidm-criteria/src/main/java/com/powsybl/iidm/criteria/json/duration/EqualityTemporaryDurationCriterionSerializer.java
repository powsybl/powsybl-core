/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;

import java.io.IOException;

/**
 * <p>Serializer for {@link EqualityTemporaryDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class EqualityTemporaryDurationCriterionSerializer extends StdSerializer<EqualityTemporaryDurationCriterion> {

    public EqualityTemporaryDurationCriterionSerializer() {
        super(EqualityTemporaryDurationCriterion.class);
    }

    @Override
    public void serialize(EqualityTemporaryDurationCriterion criterion, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        LimitDurationCriterionSerDeUtil.serializeCommonHeadAttributes(criterion, jsonGenerator);
        jsonGenerator.writeNumberField("value", criterion.getDurationEqualityValue());
        jsonGenerator.writeEndObject();
    }
}
