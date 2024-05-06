/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.DefaultNetworkListener;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.FourSubstationsNodeBreakerFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractConnectionNotificationTest {

    record Event(String id, String attribute, String variantId, Object oldValue, Object newValue) {
    }

    static class EventsRecorder extends DefaultNetworkListener {

        private final List<Event> events = new ArrayList<>();

        public EventsRecorder(Network network) {
            network.addListener(this);
        }

        @Override
        public void onUpdate(Identifiable<?> identifiable, String attribute, Object oldValue, Object newValue) {
            events.add(new Event(identifiable.getId(), attribute, null, oldValue, newValue));
        }

        @Override
        public void onUpdate(Identifiable<?> identifiable, String attribute, String variantId, Object oldValue, Object newValue) {
            events.add(new Event(identifiable.getId(), attribute, variantId, oldValue, newValue));
        }

        List<Event> getEvents() {
            return events;
        }

        void clear() {
            events.clear();
        }
    }

    @Test
    void busBreakerTest() {
        var network = EurostagTutorialExample1Factory.create();
        EventsRecorder eventsRecorder = new EventsRecorder(network);
        var l1 = network.getLine("NHV1_NHV2_1");

        assertTrue(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new Event("NHV1_NHV2_1", "beginDisconnect", "InitialState", false, null),
                new Event("NHV1_NHV2_1", "connected1", "InitialState", true, false),
                new Event("NHV1_NHV2_1", "endDisconnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        // try with an already disconnected terminal
        eventsRecorder.clear();
        assertFalse(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                        new Event("NHV1_NHV2_1", "beginDisconnect", "InitialState", true, null),
                        new Event("NHV1_NHV2_1", "endDisconnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        eventsRecorder.clear();
        assertTrue(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new Event("NHV1_NHV2_1", "beginConnect", "InitialState", false, null),
                        new Event("NHV1_NHV2_1", "connected1", "InitialState", false, true),
                        new Event("NHV1_NHV2_1", "endConnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        // try with an already connected terminal
        eventsRecorder.clear();
        assertFalse(l1.getTerminal1().connect()); // no action has been done
        assertEquals(List.of(
                        new Event("NHV1_NHV2_1", "beginConnect", "InitialState", true, null),
                        new Event("NHV1_NHV2_1", "endConnect", "InitialState", null, true)),
                eventsRecorder.getEvents());
    }

    @Test
    void nodeBreakerTest() {
        var network = FourSubstationsNodeBreakerFactory.create();
        EventsRecorder eventsRecorder = new EventsRecorder(network);
        var l1 = network.getLine("LINE_S2S3");

        assertTrue(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                new Event("LINE_S2S3", "beginDisconnect", "InitialState", false, null),
                new Event("S2VL1_LINES2S3_BREAKER", "open", "InitialState", false, true),
                new Event("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        eventsRecorder.clear();
        assertFalse(l1.getTerminal1().disconnect());
        assertEquals(List.of(
                        new Event("LINE_S2S3", "beginDisconnect", "InitialState", true, null),
                        new Event("LINE_S2S3", "endDisconnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        eventsRecorder.clear();
        assertTrue(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new Event("LINE_S2S3", "beginConnect", "InitialState", false, null),
                        new Event("S2VL1_LINES2S3_BREAKER", "open", "InitialState", true, false),
                        new Event("LINE_S2S3", "endConnect", "InitialState", null, true)),
                eventsRecorder.getEvents());

        eventsRecorder.clear();
        assertFalse(l1.getTerminal1().connect());
        assertEquals(List.of(
                        new Event("LINE_S2S3", "beginConnect", "InitialState", true, null),
                        new Event("LINE_S2S3", "endConnect", "InitialState", null, true)),
                eventsRecorder.getEvents());
    }
}
