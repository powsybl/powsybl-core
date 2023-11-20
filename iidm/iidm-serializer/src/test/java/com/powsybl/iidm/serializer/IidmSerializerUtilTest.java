/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serializer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.serializer.util.IidmSerializerUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
class IidmSerializerUtilTest {

    private static final String ROOT = "root";
    private static final String ELEMENT = "element";

    @Test
    void testReadMaximumVersion() {
        NetworkSerializerReaderContext context = Mockito.mock(NetworkSerializerReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);

        try {
            IidmSerializerUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        try {
            IidmSerializerUtil.assertMaximumVersion(ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }
    }

    @Test
    void testWriteAssertMaximumVersion() {
        NetworkSerializerWriterContext context = Mockito.mock(NetworkSerializerWriterContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmSerializerUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        try {
            IidmSerializerUtil.assertMaximumVersion(ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.1. IIDM version should be <= 1.0", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmSerializerUtil.assertMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
        IidmSerializerUtil.assertMaximumVersion(ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
    }

    @Test
    void testMinimumVersion() {
        NetworkSerializerReaderContext context = Mockito.mock(NetworkSerializerReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_0);

        try {
            IidmSerializerUtil.assertMinimumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.0. IIDM version should be >= 1.1", e.getMessage());
        }

        try {
            IidmSerializerUtil.assertMinimumVersion(ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM version 1.0. IIDM version should be >= 1.1", e.getMessage());
        }
    }

    @Test
    void testReadStrictMaximumVersion() {
        NetworkSerializerReaderContext context = Mockito.mock(NetworkSerializerReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);

        try {
            IidmSerializerUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be < 1.1", e.getMessage());
        }
    }

    @Test
    void testWriteStrictMaximumVersion() {
        NetworkSerializerWriterContext context = Mockito.mock(NetworkSerializerWriterContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmSerializerUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM version 1.1. IIDM version should be < 1.1", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmSerializerUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmSerializerUtil.ErrorMessage.NOT_SUPPORTED, IidmVersion.V_1_1, context); // check it doesn't fail when behavior is LOG_ERROR
    }
}
