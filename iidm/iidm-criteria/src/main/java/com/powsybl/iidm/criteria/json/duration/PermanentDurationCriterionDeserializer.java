/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json.duration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion.LimitDurationType;
import com.powsybl.iidm.criteria.duration.PermanentDurationCriterion;

import java.io.IOException;

import static com.powsybl.iidm.criteria.json.duration.LimitDurationCriterionSerDeUtil.readAndCheckType;

/**
 * <p>Deserializer for {@link PermanentDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class PermanentDurationCriterionDeserializer extends StdDeserializer<PermanentDurationCriterion> {

    public PermanentDurationCriterionDeserializer() {
        super(PermanentDurationCriterion.class);
    }

    @Override
    public PermanentDurationCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type" -> {
                    readAndCheckType(LimitDurationType.PERMANENT, null, parser);
                    return true;
                }
                case "version" -> {
                    parser.nextTextValue();
                    return true;
                }
                default -> {
                    return false;
                }
            }
        });
        return new PermanentDurationCriterion();
    }
}
