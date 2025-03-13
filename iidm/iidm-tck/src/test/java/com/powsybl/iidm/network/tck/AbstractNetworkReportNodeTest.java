/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.report.PowsyblCoreReportResourceBundles;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReportNodeContext;
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
    public void executeWithReportNodeTest() {
        // Create a network and affect it a reportNode (reportNode1)
        Network network = EurostagTutorialExample1Factory.create();
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("key1")
                .build();
        network.getReportNodeContext().pushReportNode(reportNode1);
        assertTrue(reportNode1.getChildren().isEmpty());

        // Create another reportNode (reportNode2)
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("key2")
                .build();
        assertTrue(reportNode2.getChildren().isEmpty());

        // Execute a task using reportNode2
        Networks.executeWithReportNode(network, reportNode2, getTask(network));

        // The network's reportNode is still reportNode1
        assertEquals(reportNode1, network.getReportNodeContext().getReportNode());
        // reportNode1 wasn't affected
        assertTrue(reportNode1.getChildren().isEmpty());
        // reportNode2 was used by the task
        assertEquals(1, reportNode2.getChildren().size());
    }

    private Runnable getTask(Network network) {
        return () -> network.getReportNodeContext().getReportNode().newReportNode()
                .withMessageTemplate("reportKey")
                .add();
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        // Create a network and affect it a reportNode (reportNode1)
        Network network = EurostagTutorialExample1Factory.create();
        ReportNode reportNode1 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("key1")
                .build();
        network.getReportNodeContext().pushReportNode(reportNode1);
        assertTrue(reportNode1.getChildren().isEmpty());

        // Create 2 other reportNodes (reportNode2 and reportNode3)
        ReportNode reportNode2 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("key2")
                .build();
        ReportNode reportNode3 = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("key3")
                .build();
        assertTrue(reportNode2.getChildren().isEmpty());
        assertTrue(reportNode3.getChildren().isEmpty());

        // Switch in multi-thread management
        network.allowReportNodeContextMultiThreadAccess(true);

        final CountDownLatch latch = new CountDownLatch(2); // to sync threads after having set the working variant
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.invokeAll(Arrays.asList(
                () -> {
                    network.getReportNodeContext().pushReportNode(reportNode2);
                    try {
                        latch.countDown();
                        latch.await();
                        network.getReportNodeContext().getReportNode().newReportNode()
                                .withMessageTemplate("key2")
                                .add();
                    } finally {
                        network.getReportNodeContext().popReportNode();
                    }
                    return null;
                },
                () -> {
                    network.getReportNodeContext().pushReportNode(reportNode3);
                    try {
                        latch.countDown();
                        latch.await();
                        network.getReportNodeContext().getReportNode().newReportNode()
                                .withMessageTemplate("key3")
                                .add();
                    } finally {
                        network.getReportNodeContext().popReportNode();
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
        network.allowReportNodeContextMultiThreadAccess(false);

        // The network's reportNode is still reportNode1
        assertEquals(reportNode1, network.getReportNodeContext().getReportNode());
        // reportNode1 wasn't affected
        assertTrue(reportNode1.getChildren().isEmpty());
        // reportNode2 was used by the 1st task
        assertEquals(1, reportNode2.getChildren().size());
        assertEquals("key2", reportNode2.getChildren().stream().findFirst().orElseThrow().getMessageKey());
        // reportNode3 was used by the 2nd task
        assertEquals(1, reportNode3.getChildren().size());
        assertEquals("key3", reportNode3.getChildren().stream().findFirst().orElseThrow().getMessageKey());
    }

    @Test
    public void onSubnetworkTest() {
        // Create a network and affect it a reportNode (reportNode1)
        Network network = Network.create("Root", "format0");
        ReportNodeContext reportNodeContext1 = network.getReportNodeContext();

        // Create a subnetwork
        Network subnetwork = network.createSubnetwork("Sub1", "Sub1", "format1");

        // Check that the retrieved reportNode context is the same as the root network
        assertEquals(reportNodeContext1, subnetwork.getReportNodeContext());

        // Context change on the network/subnetwork is reflected to the other
        network.allowReportNodeContextMultiThreadAccess(true);
        ReportNodeContext reportNodeContext2 = network.getReportNodeContext();
        assertNotEquals(reportNodeContext1, reportNodeContext2);
        assertEquals(reportNodeContext2, subnetwork.getReportNodeContext());

        subnetwork.allowReportNodeContextMultiThreadAccess(false);
        ReportNodeContext reportNodeContext3 = subnetwork.getReportNodeContext();
        assertNotEquals(reportNodeContext2, reportNodeContext3);
        assertEquals(reportNodeContext3, network.getReportNodeContext());
    }

}
