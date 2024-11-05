/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.events.RemovalNetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractConvertTopologyTest {

    private Network network;
    private VoltageLevel vl;
    private NetworkEventRecorder eventRecorder;

    @BeforeEach
    void setUp() {
        network = FourSubstationsNodeBreakerFactory.create();
        vl = network.getVoltageLevel("S1VL2");
        for (Switch sw : vl.getSwitches()) {
            sw.setRetained(sw.getId().equals("S1VL2_COUPLER"));
        }
        eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
    }

    @Test
    void testNodeBreakerToBusBreaker() {
        var busesNbModel = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(2, busesNbModel.size());
        assertEquals(List.of("S1VL2_0", "S1VL2_1"), busesNbModel.stream().map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL2_BBS1", "TWT", "GH1", "GH2", "GH3", "SHUNT"), busesNbModel.get(0).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(List.of("S1VL2_BBS2", "VSC1", "LD2", "LD3", "LD4", "LCC1"), busesNbModel.get(1).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        vl.convertToTopology(TopologyKind.BUS_BREAKER);

        var busesBbModel = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(2, busesBbModel.size());
        assertEquals(List.of("S1VL2_0", "S1VL2_1"), busesNbModel.stream().map(Identifiable::getId).toList());
        // compare to initial node/breaker model, only difference is that there is no more busbar sections
        assertEquals(List.of("TWT", "GH1", "GH2", "GH3", "SHUNT"), busesBbModel.get(0).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(List.of("VSC1", "LD2", "LD3", "LD4", "LCC1"), busesBbModel.get(1).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        // only retained switches have been kept
        assertEquals(List.of("S1VL2_COUPLER"), vl.getBusBreakerView().getSwitchStream().map(Identifiable::getId).toList());
        assertEquals(List.of(new RemovalNetworkEvent("S1VL2_BBS1_TWT_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_TWT_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_TWT_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_VSC1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_VSC1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_VSC1_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH2_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH3_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH2_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH3_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_GH1_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_GH2_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_GH3_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD2_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD3_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD4_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD2_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD3_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD4_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_LD2_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_LD3_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_LD4_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_SHUNT_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_SHUNT_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_SHUNT_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_LCC1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_LCC1_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_LCC1_BREAKER", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_COUPLER_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS2_COUPLER_DISCONNECTOR", false),
                             new RemovalNetworkEvent("S1VL2_BBS1_TWT_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_TWT_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_TWT_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_VSC1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_VSC1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_VSC1_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH2_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_GH3_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH2_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_GH3_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_GH1_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_GH2_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_GH3_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD2_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD3_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_LD4_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD2_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD3_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_LD4_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_LD2_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_LD3_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_LD4_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_SHUNT_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_SHUNT_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_SHUNT_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_LCC1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_LCC1_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_LCC1_BREAKER", true),
                             new RemovalNetworkEvent("S1VL2_BBS1_COUPLER_DISCONNECTOR", true),
                             new RemovalNetworkEvent("S1VL2_BBS2_COUPLER_DISCONNECTOR", true),
                             new UpdateNetworkEvent("S1VL2", "topologyKind", null, "NODE_BREAKER", "BUS_BREAKER")),
                eventRecorder.getEvents());
    }

    @Test
    void testNodeBreakerToBusBreakerOneElementDisconnected() {
        var gh2 = network.getGenerator("GH2");
        gh2.disconnect();
        assertEquals(List.of(new UpdateNetworkEvent("S1VL2_GH2_BREAKER", "open", "InitialState", false, true)), eventRecorder.getEvents());
        eventRecorder.reset();
        var busesNbModel = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(3, busesNbModel.size());
        assertEquals(List.of("S1VL2_0", "S1VL2_1", "S1VL2_9"), busesNbModel.stream().map(Identifiable::getId).toList());
        assertEquals(List.of("S1VL2_BBS1", "TWT", "GH1", "GH3", "SHUNT"), busesNbModel.get(0).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(List.of("S1VL2_BBS2", "VSC1", "LD2", "LD3", "LD4", "LCC1"), busesNbModel.get(1).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(List.of("GH2"), busesNbModel.get(2).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        vl.convertToTopology(TopologyKind.BUS_BREAKER);
        assertEquals(65, eventRecorder.getEvents().size());

        var busesBbModel = vl.getBusBreakerView().getBusStream().toList();
        assertEquals(3, busesBbModel.size());
        assertEquals(List.of("S1VL2_0", "S1VL2_1", "S1VL2_9"), busesNbModel.stream().map(Identifiable::getId).toList());
        // compare to initial node/breaker model, only difference is that there is no more busbar sections
        assertEquals(List.of("TWT", "GH1", "GH3", "SHUNT"), busesBbModel.get(0).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(List.of("VSC1", "LD2", "LD3", "LD4", "LCC1"), busesBbModel.get(1).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals(Collections.emptyList(), busesBbModel.get(2).getConnectedTerminalStream().map(t -> t.getConnectable().getId()).toList());
        assertEquals("S1VL2_9", gh2.getTerminal().getBusBreakerView().getConnectableBus().getId());
        assertFalse(gh2.getTerminal().isConnected());
        // only retained switches have been kept
        assertEquals(List.of("S1VL2_COUPLER"), vl.getBusBreakerView().getSwitchStream().map(Identifiable::getId).toList());
    }
}
