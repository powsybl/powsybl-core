/**
 * Copyright (c) 2018, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
public abstract class AbstractNetworksTest {

    @Test
    public void printBalanceSummaryTest() throws IOException {
        try (Writer writer = new StringWriter()) {
            Network network = EurostagTutorialExample1Factory.createWithMultipleConnectedComponents();
            Networks.printBalanceSummary("", network, writer);
            assertEquals("Active balance at step '':" + System.lineSeparator() +
                            "+-----------------------+--------------------------------+----------------------------------+\n" +
                            "|                       | Main CC connected/disconnected | Others CC connected/disconnected |\n" +
                            "+-----------------------+--------------------------------+----------------------------------+\n" +
                            "| Bus count             |               4                |                3                 |\n" +
                            "| Load count            | 1           | 0                | 1           | 1                  |\n" +
                            "| Load (MW)             | 600.0       | 0.0              | 600.0       | 600.0              |\n" +
                            "| Generator count       | 1           | 0                | 1           | 1                  |\n" +
                            "| Max generation (MW)   | 9999.99     | 0.0              | 9999.99     | 9999.99            |\n" +
                            "| Generation (MW)       | 607.0       | 0.0              | 607.0       | 607.0              |\n" +
                            "| Shunt at nom V (MVar) | 0.0 0.0 (0) | 0.0 0.0 (0)      | 0.0 0.0 (0) | 0.00576 0.0 (1)    |\n" +
                            "+-----------------------+-------------+------------------+-------------+--------------------+" + System.lineSeparator() +
                            "Connected loads in other CC: [LOAD2]" + System.lineSeparator() +
                            "Disconnected loads in other CC: [LOAD3]" + System.lineSeparator() +
                            "Connected generators in other CC: [GEN2]" + System.lineSeparator() +
                            "Disconnected generators in other CC: [GEN3]" + System.lineSeparator() +
                            "Disconnected shunts in other CC: [SHUNT]" + System.lineSeparator(),
                    writer.toString());
        }
    }
}
