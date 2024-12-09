/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractMoveConnectableNotifTest {

    @Test
    public void nodeBreakerTest() {
        var network = FictitiousSwitchFactory.create();
        MutableObject<Object> obj = new MutableObject<>();
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
                obj.setValue(newValue);
            }
        });
        Load cf = network.getLoad("CF");
        cf.getTerminal().getNodeBreakerView().moveConnectable(3, "C");
        assertNotNull(obj.getValue());
        assertInstanceOf(NodeTopologyPoint.class, obj.getValue());
        NodeTopologyPoint topologyPoint = (NodeTopologyPoint) obj.getValue();
        assertSame(TopologyKind.NODE_BREAKER, topologyPoint.getTopologyKind());
        assertEquals("C", topologyPoint.getVoltageLevelId());
        assertEquals(3, topologyPoint.getNode());
    }

    @Test
    public void busBreakerTest() {
        var network = EurostagTutorialExample1Factory.create();
        MutableObject<Object> obj = new MutableObject<>();
        network.addListener(new DefaultNetworkListener() {
            @Override
            public void onUpdate(Identifiable identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
                obj.setValue(newValue);
            }
        });
        Load load = network.getLoad("LOAD");
        load.getTerminal().getBusBreakerView().moveConnectable("NGEN", true);
        assertNotNull(obj.getValue());
        assertInstanceOf(BusTopologyPoint.class, obj.getValue());
        BusTopologyPoint topologyPoint = (BusTopologyPoint) obj.getValue();
        assertSame(TopologyKind.BUS_BREAKER, topologyPoint.getTopologyKind());
        assertEquals("VLGEN", topologyPoint.getVoltageLevelId());
        assertEquals("NGEN", topologyPoint.getConnectableBusId());
        assertTrue(topologyPoint.isConnected());
    }
}
