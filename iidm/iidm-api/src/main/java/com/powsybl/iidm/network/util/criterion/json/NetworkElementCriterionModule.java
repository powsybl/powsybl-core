/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.network.util.criterion.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class NetworkElementCriterionModule extends SimpleModule {
    public NetworkElementCriterionModule() {
        addSerializer(Criterion.class, new CriterionSerializer());
        addSerializer(LineCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(LineCriterion.class));
        addSerializer(TwoWindingsTransformerCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(TwoWindingsTransformerCriterion.class));
        addSerializer(ThreeWindingsTransformerCriterion.class, new NetworkElementEquipmentCriterionSerializer<>(ThreeWindingsTransformerCriterion.class));
        addSerializer(NetworkElementIdListCriterion.class, new NetworkElementIdListCriterionSerializer());

        addDeserializer(Criterion.class, new CriterionDeserializer());
        addDeserializer(LineCriterion.class, new LineCriterionDeserializer());
        addDeserializer(TwoWindingsTransformerCriterion.class, new TwoWindingsTransformerCriterionDeserializer());
        addDeserializer(ThreeWindingsTransformerCriterion.class, new ThreeWindingsTransformerCriterionDeserializer());
        addDeserializer(NetworkElementIdListCriterion.class, new NetworkElementIdListCriterionDeserializer());
    }
}
