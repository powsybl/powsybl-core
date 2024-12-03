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
        Bus genbus2 = network.getBusBreakerView().getBus("NGEN2");
        Bus ucteBus = network.getBusBreakerView().getBus("F0000079");
        Bus loadBus = network.getBusBreakerView().getBus("NLOAD");

        UcteNodeCode genCode = strategy.getUcteNodeCode(genBus);
        UcteNodeCode genCode2 = strategy.getUcteNodeCode(genbus2);
        UcteNodeCode loadCode = strategy.getUcteNodeCode(loadBus);
        UcteNodeCode ucteCode = strategy.getUcteNodeCode(ucteBus);

        assertAll(
                () -> assertTrue(UcteNodeCode.isUcteNodeId(genCode.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(genCode2.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(loadCode.toString())),
                () -> assertTrue(UcteNodeCode.isUcteNodeId(ucteCode.toString())),

                () -> assertNotEquals(genCode, genCode2),
                () -> assertNotEquals(genCode, loadCode),
                () -> assertNotEquals(genCode, ucteCode),
                () -> assertNotEquals(genCode2, loadCode),
                () -> assertNotEquals(genCode2, ucteCode),
                () -> assertNotEquals(ucteCode, loadCode),

                () -> assertEquals("F0000071", genCode.toString()),
                () -> assertEquals("F0000072", genCode2.toString()),
                () -> assertEquals("F0000074", ucteCode.toString()),
                () -> assertEquals("F0000331", loadCode.toString())

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
                () -> assertNotEquals(lineId1, lineId2),

                () -> assertEquals(transformerId1, strategy.getUcteElementId(transformer1)),
                () -> assertEquals(transformerId2, strategy.getUcteElementId(transformer2)),

                () -> assertEquals("F0000071 F0000111 1", transformerId1.toString()),
                () -> assertEquals("F0000211 F0000331 1", transformerId2.toString()),
                () -> assertEquals("F0000111 F0000211 1", lineId1.toString()),
                () -> assertEquals("F0000111 F0000211 2", lineId2.toString())

        );
    }

    @Test
    void testSwitchElementIds() {
        strategy.initialiseNetwork(network);
        Switch sw = network.getSwitch("NGEN-NGEN2");
        Switch sw2 = network.getSwitch("NGEN-NGEN3");
        Switch sw3 = network.getSwitch("NGEN-NGEN3");
        Switch sw4 = network.getSwitch("NGEN-NGEN4");

        UcteElementId swId = strategy.getUcteElementId(sw);
        UcteElementId swId2 = strategy.getUcteElementId(sw2);
        UcteElementId swId3 = strategy.getUcteElementId(sw3);
        UcteElementId swId4 = strategy.getUcteElementId(sw4);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(swId.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(swId2.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(swId3.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(swId4.toString())),
                () -> assertNotEquals(swId, swId2),
                () -> assertNotEquals(swId3, swId4),
                () -> assertNotEquals(swId, swId4),
                () -> assertNotEquals(swId2, swId4),
                () -> assertEquals(swId3, swId2),

                () -> assertEquals("F0000071 F0000072 1", swId.toString()),
                () -> assertEquals("F0000071 F0000073 1", swId2.toString()),
                () -> assertEquals("F0000071 F0000073 1", swId3.toString()),
                () -> assertEquals("F0000071 F0000073 2", swId4.toString())
        );
    }

    @Test
    void testDanglingLineElementIds() {
        strategy.initialiseNetwork(network);
        DanglingLine dl1 = network.getDanglingLine("DL1");
        DanglingLine dl2 = network.getDanglingLine("DL2");
        DanglingLine dl3 = network.getDanglingLine("DL3");
        UcteElementId dlId1 = strategy.getUcteElementId(dl1);
        UcteElementId dlId2 = strategy.getUcteElementId(dl2);
        UcteElementId dlId3 = strategy.getUcteElementId(dl3);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(dlId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(dlId3.toString())),
                () -> assertNotEquals(dlId1, dlId2),
                () -> assertNotEquals(dlId1, dlId3),
                () -> assertNotEquals(dlId2, dlId3),

                () -> assertEquals("F0000072 F0000671 1", dlId1.toString()),
                () -> assertEquals("F0000072 X0000011 1", dlId2.toString()),
                () -> assertEquals("F0000072 F0000671 2", dlId3.toString())
        );
    }

    @Test
    void testParallelLines() {
        strategy.initialiseNetwork(network);

        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId id1 = strategy.getUcteElementId(line1);
        UcteElementId id2 = strategy.getUcteElementId(line2);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(id1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(id2.toString())),
                () -> assertNotEquals(id1, id2),

                () -> assertEquals("F0000111 F0000211 1", id1.toString()),
                () -> assertEquals("F0000111 F0000211 2", id2.toString())
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
