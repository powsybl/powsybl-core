/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractConnectionNotificationTest {

    @Test
    public void busBreakerTest() {
        var network = EurostagTutorialExample1Factory.create();
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        var l1 = network.getLine("NHV1_NHV2_1");

        assertTrue(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new UpdateNetworkEvent("NHV1_NHV2_1", "beginDisconnect", "InitialState", false, null),
                new UpdateNetworkEvent("NHV1_NHV2_1", "connected1", "InitialState", true, false),
                new UpdateNetworkEvent("NHV1_NHV2_1", "endDisconnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        // try with an already disconnected terminal
        eventRecorder.reset();
        assertFalse(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                        new UpdateNetworkEvent("NHV1_NHV2_1", "beginDisconnect", "InitialState", true, null),
                        new UpdateNetworkEvent("NHV1_NHV2_1", "endDisconnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        assertTrue(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new UpdateNetworkEvent("NHV1_NHV2_1", "beginConnect", "InitialState", false, null),
                        new UpdateNetworkEvent("NHV1_NHV2_1", "connected1", "InitialState", false, true),
                        new UpdateNetworkEvent("NHV1_NHV2_1", "endConnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        // try with an already connected terminal
        eventRecorder.reset();
        assertFalse(l1.getTerminal1().connect()); // no action has been done
        assertEquals(List.of(
                        new UpdateNetworkEvent("NHV1_NHV2_1", "beginConnect", "InitialState", true, null),
                        new UpdateNetworkEvent("NHV1_NHV2_1", "endConnect", "InitialState", null, true)),
                eventRecorder.getEvents());
    }

    @Test
    public void nodeBreakerTest() {
        var network = FourSubstationsNodeBreakerFactory.create();
        NetworkEventRecorder eventRecorder = new NetworkEventRecorder();
        network.addListener(eventRecorder);
        var l1 = network.getLine("LINE_S2S3");

        assertTrue(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new UpdateNetworkEvent("LINE_S2S3", "beginDisconnect", "InitialState", false, null),
                new UpdateNetworkEvent("S2VL1_LINES2S3_BREAKER", "open", "InitialState", false, true),
                new UpdateNetworkEvent("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        assertFalse(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                        new UpdateNetworkEvent("LINE_S2S3", "beginDisconnect", "InitialState", true, null),
                        new UpdateNetworkEvent("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        assertTrue(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new UpdateNetworkEvent("LINE_S2S3", "beginConnect", "InitialState", false, null),
                        new UpdateNetworkEvent("S2VL1_LINES2S3_BREAKER", "open", "InitialState", true, false),
                        new UpdateNetworkEvent("LINE_S2S3", "endConnect", "InitialState", null, true)),
                eventRecorder.getEvents());

        eventRecorder.reset();
        assertFalse(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new UpdateNetworkEvent("LINE_S2S3", "beginConnect", "InitialState", true, null),
                        new UpdateNetworkEvent("LINE_S2S3", "endConnect", "InitialState", null, true)),
                eventRecorder.getEvents());
    }
}
