/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json.limitsreduction;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.network.util.criterion.*;
import com.powsybl.iidm.network.util.criterion.json.*;
import com.powsybl.security.json.limitsreduction.criterion.duration.*;
import com.powsybl.security.limitsreduction.LimitReductionDefinitionList.LimitReductionDefinition;
import com.powsybl.security.limitsreduction.criterion.duration.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class LimitReductionModule extends SimpleModule {
    public LimitReductionModule() {
        addSerializer(LimitReductionDefinition.class, new LimitReductionDefinitionSerializer());
        addSerializer(Criterion.class, new CriterionSerializer());
        addDeserializer(LimitReductionDefinition.class, new LimitReductionDefinitionDeserializer());
        addDeserializer(Criterion.class, new CriterionDeserializer());

        registerNetworkElementCriteria();
        registerLimitDurationCriteria();
    }

    private void registerNetworkElementCriteria() {
        setMixInAnnotation(NetworkElementCriterion.class, NetworkElementCriterionMixIn.class);
        registerNamedSubtype(LineCriterion.class, LineCriterion.TYPE.getName(),
                new NetworkElementEquipmentCriterionSerializer<>(LineCriterion.class),
                new LineCriterionDeserializer());
        registerNamedSubtype(TwoWindingsTransformerCriterion.class, TwoWindingsTransformerCriterion.TYPE.getName(),
                new NetworkElementEquipmentCriterionSerializer<>(TwoWindingsTransformerCriterion.class),
                new TwoWindingsTransformerCriterionDeserializer());
        registerNamedSubtype(ThreeWindingsTransformerCriterion.class, ThreeWindingsTransformerCriterion.TYPE.getName(),
                new NetworkElementEquipmentCriterionSerializer<>(ThreeWindingsTransformerCriterion.class),
                new ThreeWindingsTransformerCriterionDeserializer());
        registerNamedSubtype(NetworkElementIdListCriterion.class, NetworkElementIdListCriterion.TYPE.getName(),
                new NetworkElementIdListCriterionSerializer(),
                new NetworkElementIdListCriterionDeserializer());
    }

    private void registerLimitDurationCriteria() {
        setMixInAnnotation(LimitDurationCriterion.class, LimitDurationCriterionMixIn.class);
        registerNamedSubtype(PermanentDurationCriterion.class,
                LimitDurationCriterionSerDeUtil.SerializationType.PERMANENT.name(),
                new PermanentDurationCriterionSerializer(),
                new PermanentDurationCriterionDeserializer());
        registerNamedSubtype(AllTemporaryDurationCriterion.class,
                LimitDurationCriterionSerDeUtil.SerializationType.TEMPORARY_ALL.name(),
                new AllTemporaryDurationCriterionSerializer(),
                new AllTemporaryDurationCriterionDeserializer());
        registerNamedSubtype(EqualityTemporaryDurationCriterion.class,
                LimitDurationCriterionSerDeUtil.SerializationType.TEMPORARY_EQUALITY.name(),
                new EqualityTemporaryDurationCriterionSerializer(),
                new EqualityTemporaryDurationCriterionDeserializer());
        registerNamedSubtype(IntervalTemporaryDurationCriterion.class,
                LimitDurationCriterionSerDeUtil.SerializationType.TEMPORARY_INTERVAL.name(),
                new IntervalTemporaryDurationCriterionSerializer(),
                new IntervalTemporaryDurationCriterionDeserializer());
    }

    /**
     * Deserializer for NetworkElementCriterion will be chosen based on the "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface NetworkElementCriterionMixIn {
    }

    /**
     * Deserializer for LimitDurationCriterion be chosen based on deduction (presence of existing attributes).
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface LimitDurationCriterionMixIn {
    }

    private <T> void registerNamedSubtype(Class<T> clazz, String typeName, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        registerSubtypes(new NamedType(clazz, typeName));
        addSerializer(clazz, serializer);
        addDeserializer(clazz, deserializer);
    }
}
