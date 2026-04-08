/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.serde.util.IidmSerDeUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class IidmSerDeUtilTest {

    private static final String ROOT = "root";
    private static final String ELEMENT = "element";

    @Test
    void testReadMaximumVersion() {
        NetworkDeserializerContext context = Mockito.mock(NetworkDeserializerContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);

        try {
            IidmSerDeUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        try {
            IidmSerDeUtil.assertMaximumVersion(ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }
    }

    @Test
    void testWriteAssertMaximumVersion() {
        NetworkSerializerContext context = Mockito.mock(NetworkSerializerContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmSerDeUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        try {
            IidmSerDeUtil.assertMaximumVersion(ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmSerDeUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
        IidmSerDeUtil.assertMaximumVersion(ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
    }

    @Test
    void testMinimumVersion() {
        NetworkDeserializerContext context = Mockito.mock(NetworkDeserializerContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_0);

        try {
            IidmSerDeUtil.assertMinimumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.0. IIDM version should be >= 1.1", e.getMessage());
        }

        try {
            IidmSerDeUtil.assertMinimumVersion(ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.0. IIDM version should be >= 1.1", e.getMessage());
        }
    }

    @Test
    void testReadStrictMaximumVersion() {
        NetworkDeserializerContext context = Mockito.mock(NetworkDeserializerContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);

        try {
            IidmSerDeUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be < 1.1", e.getMessage());
        }
    }

    @Test
    void testWriteStrictMaximumVersion() {
        NetworkSerializerContext context = Mockito.mock(NetworkSerializerContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmSerDeUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be < 1.1", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmSerDeUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerDeUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context); // check it doesn't fail when behavior is LOG_ERROR
    }
}
