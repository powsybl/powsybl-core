/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.TieLineUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Olivier Perrin <olivier.perrin at rte-france.com>
 */
public class TieLineUtilTest {

    public static final String PAIRING_KEY = "key";
    private static Network network;
    private static DanglingLine dlConnected1;
    private static DanglingLine dlConnected2;
    private static DanglingLine dlDisconnected1;
    private static DanglingLine dlDisconnected2;

    @BeforeAll
    public static void setup() {
        network = mock(Network.class);
        dlConnected1 = createDanglingLine("connected1", true);
        dlConnected2 = createDanglingLine("connected2", true);
        dlDisconnected1 = createDanglingLine("disconnected1", false);
        dlDisconnected2 = createDanglingLine("disconnected2", false);
    }

    @Test
    void testFindCandidateDanglingLinesWithNoConnected() {
        // 0 connected, 0 disconnected
        configureDanglingLines();
        Assertions.assertTrue(getCandidate().isEmpty());

        // 0 connected, 1 disconnected
        configureDanglingLines(dlDisconnected1);
        Assertions.assertEquals(dlDisconnected1, getCandidate().orElse(null));

        // 0 connected, several disconnected
        configureDanglingLines(dlDisconnected1, dlDisconnected2);
        Assertions.assertTrue(getCandidate().isEmpty());
    }

    @Test
    void testFindCandidateDanglingLinesWithOneConnected() {
        // 1 connected, 0 disconnected
        configureDanglingLines(dlConnected1);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));

        // 1 connected, 1 disconnected
        configureDanglingLines(dlConnected1, dlDisconnected1);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));

        // 1 connected, several disconnected
        configureDanglingLines(dlConnected1, dlDisconnected1, dlDisconnected2);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));
    }

    @Test
    void testFindCandidateDanglingLinesWithSeveralConnected() {
        // several connected, 0 disconnected
        configureDanglingLines(dlConnected1, dlConnected2);
        Assertions.assertTrue(getCandidate().isEmpty());

        // several connected, 1 disconnected
        configureDanglingLines(dlConnected1, dlConnected2, dlDisconnected1);
        Assertions.assertTrue(getCandidate().isEmpty());

        // several connected, several disconnected
        configureDanglingLines(dlConnected1, dlConnected2, dlDisconnected1, dlDisconnected2);
        Assertions.assertTrue(getCandidate().isEmpty());
    }

    private static void configureDanglingLines(DanglingLine... danglingLines) {
        when(network.getDanglingLines(DanglingLineFilter.UNPAIRED)).thenReturn(Arrays.asList(danglingLines));
    }

    private static Optional<DanglingLine> getCandidate() {
        return TieLineUtil.findCandidateDanglingLines(network, k -> false).stream().findFirst();
    }

    private static DanglingLine createDanglingLine(String id, boolean connected) {
        DanglingLine dl = mock(DanglingLine.class);
        when(dl.getId()).thenReturn(id);
        when(dl.getPairingKey()).thenReturn(PAIRING_KEY);
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(connected);
        when(dl.getTerminal()).thenReturn(terminal);
        return dl;
    }
}
