/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class BaseVoltageMappingSerDeTest extends AbstractCgmesExtensionTest {

    @Test
    void test() throws IOException {
        Network network = NoEquipmentNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2024-09-17T13:36:37.831Z"));
        network.newExtension(BaseVoltageMappingAdder.class)
                .addBaseVoltage("id_400", 400, Source.IGM)
                .addBaseVoltage("id_380", 380, Source.BOUNDARY)
                .add();
        allFormatsRoundTripTest(network, "/no_equipment_base_voltage_mapping.xml");
    }
}
