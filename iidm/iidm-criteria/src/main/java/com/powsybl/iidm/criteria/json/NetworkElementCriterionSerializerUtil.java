/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.json;

import com.powsybl.iidm.criteria.AbstractNetworkElementEquipmentCriterion;
import com.powsybl.iidm.criteria.NetworkElementCriterion;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * <p>Utility class for serialization/deserialization of {@link NetworkElementCriterion} objects.</p>
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class NetworkElementCriterionSerializerUtil {
    private NetworkElementCriterionSerializerUtil() {
    }

    public static void serializeCommonHeadAttributes(NetworkElementCriterion criterion, JsonGenerator jsonGenerator) throws JacksonException {
        jsonGenerator.writeStringProperty("type", criterion.getNetworkElementCriterionType().getName());
        jsonGenerator.writeStringProperty("version", NetworkElementCriterion.getVersion());
        if (criterion.getName() != null) {
            jsonGenerator.writeStringProperty("name", criterion.getName());
        }
    }

    public static void serializeCountryCriterion(AbstractNetworkElementEquipmentCriterion criterion, JsonGenerator jsonGenerator,
                                                 SerializationContext serializationContext) throws JacksonException {
        if (criterion.getCountryCriterion() != null) {
            serializationContext.defaultSerializeProperty("countryCriterion", criterion.getCountryCriterion(), jsonGenerator);
        }
    }

    public static void serializeNominalVoltageCriterion(AbstractNetworkElementEquipmentCriterion criterion, JsonGenerator jsonGenerator,
                                                        SerializationContext serializationContext) throws JacksonException {
        if (criterion.getNominalVoltageCriterion() != null) {
            serializationContext.defaultSerializeProperty("nominalVoltageCriterion", criterion.getNominalVoltageCriterion(), jsonGenerator);
        }
    }
}
