/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.commons.test.PowsyblCoreTestReportResourceBundle;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class NoOpTest extends AbstractSerDeTest {

    @Test
    void test() throws IOException {
        ReportNode root = ReportNode.NO_OP;
        ReportNode reportNode = root.newReportNode()
                .withMessageTemplate("key")
                .withTypedValue("double", 2.0, TypedValue.ACTIVE_POWER)
                .withTypedValue("float", 2.0f, TypedValue.ACTIVE_POWER)
                .withTypedValue("int", 4, "counter")
                .withTypedValue("long", 4L, "counter")
                .withTypedValue("boolean", true, "condition")
                .withTypedValue("string", "vl1", TypedValue.VOLTAGE_LEVEL)
                .withUntypedValue("untyped_double", 2.0)
                .withUntypedValue("untyped_float", 2.0f)
                .withUntypedValue("untyped_int", 4)
                .withUntypedValue("untyped_long", 4L)
                .withUntypedValue("untyped_boolean", true)
                .withUntypedValue("untyped_string", "vl1")
                .withSeverity(TypedValue.TRACE_SEVERITY)
                .withSeverity("Custom severity")
                .add();
        assertEquals(Collections.emptyList(), root.getChildren());
        assertNotEquals(ReportNode.NO_OP, reportNode);
        assertEquals(Collections.emptyList(), reportNode.getChildren());
        assertEquals(Collections.emptyMap(), reportNode.getValues());
        assertEquals(Optional.empty(), reportNode.getValue("int"));
        assertNull(reportNode.getMessageTemplate());
        assertNull(reportNode.getMessageKey());

        ReportNode reportNodeImplRoot = ReportNode.newRootReportNode(PowsyblCoreTestReportResourceBundle.TEST_BASE_NAME, PowsyblCoreReportResourceBundle.BASE_NAME)
                .withMessageTemplate("k")
                .build();
        reportNode.include(reportNodeImplRoot);
        assertEquals(Collections.emptyList(), reportNode.getChildren());

        root.addCopy(reportNodeImplRoot);
        assertEquals(Collections.emptyList(), root.getChildren());

        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals("", sw.toString());

        Path serializedReport = tmpDir.resolve("tmp.json");
        ReportNodeSerializer.write(root, serializedReport);
        ComparisonUtils.assertTxtEquals(getClass().getResourceAsStream("/testReportNodeNoOp.json"), Files.newInputStream(serializedReport));
    }

    @Test
    void testTreeContextNoOp() {
        assertEquals(0, TreeContextNoOp.NO_OP.getDictionary().size());
        assertNull(TreeContextNoOp.NO_OP.getDefaultTimestampFormatter());
        assertNotNull(TreeContextNoOp.NO_OP.getLocale());

        TreeContextImpl treeContext = new TreeContextImpl();
        treeContext.addDictionaryEntry("key", "value");
        PowsyblException e = assertThrows(PowsyblException.class, () -> TreeContextNoOp.NO_OP.merge(treeContext));
        assertEquals("Cannot merge a TreeContextNoOp with non TreeContextNoOp", e.getMessage());

        assertEquals(Locale.US, treeContext.getLocale());
    }

    @Test
    void testTreeContextMerge() {
        TreeContextImpl treeContext = new TreeContextImpl();

        assertEquals(0, treeContext.getDictionary().size());
        assertEquals(ReportConstants.DEFAULT_LOCALE, treeContext.getLocale());

        TreeContextImpl treeContext2 = new TreeContextImpl();
        treeContext2.addDictionaryEntry("key", "value");
        treeContext.merge(treeContext2);
        assertEquals(1, treeContext.getDictionary().size());
    }

    @Test
    void testPostponedValuesAdded() throws IOException {
        ReportNode root = ReportNode.NO_OP;
        ReportNode childNode = root.newReportNode()
                .withMessageTemplate("key")
                .withTimestamp()
                .withTimestamp("pattern")
                .add();
        childNode.addTypedValue("double", 2.0, TypedValue.ACTIVE_POWER)
                .addTypedValue("float", 2.0f, TypedValue.ACTIVE_POWER)
                .addTypedValue("int", 4, "counter")
                .addTypedValue("long", 4L, "counter")
                .addTypedValue("boolean", true, "condition")
                .addTypedValue("string", "vl1", TypedValue.VOLTAGE_LEVEL)
                .addUntypedValue("untyped_double", 2.0)
                .addUntypedValue("untyped_float", 2.0f)
                .addUntypedValue("untyped_int", 4)
                .addUntypedValue("untyped_long", 4L)
                .addUntypedValue("untyped_boolean", true)
                .addUntypedValue("untyped_string", "vl1")
                .addSeverity(TypedValue.TRACE_SEVERITY)
                .addSeverity("Custom severity");

        assertEquals(Collections.emptyMap(), childNode.getValues());
        assertEquals(Optional.empty(), childNode.getValue("int"));
        assertNull(childNode.getMessage());

        StringWriter sw = new StringWriter();
        childNode.print(sw);
        assertEquals("", sw.toString());
    }

}
