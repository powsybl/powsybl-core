/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractNpeWhenAdding3wtOrLineAndNodeAlreadyUsed {

    @Test
    public void shouldNotThrowNpe() {
        Network network = Network.create("test", "");
        Substation s = network.newSubstation()
                .setId("S1")
                .add();
        s.newVoltageLevel()
                .setId("VL1")
                .setNominalV(1)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel vl2 = s.newVoltageLevel()
                .setId("VL2")
                .setNominalV(1)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BBS")
                .setNode(0)
                .add();
        s.newVoltageLevel()
                .setId("VL3")
                .setNominalV(1)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        var twtAdder = s.newThreeWindingsTransformer()
                .setId("TR")
                .newLeg1()
                    .setVoltageLevel("VL1")
                    .setNode(0) // already taken by BBS
                    .setRatedU(1)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                .add()
                .newLeg2()
                    .setVoltageLevel("VL2")
                    .setNode(0)
                    .setRatedU(1)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                .add()
                .newLeg3()
                    .setVoltageLevel("VL3")
                    .setNode(0)
                    .setRatedU(1)
                    .setR(0)
                    .setX(0)
                    .setG(0)
                    .setB(0)
                .add();
        ValidationException e = assertThrows(ValidationException.class, twtAdder::add);
        assertEquals("3 windings transformer 'TR': an equipment (BBS) is already connected to node 0 of voltage level VL2", e.getMessage());

        var lineAdder = network.newLine()
                .setId("L")
                .setVoltageLevel1("VL1")
                .setNode1(0) // already taken by BBS
                .setVoltageLevel2("VL2")
                .setNode2(0)
                .setR(0)
                .setX(0)
                .setG1(0)
                .setG2(0)
                .setB1(0)
                .setB2(0);
        e = assertThrows(ValidationException.class, lineAdder::add);
        assertEquals("AC line 'L': an equipment (BBS) is already connected to node 0 of voltage level VL2", e.getMessage());
    }
}
