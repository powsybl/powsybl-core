/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.modification;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Pauline Jean-Marie {@literal <pauline.jean-marie at artelys.com>}
 */
class TerminalsConnectionModificationTest {

    private Network network;
    private Connectable<?> connectable;
    private Branch<?> branch;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
        connectable = network.getConnectable("NHV1_NHV2_2");
        branch = network.getBranch("NHV1_NHV2_2");
    }

    @Test
    void disconnectAllTerminals() {
        assertTrue(connectable.getTerminals().stream().allMatch(Terminal::isConnected));
        TerminalsConnectionModification modification = new TerminalsConnectionModification("NHV1_NHV2_2", true);
        modification.apply(network);
        assertTrue(connectable.getTerminals().stream().noneMatch(Terminal::isConnected));
    }

    @Test
    void connectOneSide() {
        disconnectAllTerminals();
        assertFalse(branch.getTerminal(TwoSides.TWO).isConnected());
        assertFalse(branch.getTerminal(TwoSides.ONE).isConnected());
        TerminalsConnectionModification modification = new TerminalsConnectionModification("NHV1_NHV2_2", ThreeSides.TWO, false);
        modification.apply(network);
        assertTrue(branch.getTerminal(TwoSides.TWO).isConnected());
        assertFalse(branch.getTerminal(TwoSides.ONE).isConnected());
    }
}
