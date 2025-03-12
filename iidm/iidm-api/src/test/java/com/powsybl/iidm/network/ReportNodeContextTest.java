/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.report.ReportBundleBaseName;
import com.powsybl.commons.report.ReportNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class ReportNodeContextTest {

    private static SimpleReportNodeContext simpleContext;
    private static MultiThreadReportNodeContext multiThreadContext;

    @BeforeEach
    void init() {
        simpleContext = new SimpleReportNodeContext();
        multiThreadContext = new MultiThreadReportNodeContext();
    }

    @AfterEach
    void tearDown() {
        multiThreadContext.close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void getReportNodeOnEmptyContextTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();
        assertEquals(ReportNode.NO_OP, reportNodeContext.getReportNode());
        assertEquals(ReportNode.NO_OP, reportNodeContext.peekReportNode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void pushAndGetReportNodeTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();
        ReportNode reportNode0 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("task0")
                .build();
        reportNodeContext.pushReportNode(reportNode0);
        assertEquals(reportNode0, reportNodeContext.getReportNode());
        assertEquals(reportNode0, reportNodeContext.peekReportNode());

        ReportNode reportNode1 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("task1")
                .build();
        reportNodeContext.pushReportNode(reportNode1);
        assertEquals(reportNode1, reportNodeContext.getReportNode());
        assertEquals(reportNode1, reportNodeContext.peekReportNode());

        // Several get (or peek) doesn't affect the reportNode
        assertEquals(reportNode1, reportNodeContext.getReportNode());
        assertEquals(reportNode1, reportNodeContext.peekReportNode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void popReportNodeTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();

        ReportNode reportNode0 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("task0")
                .build();
        ReportNode reportNode1 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("task1")
                .build();
        reportNodeContext.pushReportNode(reportNode0);
        reportNodeContext.pushReportNode(reportNode1);
        assertEquals(reportNode1, reportNodeContext.getReportNode());
        // Pop
        ReportNode poppedReportNode = reportNodeContext.popReportNode();
        assertEquals(reportNode1, poppedReportNode);
        assertEquals(reportNode0, reportNodeContext.getReportNode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void popOnEmptyReportNodeContextTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();

        assert ReportNode.NO_OP.equals(reportNodeContext.getReportNode());
        ReportNode poppedReportNode = reportNodeContext.popReportNode();
        assertEquals(ReportNode.NO_OP, poppedReportNode);
        assertEquals(ReportNode.NO_OP, reportNodeContext.getReportNode());

        // Empty context can be popped several times
        poppedReportNode = reportNodeContext.popReportNode();
        assertEquals(ReportNode.NO_OP, poppedReportNode);
        assertEquals(ReportNode.NO_OP, reportNodeContext.getReportNode());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void copyEmptyReportNodeContextTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();
        assert ReportNode.NO_OP.equals(reportNodeContext.getReportNode());

        // Simple
        ReportNodeContext copy = new SimpleReportNodeContext((AbstractReportNodeContext) reportNodeContext);
        Assertions.assertEquals(SimpleReportNodeContext.class, copy.getClass());
        Assertions.assertEquals(ReportNode.NO_OP, copy.getReportNode());

        // Multi-thread
        copy = new MultiThreadReportNodeContext((AbstractReportNodeContext) reportNodeContext);
        Assertions.assertEquals(MultiThreadReportNodeContext.class, copy.getClass());
        Assertions.assertEquals(ReportNode.NO_OP, copy.getReportNode());

        // Clean
        ((MultiThreadReportNodeContext) copy).close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReportNodeContextStream")
    void copyNonEmptyReportNodeContextTest(String desc, Supplier<ReportNodeContext> contextSupplier) {
        ReportNodeContext reportNodeContext = contextSupplier.get();
        assert ReportNode.NO_OP.equals(reportNodeContext.getReportNode());

        // Push reportNodes in the context
        ReportNode reportNode1 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("1")
                .build();
        reportNodeContext.pushReportNode(reportNode1);
        ReportNode reportNode2 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("2")
                .build();
        reportNodeContext.pushReportNode(reportNode2);
        ReportNode reportNode3 = ReportNode.newRootReportNode(ReportBundleBaseName.BUNDLE_TEST_BASE_NAME)
                .withMessageTemplate("3")
                .build();
        reportNodeContext.pushReportNode(reportNode3);

        // Create copies
        SimpleReportNodeContext simpleCopy = new SimpleReportNodeContext((AbstractReportNodeContext) reportNodeContext);
        Assertions.assertEquals(SimpleReportNodeContext.class, simpleCopy.getClass());
        MultiThreadReportNodeContext multiThreadCopy = new MultiThreadReportNodeContext((AbstractReportNodeContext) reportNodeContext);
        Assertions.assertEquals(MultiThreadReportNodeContext.class, multiThreadCopy.getClass());

        // Test copies' reportNodes (pop and test all of them)
        Assertions.assertEquals(reportNode3, simpleCopy.getReportNode());
        Assertions.assertEquals(reportNode3, multiThreadCopy.getReportNode());

        simpleCopy.popReportNode();
        multiThreadCopy.popReportNode();
        Assertions.assertEquals(reportNode2, simpleCopy.getReportNode());
        Assertions.assertEquals(reportNode2, multiThreadCopy.getReportNode());

        simpleCopy.popReportNode();
        multiThreadCopy.popReportNode();
        Assertions.assertEquals(reportNode1, simpleCopy.getReportNode());
        Assertions.assertEquals(reportNode1, multiThreadCopy.getReportNode());

        simpleCopy.popReportNode();
        multiThreadCopy.popReportNode();
        Assertions.assertEquals(ReportNode.NO_OP, simpleCopy.getReportNode());
        Assertions.assertEquals(ReportNode.NO_OP, multiThreadCopy.getReportNode());

        // Clean
        multiThreadCopy.close();
    }

    static Stream<Arguments> getReportNodeContextStream() {
        return Stream.of(
                Arguments.of("Simple",
                        (Supplier<ReportNodeContext>) () -> simpleContext),
                Arguments.of("Multi-thread",
                        (Supplier<ReportNodeContext>) () -> multiThreadContext)
        );
    }

}
