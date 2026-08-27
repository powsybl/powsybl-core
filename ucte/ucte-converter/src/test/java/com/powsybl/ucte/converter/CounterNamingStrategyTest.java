/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.UcteElementId;
import com.powsybl.ucte.network.UcteNodeCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Clément LECLERC {@literal <clement.leclerc@rte-france.com>}
 */
class CounterNamingStrategyTest {

    private Network network;
    private CounterNamingStrategy strategy;
    private NamingStrategy.Context context;

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
        NamingStrategy.Context emptyContext = new CounterNamingStrategy.Context(network);
        assertThrows(UcteException.class, () -> strategy.getUcteNodeCode(emptyContext, "NGEN"));
        assertThrows(UcteException.class, () -> strategy.getUcteElementId(emptyContext, "NHV1_NHV2_1"));

        context = strategy.initialize(network);

        assertDoesNotThrow(() -> strategy.getUcteNodeCode(context, "NGEN"));
        assertDoesNotThrow(() -> strategy.getUcteElementId(context, "NHV1_NHV2_1"));
    }

    @Test
    void testVoltageLevelCounterNaming() {
        context = strategy.initialize(network);

        UcteNodeCode firstBusCode = strategy.getUcteNodeCode(context, network.getBusBreakerView().getBus("NGEN"));
        UcteNodeCode secondBusCode = strategy.getUcteNodeCode(context, network.getBusBreakerView().getBus("NGEN2"));
        UcteNodeCode thirdBusCode = strategy.getUcteNodeCode(context, network.getBusBreakerView().getBus("NGEN3"));

        String firstIdPart = firstBusCode.toString().substring(0, 6);
        String secondIdPart = secondBusCode.toString().substring(0, 6);
        String thirdIdPart = thirdBusCode.toString().substring(0, 6);

        assertEquals(firstIdPart, secondIdPart);
        assertEquals(firstIdPart, thirdIdPart);
        assertEquals(secondIdPart, thirdIdPart);
    }

    @Test
    void testBasicNodeCodeGeneration() {

        context = strategy.initialize(network);
        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        Bus genbus2 = network.getBusBreakerView().getBus("NGEN2");
        Bus ucteBus = network.getBusBreakerView().getBus("F0000079");
        Bus loadBus = network.getBusBreakerView().getBus("NLOAD");

        UcteNodeCode genCode = strategy.getUcteNodeCode(context, genBus);
        UcteNodeCode genCode2 = strategy.getUcteNodeCode(context, genbus2);
        UcteNodeCode loadCode = strategy.getUcteNodeCode(context, loadBus);
        UcteNodeCode ucteCode = strategy.getUcteNodeCode(context, ucteBus);

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

                () -> assertEquals("F0000070", genCode.toString()),
                () -> assertEquals("F0000071", genCode2.toString()),
                () -> assertEquals("F0000073", ucteCode.toString()),
                () -> assertEquals("F0000330", loadCode.toString())

        );
    }

    @Test
    void testBranchElementIds() {
        context = strategy.initialize(network);

        Branch<?> transformer1 = network.getBranch("NGEN_NHV1");
        Branch<?> transformer2 = network.getBranch("NHV2_NLOAD");
        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId transformerId1 = strategy.getUcteElementId(context, transformer1);
        UcteElementId transformerId2 = strategy.getUcteElementId(context, transformer2);
        UcteElementId lineId1 = strategy.getUcteElementId(context, line1);
        UcteElementId lineId2 = strategy.getUcteElementId(context, line2);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(transformerId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(transformerId2.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(lineId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(lineId2.toString())),

                () -> assertNotEquals(transformerId1, transformerId2),
                () -> assertNotEquals(lineId1, lineId2),

                () -> assertEquals(transformerId1, strategy.getUcteElementId(context, transformer1)),
                () -> assertEquals(transformerId2, strategy.getUcteElementId(context, transformer2)),

                () -> assertEquals("F0000070 F0000110 0", transformerId1.toString()),
                () -> assertEquals("F0000210 F0000330 0", transformerId2.toString()),
                () -> assertEquals("F0000110 F0000210 0", lineId1.toString()),
                () -> assertEquals("F0000110 F0000210 1", lineId2.toString())

        );
    }

    @Test
    void testSwitchElementIds() {
        context = strategy.initialize(network);
        Switch sw = network.getSwitch("NGEN-NGEN2");
        Switch sw2 = network.getSwitch("NGEN-NGEN3");
        Switch sw3 = network.getSwitch("NGEN-NGEN3");
        Switch sw4 = network.getSwitch("NGEN-NGEN4");

        UcteElementId swId = strategy.getUcteElementId(context, sw);
        UcteElementId swId2 = strategy.getUcteElementId(context, sw2);
        UcteElementId swId3 = strategy.getUcteElementId(context, sw3);
        UcteElementId swId4 = strategy.getUcteElementId(context, sw4);

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

                () -> assertEquals("F0000070 F0000071 0", swId.toString()),
                () -> assertEquals("F0000070 F0000072 0", swId2.toString()),
                () -> assertEquals("F0000070 F0000072 0", swId3.toString()),
                () -> assertEquals("F0000070 F0000072 1", swId4.toString())
        );
    }

    @Test
    void testBoundaryLineElementIds() {
        context = strategy.initialize(network);
        BoundaryLine dl1 = network.getBoundaryLine("DL1");
        BoundaryLine dl2 = network.getBoundaryLine("DL2");
        BoundaryLine dl3 = network.getBoundaryLine("DL3");
        UcteElementId dlId1 = strategy.getUcteElementId(context, dl1);
        UcteElementId dlId2 = strategy.getUcteElementId(context, dl2);
        UcteElementId dlId3 = strategy.getUcteElementId(context, dl3);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(dlId1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(dlId3.toString())),
                () -> assertNotEquals(dlId1, dlId2),
                () -> assertNotEquals(dlId1, dlId3),
                () -> assertNotEquals(dlId2, dlId3),

                () -> assertEquals("F0000071 F0000670 0", dlId1.toString()),
                () -> assertEquals("F0000071 X0000011 0", dlId2.toString()),
                () -> assertEquals("F0000071 F0000670 1", dlId3.toString())
        );
    }

    @Test
    void testParallelLines() {
        context = strategy.initialize(network);

        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId id1 = strategy.getUcteElementId(context, line1);
        UcteElementId id2 = strategy.getUcteElementId(context, line2);

        assertAll(
                () -> assertTrue(UcteElementId.isUcteElementId(id1.toString())),
                () -> assertTrue(UcteElementId.isUcteElementId(id2.toString())),
                () -> assertNotEquals(id1, id2),

                () -> assertEquals("F0000110 F0000210 0", id1.toString()),
                () -> assertEquals("F0000110 F0000210 1", id2.toString())
        );
    }

    @Test
    void testExistingUcteNodeCodes() {
        context = strategy.initialize(network);

        Bus bus = network.getBusBreakerView().getBus("NGEN");
        UcteNodeCode firstCode = strategy.getUcteNodeCode(context, bus);
        assertNotNull(firstCode);

        UcteNodeCode existingCode = strategy.getUcteNodeCode(context, bus);
        assertNotNull(existingCode);
        assertEquals(firstCode, existingCode);

        UcteNodeCode presentCode = strategy.getUcteNodeCode(context, bus.getId());
        assertNotNull(presentCode);
        assertEquals(firstCode, presentCode);
        assertEquals(existingCode, presentCode);
    }

    @Test
    void testNullAndInvalidIds() {
        context = strategy.initialize(network);

        assertAll(
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteNodeCode(context, (String) null)),
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteElementId(context, (String) null)),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteNodeCode(context, "INVALID_ID")),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteElementId(context, "INVALID_ID"))
        );
    }

    @Test
    void testCountryCode() {
        context = strategy.initialize(network);

        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        UcteNodeCode code = strategy.getUcteNodeCode(context, genBus);
        assertEquals('F', code.toString().charAt(0));
    }
}
