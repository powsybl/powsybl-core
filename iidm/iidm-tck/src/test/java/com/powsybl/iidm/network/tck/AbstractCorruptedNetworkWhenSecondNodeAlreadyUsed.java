/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractCorruptedNetworkWhenSecondNodeAlreadyUsed {

    @Test
    public void shouldNotInsertTheTransformer() {
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
        try {
            s.newTwoWindingsTransformer()
                    .setId("TR")
                    .setVoltageLevel1("VL1")
                    .setNode1(0)
                    .setVoltageLevel2("VL2")
                    .setNode2(0) // already taken by BBS
                    .setR(0)
                    .setX(1)
                    .setB(1)
                    .setG(0)
                    .setRatedU1(1)
                    .setRatedU2(1)
                    .add();
        } catch (PowsyblException ignored) {
        }

        TwoWindingsTransformer twt = network.getTwoWindingsTransformer("TR");
        assertNull(twt);
        assertEquals(0, s.getTwoWindingsTransformerCount());
    }
}
