/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteElementId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
class CounterNamingStrategyTest {

    private Network network;
    private CounterNamingStrategy strategy;

    @BeforeEach
    void setUp() {
        ResourceDataSource dataSource = new ResourceDataSource("network", new ResourceSet("/", "network.xiidm"));
        network = Network.read(dataSource);
        strategy = new CounterNamingStrategy();
    }

    @Test
    void testName() {
        assertEquals("Counter", strategy.getName());
    }

    @Test
    void testInitialNetwork() {
        assertThrows(UcteException.class, () -> strategy.getUcteNodeCode("NGEN"));
        assertThrows(UcteException.class, () -> strategy.getUcteElementId("NHV1_NHV2_1"));

        strategy.initialiseNetwork(network);

        assertDoesNotThrow(() -> strategy.getUcteNodeCode("NGEN"));
        assertDoesNotThrow(() -> strategy.getUcteElementId("NHV1_NHV2_1"));
    }

    @Test
    void testVoltageLevelCounterNaming() {
        strategy.initialiseNetwork(network);

        UcteNodeCode firstBusCode = strategy.getUcteNodeCode(network.getBusBreakerView().getBus("NGEN"));
        UcteNodeCode secondBusCode = strategy.getUcteNodeCode(network.getBusBreakerView().getBus("NGEN2"));
        UcteNodeCode thirdBusCode = strategy.getUcteNodeCode(network.getBusBreakerView().getBus("NGEN3"));

        String firstIdPart = firstBusCode.toString().substring(0, 6);
        String secondIdPart = secondBusCode.toString().substring(0, 6);
        String thirdIdPart = thirdBusCode.toString().substring(0, 6);

        assertEquals(firstIdPart, secondIdPart);
        assertEquals(firstIdPart, thirdIdPart);
        assertEquals(secondIdPart, thirdIdPart);
    }

    @Test
    void testBasicNodeCodeGeneration() {

        strategy.initialiseNetwork(network);
        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        Bus hv1Bus = network.getBusBreakerView().getBus("NHV1");
        Bus hv2Bus = network.getBusBreakerView().getBus("NHV2");
        Bus loadBus = network.getBusBreakerView().getBus("NLOAD");
        Bus ucteBus = network.getBusBreakerView().getBus("F0000010");

        UcteNodeCode genCode = strategy.getUcteNodeCode(genBus);
        UcteNodeCode hv1Code = strategy.getUcteNodeCode(hv1Bus);
        UcteNodeCode hv2Code = strategy.getUcteNodeCode(hv2Bus);
        UcteNodeCode loadCode = strategy.getUcteNodeCode(loadBus);
        UcteNodeCode ucteCode = strategy.getUcteNodeCode(ucteBus);

        assertAll(
                () -> assertTrue(UcteNodeCode.isUcteNodeId(genCode.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(hv1Code.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(hv2Code.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(loadCode.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(ucteCode.toString()))
        );

        assertAll(
                () -> assertNotEquals(genCode, hv1Code),
                () -> assertNotEquals(genCode, hv2Code),
                () -> assertNotEquals(genCode, loadCode),
                () -> assertNotEquals(hv1Code, hv2Code),
                () -> assertNotEquals(hv1Code, loadCode),
                () -> assertNotEquals(hv2Code, loadCode)
        );
    }

    @Test
    void testBranchElementIds() {
        strategy.initialiseNetwork(network);

        Branch<?> transformer1 = network.getBranch("NGEN_NHV1");
        Branch<?> transformer2 = network.getBranch("NHV2_NLOAD");
        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId transformerId1 = strategy.getUcteElementId(transformer1);
        UcteElementId transformerId2 = strategy.getUcteElementId(transformer2);
        UcteElementId lineId1 = strategy.getUcteElementId(line1);
        UcteElementId lineId2 = strategy.getUcteElementId(line2);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(transformerId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(transformerId2.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(lineId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(lineId2.toString())),
                () -> assertNotEquals(transformerId1, transformerId2),
                () -> assertNotEquals(lineId1, lineId2)
        );

        assertEquals(transformerId1, strategy.getUcteElementId(transformer1));
        assertEquals(transformerId2, strategy.getUcteElementId(transformer2));
    }

    @Test
    void testSwitchElementIds() {
        strategy.initialiseNetwork(network);
        Switch sw = network.getSwitch("NGEN-NGEN2");
        Switch sw2 = network.getSwitch("NGEN-NGEN3");
        Switch sw3 = network.getSwitch("NGEN-NGEN3");

        UcteElementId swId = strategy.getUcteElementId(sw);
        UcteElementId swId2 = strategy.getUcteElementId(sw2);
        UcteElementId swId3 = strategy.getUcteElementId(sw3);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(swId.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(swId2.toString())),
                () -> assertNotEquals(swId, swId2),
                () -> assertEquals(swId3, swId2)
        );

    }

    @Test
    void testDanglingLineElementIds() {
        strategy.initialiseNetwork(network);
        DanglingLine dl1 = network.getDanglingLine("DL1");
        DanglingLine dl2 = network.getDanglingLine("DL2");
        UcteElementId dlId1 = strategy.getUcteElementId(dl1);
        UcteElementId dlId2 = strategy.getUcteElementId(dl2);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(dlId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(dlId1.toString())),
                () -> assertNotEquals(dlId1, dlId2)

        );

    }

    @Test
    void testDanglingLineNodeIds() {
        strategy.initialiseNetwork(network);
        DanglingLine dl1 = network.getDanglingLine("DL1");
        UcteNodeCode code1 = strategy.getUcteNodeCode(dl1);

        assertTrue(UcteNodeCode.isUcteNodeId(code1.toString()));
    }

    @Test
    void testParallelLines() {
        strategy.initialiseNetwork(network);

        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId id1 = strategy.getUcteElementId(line1);
        UcteElementId id2 = strategy.getUcteElementId(line2);

        assertAll(
                () -> assertNotNull(id1),
                () -> assertNotNull(id2),
                () -> assertTrue(UcteElementId.isUcteElementId(id1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(id2.toString())),
                () -> assertNotEquals(id1, id2)
        );
    }

    @Test
    void testExistingUcteNodeCodes() {
        strategy.initialiseNetwork(network);

        Bus bus = network.getBusBreakerView().getBus("NGEN");
        UcteNodeCode firstCode = strategy.getUcteNodeCode(bus);
        assertNotNull(firstCode);

        UcteNodeCode existingCode = strategy.getUcteNodeCode(bus);
        assertNotNull(existingCode);
        assertEquals(firstCode, existingCode);

        UcteNodeCode presentCode = strategy.getUcteNodeCode(bus.getId());
        assertNotNull(presentCode);
        assertEquals(firstCode, presentCode);
        assertEquals(existingCode, presentCode);
    }

    @Test
    void testNullAndInvalidIds() {
        strategy.initialiseNetwork(network);

        assertAll(
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteNodeCode((String) null)),
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteElementId((String) null)),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteNodeCode("INVALID_ID")),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteElementId("INVALID_ID"))
        );
    }

    @Test
    void testCountryCode() {
        strategy.initialiseNetwork(network);

        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        UcteNodeCode code = strategy.getUcteNodeCode(genBus);
        assertEquals('F', code.toString().charAt(0)); // France
    }

}
