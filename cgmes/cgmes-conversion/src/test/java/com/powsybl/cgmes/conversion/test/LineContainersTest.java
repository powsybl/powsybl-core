/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class LineContainersTest {

    @Test
    void testLineContainerWithSwitchConnectedToVoltageLevel() {
        // This unit test reproduces a configuration observed in public data from a European TSO (NG)
        // where a disconnector has one terminal inside a cim:Line container and the other inside a cim:VoltageLevel.
        // First a fictitious voltage level is created to hold the connectivity node inside the cim:Line container
        // (an alias is created for this fictitious voltage level).
        // The fictitious voltage level is later merged with the voltage level of the other end of the switch.
        // We test the two alternatives for creating the fictitious voltage levels from cim:Lines.

        ResourceDataSource ds = new ResourceDataSource("line_container_switch",
                new ResourceSet("/issues", "line_container_switch_EQ.xml"));
        Properties importParams = new Properties();
        Network network;
        Switch s;

        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "false");
        network = Network.read(ds, importParams);
        assertNotNull(network);
        s = network.getSwitch("SwitchBetweenVoltageLevel1AndLineContainer");
        assertNotNull(s);
        assertEquals("VoltageLevel1", s.getVoltageLevel().getId());
        assertEquals(s.getVoltageLevel(), network.getIdentifiable("LineContainer_VL"));

        importParams.put(CgmesImport.CREATE_FICTITIOUS_VOLTAGE_LEVEL_FOR_EVERY_NODE, "true");
        network = Network.read(ds, importParams);
        assertNotNull(network);
        s = network.getSwitch("SwitchBetweenVoltageLevel1AndLineContainer");
        assertNotNull(s);
        assertEquals("VoltageLevel1", s.getVoltageLevel().getId());
        assertEquals(s.getVoltageLevel(), network.getIdentifiable("NodeInsideLineContainer_VL"));
    }
}
