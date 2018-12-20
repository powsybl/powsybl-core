/**
 * Copyright (c) 2018, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;

import static org.junit.Assert.assertEquals;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public class NetworksTest {

    @Test
    public void printBalanceSummaryTest()  {
        StringBuilder buffer = new StringBuilder();

        Logger logger = Mockito.mock(Logger.class);
        Mockito.when(logger.isDebugEnabled()).thenReturn(true);
        Mockito.doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            FormattingTuple formatter = MessageFormatter.format(Objects.toString(args[0]), args[1], args[2]);
            buffer.append(formatter.getMessage());
            return null;
        }).when(logger).debug(Mockito.anyString(), Mockito.any(), Mockito.any());

        Network network = EurostagTutorialExample1Factory.create();
        Networks.printBalanceSummary("", network, logger);
        assertEquals("Active balance at step '':\n" +
                     "+-----------------------+--------------------------------+----------------------------------+\n" +
                     "|                       | Main CC connected/disconnected | Others CC connected/disconnected |\n" +
                     "+-----------------------+--------------------------------+----------------------------------+\n" +
                     "| Bus count             |               4                |                0                 |\n" +
                     "| Load count            | 1           | 0                | 0           | 0                  |\n" +
                     "| Load (MW)             | 600.0       | 0.0              | 0.0         | 0.0                |\n" +
                     "| Generator count       | 1           | 0                | 0           | 0                  |\n" +
                     "| Max generation (MW)   | 9999.99     | 0.0              | 0.0         | 0.0                |\n" +
                     "| Generation (MW)       | 607.0       | 0.0              | 0.0         | 0.0                |\n" +
                     "| Shunt at nom V (MVar) | 0.0 0.0 (0) | 0.0 0.0 (0)      | 0.0 0.0 (0) | 0.0 0.0 (0)        |\n" +
                     "+-----------------------+-------------+------------------+-------------+--------------------+" + System.lineSeparator(),
                buffer.toString());
    }
}
