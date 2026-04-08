/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class CimCharacteristicsTest {

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        network.newExtension(CimCharacteristicsAdder.class)
                .setTopologyKind(CgmesTopologyKind.NODE_BREAKER)
                .setCimVersion(16)
                .add();
        CimCharacteristics extension = network.getExtension(CimCharacteristics.class);
        assertNotNull(extension);
        assertEquals(CgmesTopologyKind.NODE_BREAKER, extension.getTopologyKind());
        assertEquals(16, extension.getCimVersion());
    }

    @Test
    void invalid() {
        PowsyblException e = assertThrows(PowsyblException.class, () -> EurostagTutorialExample1Factory.create().newExtension(CimCharacteristicsAdder.class).add());
        assertTrue(e.getMessage().contains("CimCharacteristics.topologyKind is undefined"));
    }
}
