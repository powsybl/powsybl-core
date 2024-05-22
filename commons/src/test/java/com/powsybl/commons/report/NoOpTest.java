/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class NoOpTest {

    @Test
    void test() throws IOException {
        ReportNode root = ReportNode.NO_OP;
        ReportNode reportNode = root.newReportNode()
                .withMessageTemplate("key", "message with value = ${double}")
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
                .add();
        assertEquals(Collections.emptyList(), root.getChildren());
        assertNotEquals(ReportNode.NO_OP, reportNode);
        assertEquals(Collections.emptyList(), reportNode.getChildren());
        assertEquals(Collections.emptyMap(), reportNode.getValues());
        assertEquals(Optional.empty(), reportNode.getValue("int"));
        assertNull(reportNode.getMessageTemplate());
        assertNull(reportNode.getMessageKey());

        reportNode.include(new ReportNodeRootBuilderImpl().withMessageTemplate("k", "Real root reportNode").build());
        assertEquals(Collections.emptyList(), reportNode.getChildren());

        StringWriter sw = new StringWriter();
        reportNode.print(sw);
        assertEquals("", sw.toString());
    }

}
