/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.util.criterion.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.iidm.network.util.criterion.AbstractNetworkElementEquipmentCriterion;
import com.powsybl.iidm.network.util.criterion.NetworkElementCriterion;

import java.io.IOException;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class NetworkElementCriterionSerializer {
    private NetworkElementCriterionSerializer() {
    }

    public static void serializeCommonHeadAttributes(NetworkElementCriterion criterion, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeStringField("type", criterion.getNetworkElementCriterionType().getName());
        jsonGenerator.writeStringField("version", NetworkElementCriterion.getVersion());
        if (criterion.getName() != null) {
            jsonGenerator.writeStringField("name", criterion.getName());
        }
    }

    public static void serializeCountryCriterion(AbstractNetworkElementEquipmentCriterion criterion, JsonGenerator jsonGenerator,
                                                 SerializerProvider serializerProvider) throws IOException {
        if (criterion.getCountryCriterion() != null) {
            serializerProvider.defaultSerializeField("countryCriterion", criterion.getCountryCriterion(), jsonGenerator);
        }
    }

    public static void serializeNominalVoltageCriterion(AbstractNetworkElementEquipmentCriterion criterion, JsonGenerator jsonGenerator,
                                                        SerializerProvider serializerProvider) throws IOException {
        if (criterion.getNominalVoltageCriterion() != null) {
            serializerProvider.defaultSerializeField("nominalVoltageCriterion", criterion.getNominalVoltageCriterion(), jsonGenerator);
        }
    }
}
