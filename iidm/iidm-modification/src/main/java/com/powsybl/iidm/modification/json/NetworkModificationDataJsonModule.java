/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.modification.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.iidm.modification.data.topology.CreateGeneratorBayData;
import com.powsybl.iidm.modification.json.topology.CreateGeneratorBayDataDeserializer;
import com.powsybl.iidm.modification.json.topology.CreateGeneratorBayDataSerializer;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class NetworkModificationDataJsonModule extends SimpleModule {

    public NetworkModificationDataJsonModule() {
        addSerializer(CreateGeneratorBayData.class, new CreateGeneratorBayDataSerializer());
        addDeserializer(CreateGeneratorBayData.class, new CreateGeneratorBayDataDeserializer());
    }
}
