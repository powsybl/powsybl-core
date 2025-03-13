/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class ReportFormatterTest {

    @Test
    void test() {
        ReportNode root = ReportNode.newRootReportNode()
                .withMessageTemplateProvider(PowsyblCoreReportResourceBundles.TEST_MESSAGE_TEMPLATE_PROVIDER)
                .withMessageTemplate("formatTest")
                .withUntypedValue("doubleDefaultFormat", 4.35684975)
                .withTypedValue("doubleSpecificFormat", 4.4664798548, TypedValue.ACTIVE_POWER)
                .withTypedValue("floatSpecificFormat", 0.6f, TypedValue.IMPEDANCE)
                .withUntypedValue("stringDefaultFormat", "tiny")
                .withTypedValue("stringSpecificFormat", "This is a sentence which needs to be truncated", "LONG_SENTENCE")
                .build();
        ReportFormatter customFormatter = typedValue -> {
            if (typedValue.getType().equals(TypedValue.ACTIVE_POWER) && typedValue.getValue() instanceof Double d) {
                return String.format(Locale.CANADA_FRENCH, "%2.4f", d);
            }
            if (typedValue.getType().equals(TypedValue.IMPEDANCE) && typedValue.getValue() instanceof Float f) {
                return String.format(Locale.CANADA_FRENCH, "%.2f", f);
            }
            if (typedValue.getType().equals("LONG_SENTENCE") && typedValue.getValue() instanceof String s) {
                return s.substring(0, 18);
            }
            return typedValue.getValue().toString();
        };
        assertEquals("Formatter test message double default format: 4.35684975 double format based on type: 4,4665 float format based on type: 0,60 string default format: tiny string format based on type: This is a sentence", root.getMessage(customFormatter));
    }
}
