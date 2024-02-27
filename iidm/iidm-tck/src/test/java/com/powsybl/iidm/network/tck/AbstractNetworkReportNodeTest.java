/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.reporter.ReportNodeImpl;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReporterContext;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractNetworkReportNodeTest {

    @Test
    public void executeWithReporterTest() {
        // Create a network and affect it a reporter (reporter1)
        Network network = EurostagTutorialExample1Factory.create();
        ReportNodeImpl reporter1 = new ReportNodeImpl("key1", "name1");
        network.getReporterContext().pushReporter(reporter1);
        assertTrue(reporter1.getChildren().isEmpty());

        // Create another reporter (reporter2)
        ReportNodeImpl reporter2 = new ReportNodeImpl("key2", "name2");
        assertTrue(reporter2.getChildren().isEmpty());

        // Execute a task using reporter2
        Networks.executeWithReporter(network, reporter2, getTask(network));

        // The network's reporter is still reporter1
        assertEquals(reporter1, network.getReporterContext().getReporter());
        // reporter1 wasn't affected
        assertTrue(reporter1.getChildren().isEmpty());
        // reporter2 was used by the task
        assertEquals(1, reporter2.getChildren().size());
    }

    private Runnable getTask(Network network) {
        return () -> network.getReporterContext().getReporter().newReportNode().withKey("reportKey").withMessageTemplate("message").add();
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        // Create a network and affect it a reporter (reporter1)
        Network network = EurostagTutorialExample1Factory.create();
        ReportNodeImpl reporter1 = new ReportNodeImpl("key1", "name1");
        network.getReporterContext().pushReporter(reporter1);
        assertTrue(reporter1.getChildren().isEmpty());

        // Create 2 other reporters (reporter2 and reporter3)
        ReportNodeImpl reporter2 = new ReportNodeImpl("key2", "name2");
        ReportNodeImpl reporter3 = new ReportNodeImpl("key3", "name3");
        assertTrue(reporter2.getChildren().isEmpty());
        assertTrue(reporter3.getChildren().isEmpty());

        // Switch in multi-thread management
        network.allowReporterContextMultiThreadAccess(true);

        final CountDownLatch latch = new CountDownLatch(2); // to sync threads after having set the working variant
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.invokeAll(Arrays.asList(
                () -> {
                    network.getReporterContext().pushReporter(reporter2);
                    try {
                        latch.countDown();
                        latch.await();
                        network.getReporterContext().getReporter().newReportNode().withKey("key2").withMessageTemplate("message2").add();
                    } finally {
                        network.getReporterContext().popReporter();
                    }
                    return null;
                },
                () -> {
                    network.getReporterContext().pushReporter(reporter3);
                    try {
                        latch.countDown();
                        latch.await();
                        network.getReporterContext().getReporter().newReportNode().withKey("key3").withMessageTemplate("message3").add();
                    } finally {
                        network.getReporterContext().popReporter();
                    }
                    return null;
                })
        );
        service.shutdown();
        boolean finished = service.awaitTermination(20, TimeUnit.SECONDS);
        if (!finished) {
            fail("Error executing test");
        }

        // Switch back to mono-thread management
        network.allowReporterContextMultiThreadAccess(false);

        // The network's reporter is still reporter1
        assertEquals(reporter1, network.getReporterContext().getReporter());
        // reporter1 wasn't affected
        assertTrue(reporter1.getChildren().isEmpty());
        // reporter2 was used by the 1st task
        assertEquals(1, reporter2.getChildren().size());
        assertEquals("key2", reporter2.getChildren().stream().findFirst().orElseThrow().getKey());
        // reporter3 was used by the 2nd task
        assertEquals(1, reporter3.getChildren().size());
        assertEquals("key3", reporter3.getChildren().stream().findFirst().orElseThrow().getKey());
    }

    @Test
    public void onSubnetworkTest() {
        // Create a network and affect it a reporter (reporter1)
        Network network = Network.create("Root", "format0");
        ReporterContext reporterContext1 = network.getReporterContext();

        // Create a subnetwork
        Network subnetwork = network.createSubnetwork("Sub1", "Sub1", "format1");

        // Check that the retrieved reporter context is the same as the root network
        assertEquals(reporterContext1, subnetwork.getReporterContext());

        // Context change on the network/subnetwork is reflected to the other
        network.allowReporterContextMultiThreadAccess(true);
        ReporterContext reporterContext2 = network.getReporterContext();
        assertNotEquals(reporterContext1, reporterContext2);
        assertEquals(reporterContext2, subnetwork.getReporterContext());

        subnetwork.allowReporterContextMultiThreadAccess(false);
        ReporterContext reporterContext3 = subnetwork.getReporterContext();
        assertNotEquals(reporterContext2, reporterContext3);
        assertEquals(reporterContext3, network.getReporterContext());
    }

}
