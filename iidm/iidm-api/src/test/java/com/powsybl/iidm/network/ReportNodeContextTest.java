/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.ReportNode;
import com.powsybl.commons.reporter.ReportNodeModel;
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

    private static SimpleReporterContext simpleContext;
    private static MultiThreadReporterContext multiThreadContext;

    @BeforeEach
    void init() {
        simpleContext = new SimpleReporterContext();
        multiThreadContext = new MultiThreadReporterContext();
    }

    @AfterEach
    void tearDown() {
        multiThreadContext.close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void getReporterOnEmptyContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        assertEquals(ReportNode.NO_OP, reporterContext.getReporter());
        assertEquals(ReportNode.NO_OP, reporterContext.peekReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void pushAndGetReporterTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        ReportNodeModel reporter0 = new ReportNodeModel("task0", "name0");
        reporterContext.pushReporter(reporter0);
        assertEquals(reporter0, reporterContext.getReporter());
        assertEquals(reporter0, reporterContext.peekReporter());

        ReportNodeModel reporter1 = new ReportNodeModel("task1", "name1");
        reporterContext.pushReporter(reporter1);
        assertEquals(reporter1, reporterContext.getReporter());
        assertEquals(reporter1, reporterContext.peekReporter());

        // Several get (or peek) doesn't affect the reporter
        assertEquals(reporter1, reporterContext.getReporter());
        assertEquals(reporter1, reporterContext.peekReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void popReporterTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();

        ReportNodeModel reporter0 = new ReportNodeModel("task0", "name0");
        ReportNodeModel reporter1 = new ReportNodeModel("task1", "name1");
        reporterContext.pushReporter(reporter0);
        reporterContext.pushReporter(reporter1);
        assertEquals(reporter1, reporterContext.getReporter());
        // Pop
        ReportNode poppedReportNode = reporterContext.popReporter();
        assertEquals(reporter1, poppedReportNode);
        assertEquals(reporter0, reporterContext.getReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void popOnEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();

        assert ReportNode.NO_OP.equals(reporterContext.getReporter());
        ReportNode poppedReportNode = reporterContext.popReporter();
        assertEquals(ReportNode.NO_OP, poppedReportNode);
        assertEquals(ReportNode.NO_OP, reporterContext.getReporter());

        // Empty context can be popped several times
        poppedReportNode = reporterContext.popReporter();
        assertEquals(ReportNode.NO_OP, poppedReportNode);
        assertEquals(ReportNode.NO_OP, reporterContext.getReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void copyEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        assert ReportNode.NO_OP.equals(reporterContext.getReporter());

        // Simple
        ReporterContext copy = new SimpleReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(SimpleReporterContext.class, copy.getClass());
        Assertions.assertEquals(ReportNode.NO_OP, copy.getReporter());

        // Multi-thread
        copy = new MultiThreadReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(MultiThreadReporterContext.class, copy.getClass());
        Assertions.assertEquals(ReportNode.NO_OP, copy.getReporter());

        // Clean
        ((MultiThreadReporterContext) copy).close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void copyNonEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        assert ReportNode.NO_OP.equals(reporterContext.getReporter());

        // Push reporters in the context
        ReportNode reportNode1 = new ReportNodeModel("1", "1");
        reporterContext.pushReporter(reportNode1);
        ReportNode reportNode2 = new ReportNodeModel("2", "2");
        reporterContext.pushReporter(reportNode2);
        ReportNode reportNode3 = new ReportNodeModel("3", "3");
        reporterContext.pushReporter(reportNode3);

        // Create copies
        SimpleReporterContext simpleCopy = new SimpleReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(SimpleReporterContext.class, simpleCopy.getClass());
        MultiThreadReporterContext multiThreadCopy = new MultiThreadReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(MultiThreadReporterContext.class, multiThreadCopy.getClass());

        // Test copies' reporters (pop and test all of them)
        Assertions.assertEquals(reportNode3, simpleCopy.getReporter());
        Assertions.assertEquals(reportNode3, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(reportNode2, simpleCopy.getReporter());
        Assertions.assertEquals(reportNode2, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(reportNode1, simpleCopy.getReporter());
        Assertions.assertEquals(reportNode1, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(ReportNode.NO_OP, simpleCopy.getReporter());
        Assertions.assertEquals(ReportNode.NO_OP, multiThreadCopy.getReporter());

        // Clean
        multiThreadCopy.close();
    }

    static Stream<Arguments> getReporterContextStream() {
        return Stream.of(
                Arguments.of("Simple",
                        (Supplier<ReporterContext>) () -> simpleContext),
                Arguments.of("Multi-thread",
                        (Supplier<ReporterContext>) () -> multiThreadContext)
        );
    }

}
