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
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public class TieLineUtilTest {

    public static final String PAIRING_KEY = "key";
    private static Network network;
    private static BoundaryLine dlConnected1;
    private static BoundaryLine dlConnected2;
    private static BoundaryLine dlDisconnected1;
    private static BoundaryLine dlDisconnected2;

    @BeforeAll
    public static void setup() {
        network = mock(Network.class);
        dlConnected1 = createBoundaryLine("connected1", true);
        dlConnected2 = createBoundaryLine("connected2", true);
        dlDisconnected1 = createBoundaryLine("disconnected1", false);
        dlDisconnected2 = createBoundaryLine("disconnected2", false);
    }

    @Test
    void testFindCandidateBoundaryLinesWithNoConnected() {
        // 0 connected, 0 disconnected
        configureBoundaryLines();
        Assertions.assertTrue(getCandidate().isEmpty());

        // 0 connected, 1 disconnected
        configureBoundaryLines(dlDisconnected1);
        Assertions.assertEquals(dlDisconnected1, getCandidate().orElse(null));

        // 0 connected, several disconnected
        configureBoundaryLines(dlDisconnected1, dlDisconnected2);
        Assertions.assertTrue(getCandidate().isEmpty());
    }

    @Test
    void testFindCandidateBoundaryLinesWithOneConnected() {
        // 1 connected, 0 disconnected
        configureBoundaryLines(dlConnected1);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));

        // 1 connected, 1 disconnected
        configureBoundaryLines(dlConnected1, dlDisconnected1);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));

        // 1 connected, several disconnected
        configureBoundaryLines(dlConnected1, dlDisconnected1, dlDisconnected2);
        Assertions.assertEquals(dlConnected1, getCandidate().orElse(null));
    }

    @Test
    void testFindCandidateBoundaryLinesWithSeveralConnected() {
        // several connected, 0 disconnected
        configureBoundaryLines(dlConnected1, dlConnected2);
        Assertions.assertTrue(getCandidate().isEmpty());

        // several connected, 1 disconnected
        configureBoundaryLines(dlConnected1, dlConnected2, dlDisconnected1);
        Assertions.assertTrue(getCandidate().isEmpty());

        // several connected, several disconnected
        configureBoundaryLines(dlConnected1, dlConnected2, dlDisconnected1, dlDisconnected2);
        Assertions.assertTrue(getCandidate().isEmpty());
    }

    private static void configureBoundaryLines(BoundaryLine... boundaryLines) {
        when(network.getBoundaryLines(BoundaryLineFilter.UNPAIRED)).thenReturn(Arrays.asList(boundaryLines));
    }

    private static Optional<BoundaryLine> getCandidate() {
        return TieLineUtil.findCandidateBoundaryLines(network, k -> false).stream().findFirst();
    }

    private static BoundaryLine createBoundaryLine(String id, boolean connected) {
        BoundaryLine dl = mock(BoundaryLine.class);
        when(dl.getId()).thenReturn(id);
        when(dl.getPairingKey()).thenReturn(PAIRING_KEY);
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(connected);
        when(dl.getTerminal()).thenReturn(terminal);
        return dl;
    }
}
