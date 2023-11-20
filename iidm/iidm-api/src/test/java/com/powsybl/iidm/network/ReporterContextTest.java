/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.reporter.Reporter;
import com.powsybl.commons.reporter.ReporterModel;
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
class ReporterContextTest {

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
        assertEquals(Reporter.NO_OP, reporterContext.getReporter());
        assertEquals(Reporter.NO_OP, reporterContext.peekReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void pushAndGetReporterTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        ReporterModel reporter0 = new ReporterModel("task0", "name0");
        reporterContext.pushReporter(reporter0);
        assertEquals(reporter0, reporterContext.getReporter());
        assertEquals(reporter0, reporterContext.peekReporter());

        ReporterModel reporter1 = new ReporterModel("task1", "name1");
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

        ReporterModel reporter0 = new ReporterModel("task0", "name0");
        ReporterModel reporter1 = new ReporterModel("task1", "name1");
        reporterContext.pushReporter(reporter0);
        reporterContext.pushReporter(reporter1);
        assertEquals(reporter1, reporterContext.getReporter());
        // Pop
        Reporter poppedReporter = reporterContext.popReporter();
        assertEquals(reporter1, poppedReporter);
        assertEquals(reporter0, reporterContext.getReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void popOnEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();

        assert Reporter.NO_OP.equals(reporterContext.getReporter());
        Reporter poppedReporter = reporterContext.popReporter();
        assertEquals(Reporter.NO_OP, poppedReporter);
        assertEquals(Reporter.NO_OP, reporterContext.getReporter());

        // Empty context can be popped several times
        poppedReporter = reporterContext.popReporter();
        assertEquals(Reporter.NO_OP, poppedReporter);
        assertEquals(Reporter.NO_OP, reporterContext.getReporter());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void copyEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        assert Reporter.NO_OP.equals(reporterContext.getReporter());

        // Simple
        ReporterContext copy = new SimpleReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(SimpleReporterContext.class, copy.getClass());
        Assertions.assertEquals(Reporter.NO_OP, copy.getReporter());

        // Multi-thread
        copy = new MultiThreadReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(MultiThreadReporterContext.class, copy.getClass());
        Assertions.assertEquals(Reporter.NO_OP, copy.getReporter());

        // Clean
        ((MultiThreadReporterContext) copy).close();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getReporterContextStream")
    void copyNonEmptyReporterContextTest(String desc, Supplier<ReporterContext> contextSupplier) {
        ReporterContext reporterContext = contextSupplier.get();
        assert Reporter.NO_OP.equals(reporterContext.getReporter());

        // Push reporters in the context
        Reporter reporter1 = new ReporterModel("1", "1");
        reporterContext.pushReporter(reporter1);
        Reporter reporter2 = new ReporterModel("2", "2");
        reporterContext.pushReporter(reporter2);
        Reporter reporter3 = new ReporterModel("3", "3");
        reporterContext.pushReporter(reporter3);

        // Create copies
        SimpleReporterContext simpleCopy = new SimpleReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(SimpleReporterContext.class, simpleCopy.getClass());
        MultiThreadReporterContext multiThreadCopy = new MultiThreadReporterContext((AbstractReporterContext) reporterContext);
        Assertions.assertEquals(MultiThreadReporterContext.class, multiThreadCopy.getClass());

        // Test copies' reporters (pop and test all of them)
        Assertions.assertEquals(reporter3, simpleCopy.getReporter());
        Assertions.assertEquals(reporter3, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(reporter2, simpleCopy.getReporter());
        Assertions.assertEquals(reporter2, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(reporter1, simpleCopy.getReporter());
        Assertions.assertEquals(reporter1, multiThreadCopy.getReporter());

        simpleCopy.popReporter();
        multiThreadCopy.popReporter();
        Assertions.assertEquals(Reporter.NO_OP, simpleCopy.getReporter());
        Assertions.assertEquals(Reporter.NO_OP, multiThreadCopy.getReporter());

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
