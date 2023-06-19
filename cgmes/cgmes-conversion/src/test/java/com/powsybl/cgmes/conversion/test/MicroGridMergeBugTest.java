/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MicroGridMergeBugTest {

    @Test
    void shouldNotThrowAnException() {
        Conversion.Config config = new Conversion.Config();
        Network be = ConversionUtil.networkModel(CgmesConformity1Catalog.microGridBaseCaseBE(), config);
        Network nl = ConversionUtil.networkModel(CgmesConformity1Catalog.microGridBaseCaseNL(), config);
        be.merge(nl);
        for (Bus bus : be.getBusView().getBuses()) {
            assertDoesNotThrow(bus::getConnectedComponent);
        }
    }
}
