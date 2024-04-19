/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.tasks;

import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.modification.NetworkModification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class GeneratorTrippingTest {

    @Test
    void generatorTrippingTest() {
        Network network = EurostagTutorialExample1Factory.create();
        assertTrue(network.getGenerator("GEN").getTerminal().isConnected());

        Contingency contingency = Contingency.generator("GEN");

        NetworkModification task = contingency.toModification();
        task.apply(network);

        assertFalse(network.getGenerator("GEN").getTerminal().isConnected());
    }
}
