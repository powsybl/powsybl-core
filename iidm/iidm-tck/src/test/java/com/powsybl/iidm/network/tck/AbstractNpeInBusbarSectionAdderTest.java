/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractNpeInBusbarSectionAdderTest {

    @Test
    public void test() {
        Network network = Network.create("test", "code");
        var s = network.newSubstation()
                .setId("S")
                .add();
        var vl = s.newVoltageLevel()
                .setId("VL")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        var bbsAdder = vl.getNodeBreakerView().newBusbarSection()
                .setId("BBS");
        ValidationException exception = assertThrows(ValidationException.class, bbsAdder::add);
        assertEquals("Busbar section 'BBS': node is not set", exception.getMessage());
    }
}
