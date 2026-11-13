/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.powsybl.iidm.criteria.duration.AllTemporaryDurationCriterion;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * <p>Serializer for {@link AllTemporaryDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class AllTemporaryDurationCriterionSerializer extends StdSerializer<AllTemporaryDurationCriterion> {

    public AllTemporaryDurationCriterionSerializer() {
        super(AllTemporaryDurationCriterion.class);
    }

    @Override
    public void serialize(AllTemporaryDurationCriterion criterion, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        LimitDurationCriterionSerDeUtil.serializeCommonHeadAttributes(criterion, jsonGenerator);
        jsonGenerator.writeEndObject();
    }
}
