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
import com.powsybl.iidm.criteria.duration.AbstractTemporaryDurationCriterion.TemporaryDurationCriterionType;
import com.powsybl.iidm.criteria.duration.EqualityTemporaryDurationCriterion;
import com.powsybl.iidm.criteria.duration.LimitDurationCriterion.LimitDurationType;

import java.io.IOException;

import static com.powsybl.iidm.criteria.json.duration.LimitDurationCriterionSerDeUtil.readAndCheckType;

/**
 * <p>Deserializer for {@link EqualityTemporaryDurationCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class EqualityTemporaryDurationCriterionDeserializer extends StdDeserializer<EqualityTemporaryDurationCriterion> {

    public EqualityTemporaryDurationCriterionDeserializer() {
        super(EqualityTemporaryDurationCriterion.class);
    }

    private static class ParsingContext {
        Integer value;
    }

    @Override
    public EqualityTemporaryDurationCriterion deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type" -> {
                    readAndCheckType(LimitDurationType.TEMPORARY, TemporaryDurationCriterionType.EQUALITY, parser);
                    return true;
                }
                case "version" -> {
                    parser.nextTextValue();
                    return true;
                }
                case "value" -> {
                    parser.nextToken();
                    context.value = parser.getValueAsInt();
                    return true;
                }
                default -> {
                    return false;
                }
            }
        });
        if (context.value == null) {
            throw new IllegalArgumentException("\"value\" attribute is expected for \"EQUALITY\" temporary limit duration criteria");
        }
        return new EqualityTemporaryDurationCriterion(context.value);
    }
}
