/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.criteria.*;

/**
 * <p>Jackson module for serialization/deserialization of {@link NetworkElementCriterion}.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkElementCriterionModule extends SimpleModule {
    public NetworkElementCriterionModule() {
        addSerializer(Criterion.class, new CriterionSerializer());
        addSerializer(LineCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(LineCriterion.class));
        addSerializer(DanglingLineCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(DanglingLineCriterion.class));
        addSerializer(TieLineCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(TieLineCriterion.class));
        addSerializer(TwoWindingsTransformerCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(TwoWindingsTransformerCriterion.class));
        addSerializer(ThreeWindingsTransformerCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(ThreeWindingsTransformerCriterion.class));
        addSerializer(IdentifiableCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(IdentifiableCriterion.class));
        addSerializer(NetworkElementIdListCriterion.class, new NetworkElementIdListCriterionSerializer());
        addSerializer(VoltageInterval.class, new VoltageIntervalSerializer());

        addDeserializer(Criterion.class, new CriterionDeserializer());
        addDeserializer(LineCriterion.class, new LineCriterionDeserializer());
        addDeserializer(DanglingLineCriterion.class, new DanglingLineCriterionDeserializer());
        addDeserializer(TieLineCriterion.class, new TieLineCriterionDeserializer());
        addDeserializer(TwoWindingsTransformerCriterion.class, new TwoWindingsTransformerCriterionDeserializer());
        addDeserializer(ThreeWindingsTransformerCriterion.class, new ThreeWindingsTransformerCriterionDeserializer());
        addDeserializer(IdentifiableCriterion.class, new IdentifiableCriterionDeserializer());
        addDeserializer(NetworkElementIdListCriterion.class, new NetworkElementIdListCriterionDeserializer());
        addDeserializer(VoltageInterval.class, new VoltageIntervalDeserializer());
    }
}
