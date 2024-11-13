/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.ucte.converter;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DirectoryDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteCountryCode;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class NamingStrategyTest {

    @Test
    void ConvertStartegyTest(){
        ResourceDataSource dataSource = new ResourceDataSource("network3", new ResourceSet("/", "network3.xiidm"));
        Network n1 = Network.read(dataSource);
        UcteExporter exporter = new UcteExporter();
        Properties props = new Properties();
        props.put(UcteExporter.NAMING_STRATEGY,"Counter");
        //NamingStrategy s = new CounterNamingStrategy();
        //s.initialiseNetwork(n1);
        //exporter.export(n1,props, new DirectoryDataSource(Path.of("./"),"test"));

    }

    @Test
    void testUcteCode() {
        NamingStrategy strategy = new DefaultNamingStrategy();

        UcteNodeCode code = strategy.getUcteNodeCode("FABCDE12");
        assertEquals(UcteCountryCode.FR, code.getUcteCountryCode());
        assertEquals("ABCDE", code.getGeographicalSpot());
        assertEquals(UcteVoltageLevelCode.VL_380, code.getVoltageLevelCode());
        assertEquals(Character.valueOf('2'), code.getBusbar());

        UcteNodeCode code2 = strategy.getUcteNodeCode("FABCDE12");
        assertSame(code, code2);
    }

    @Test
    void testUcteElementId() {
        NamingStrategy strategy = new DefaultNamingStrategy();

        UcteElementId elementId = strategy.getUcteElementId("FABCDE12 BFGHIJ2A 1");
        UcteNodeCode node1 = strategy.getUcteNodeCode("FABCDE12");
        assertEquals(node1, elementId.getNodeCode1());

        UcteNodeCode node2 = strategy.getUcteNodeCode("BFGHIJ2A");
        assertEquals(node2, elementId.getNodeCode2());

        assertEquals('1', elementId.getOrderCode());
    }

    @Test
    void testWithNetwork() {
        ResourceDataSource dataSource = new ResourceDataSource("expectedExport", new ResourceSet("/", "expectedExport.uct"));

        UcteImporter importer = new UcteImporter();
        Network network = importer.importData(dataSource, NetworkFactory.findDefault(), new Properties());

        NamingStrategy strategy = new DefaultNamingStrategy();
        UcteNodeCode code1 = strategy.getUcteNodeCode("B_SU1_11");
        Bus bus1 = network.getVoltageLevel("B_SU1_1").getBusBreakerView().getBus("B_SU1_11");
        assertEquals(code1, strategy.getUcteNodeCode(bus1));

        DanglingLine danglingLine = network.getDanglingLine("XG__F_21 F_SU1_21 1");
        UcteNodeCode code2 = strategy.getUcteNodeCode("XG__F_21");
        assertEquals(code2, strategy.getUcteNodeCode(danglingLine));

        UcteElementId elementId1 = strategy.getUcteElementId("XG__F_21 F_SU1_21 1");
        assertEquals(elementId1, strategy.getUcteElementId(danglingLine));

        UcteElementId elementId2 = strategy.getUcteElementId("B_SU1_11 B_SU1_21 1");
        Branch branch = network.getBranch("B_SU1_11 B_SU1_21 1");
        assertEquals(elementId2, strategy.getUcteElementId(branch));

        UcteElementId elementId3 = strategy.getUcteElementId("F_SU1_12 F_SU1_11 1");
        Switch sw = network.getSwitch("F_SU1_12 F_SU1_11 1");
        assertEquals(elementId3, strategy.getUcteElementId(sw));
    }
}
