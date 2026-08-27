/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class NamingStrategyTest {

    @Test
    void testUcteCode() {
        NamingStrategy strategy = new DefaultNamingStrategy();
        NamingStrategy.Context context = strategy.initialize(Network.create("test", "test"));

        UcteNodeCode code = strategy.getUcteNodeCode(context, "FABCDE12");
        assertEquals(UcteCountryCode.FR, code.getUcteCountryCode());
        assertEquals("ABCDE", code.getGeographicalSpot());
        assertEquals(UcteVoltageLevelCode.VL_380, code.getVoltageLevelCode());
        assertEquals(Character.valueOf('2'), code.getBusbar());

        UcteNodeCode code2 = strategy.getUcteNodeCode(context, "FABCDE12");
        assertSame(code, code2);
    }

    @Test
    void testUcteElementId() {
        NamingStrategy strategy = new DefaultNamingStrategy();
        NamingStrategy.Context context = strategy.initialize(Network.create("test", "test"));

        UcteElementId elementId = strategy.getUcteElementId(context, "FABCDE12 BFGHIJ2A 1");
        UcteNodeCode node1 = strategy.getUcteNodeCode(context, "FABCDE12");
        assertEquals(node1, elementId.getNodeCode1());

        UcteNodeCode node2 = strategy.getUcteNodeCode(context, "BFGHIJ2A");
        assertEquals(node2, elementId.getNodeCode2());

        assertEquals('1', elementId.getOrderCode());
    }

    @Test
    void testWithNetwork() {
        ResourceDataSource dataSource = new ResourceDataSource("expectedExport", new ResourceSet("/", "expectedExport.uct"));

        UcteImporter importer = new UcteImporter();
        Network network = importer.importData(dataSource, NetworkFactory.findDefault(), new Properties());

        NamingStrategy strategy = new DefaultNamingStrategy();
        NamingStrategy.Context context = strategy.initialize(network);
        UcteNodeCode code1 = strategy.getUcteNodeCode(context, "B_SU1_11");
        Bus bus1 = network.getVoltageLevel("B_SU1_1").getBusBreakerView().getBus("B_SU1_11");
        assertEquals(code1, strategy.getUcteNodeCode(context, bus1));

        BoundaryLine boundaryLine = network.getBoundaryLine("XG__F_21 F_SU1_21 1");
        UcteNodeCode code2 = strategy.getUcteNodeCode(context, "XG__F_21");
        assertEquals(code2, strategy.getUcteNodeCode(context, boundaryLine));

        UcteElementId elementId1 = strategy.getUcteElementId(context, "XG__F_21 F_SU1_21 1");
        assertEquals(elementId1, strategy.getUcteElementId(context, boundaryLine));

        UcteElementId elementId2 = strategy.getUcteElementId(context, "B_SU1_11 B_SU1_21 1");
        Branch branch = network.getBranch("B_SU1_11 B_SU1_21 1");
        assertEquals(elementId2, strategy.getUcteElementId(context, branch));

        UcteElementId elementId3 = strategy.getUcteElementId(context, "F_SU1_12 F_SU1_11 1");
        Switch sw = network.getSwitch("F_SU1_12 F_SU1_11 1");
        assertEquals(elementId3, strategy.getUcteElementId(context, sw));
    }
}
