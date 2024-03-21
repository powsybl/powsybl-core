/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.report;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm@aia.es>}
 */
class TypedValueTest {

    @Test
    void testSeverity() {
        // Check that all predefined Severity constraints have the proper type
        assertEquals(TypedValue.SEVERITY, TypedValue.TRACE_SEVERITY.getType());
        assertEquals(TypedValue.SEVERITY, TypedValue.DEBUG_SEVERITY.getType());
        assertEquals(TypedValue.SEVERITY, TypedValue.INFO_SEVERITY.getType());
        assertEquals(TypedValue.SEVERITY, TypedValue.WARN_SEVERITY.getType());
        assertEquals(TypedValue.SEVERITY, TypedValue.ERROR_SEVERITY.getType());

        // Check that is not possible to add a Severity attribute with a TypedValue that is not a severity
        ReportNode root = ReportNode.newRootReportNode().withMessageTemplate("root", "Root reportNode").build();
        ReportNodeAdder r1 = root.newReportNode().withMessageTemplate("key", "defaultMessage");
        TypedValue illegalSeverity = new TypedValue("error", "OTHER_TYPE");
        assertThrows(IllegalArgumentException.class, () -> r1.withSeverity(illegalSeverity));

        ReportNode rn1 = r1.withSeverity(TypedValue.DEBUG_SEVERITY).add();
        assertEquals(Optional.of(TypedValue.DEBUG_SEVERITY), rn1.getValue(ReportConstants.REPORT_SEVERITY_KEY));

        String customSeverity = "VeryImportant";
        ReportNode rn2 = root.newReportNode().withMessageTemplate("key", "defaultMessage").withSeverity(customSeverity).add();
        Optional<TypedValue> value = rn2.getValue(ReportConstants.REPORT_SEVERITY_KEY);
        assertTrue(value.isPresent());
        assertEquals(customSeverity, value.get().getValue());
    }

}
