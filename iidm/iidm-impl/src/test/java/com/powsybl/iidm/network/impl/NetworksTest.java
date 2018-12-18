package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
public class NetworksTest {

    @Test
    public void printBalanceSummaryTest()  {

        Network network = EurostagTutorialExample1Factory.create();
        LoggerForTest logger = new LoggerForTest();
        Networks.printBalanceSummary("", network, logger);
        assertEquals("+-----------------------+--------------------------------+----------------------------------+\n" +
                        "|                       | Main CC connected/disconnected | Others CC connected/disconnected |\n" +
                        "+-----------------------+--------------------------------+----------------------------------+\n" +
                        "| Bus count             | 4                              | 0                                |\n" +
                        "| Load count            | 1           | 0                | 0           | 0                  |\n" +
                        "| Load (MW)             | 600.0       | 0.0              | 0.0         | 0.0                |\n" +
                        "| Generator count       | 1           | 0                | 0           | 0                  |\n" +
                        "| Max generation (MW)   | 9999.99     | 0.0              | 0.0         | 0.0                |\n" +
                        "| Generation (MW)       | 607.0       | 0.0              | 0.0         | 0.0                |\n" +
                        "| Shunt at nom V (MVar) | 0.0 0.0 (0) | 0.0 0.0 (0)      | 0.0 0.0 (0) | 0.0 0.0 (0)        |\n" +
                        "+-----------------------+-------------+------------------+-------------+--------------------+" + System.lineSeparator(),
                logger.getContent());
    }
}
