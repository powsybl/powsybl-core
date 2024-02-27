/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        ReportNode root = new ReportNodeImpl("root", "Root reporterNode");
        ReportNodeAdder r = root.newReportNode().withKey("key").withMessageTemplate("defaultMessage");
        TypedValue illegalSeverity = new TypedValue("error", "OTHER_TYPE");
        assertThrows(IllegalArgumentException.class, () -> r.withSeverity(illegalSeverity));
    }

}
