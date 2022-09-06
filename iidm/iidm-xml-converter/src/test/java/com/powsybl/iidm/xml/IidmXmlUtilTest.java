/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class IidmXmlUtilTest {

    private static final String ROOT = "root";
    private static final String ELEMENT = "element";

    @Test
    public void testReadMaximumVersion() {
        NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);

        try {
            IidmXmlUtil.assertMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }

        try {
            IidmXmlUtil.assertMaximumVersion(ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }
    }

    @Test
    public void testWriteAssertMaximumVersion() {
        NetworkXmlWriterContext context = Mockito.mock(NetworkXmlWriterContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmXmlUtil.assertMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }

        try {
            IidmXmlUtil.assertMaximumVersion(ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmXmlUtil.assertMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
        IidmXmlUtil.assertMaximumVersion(ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context); // check it doesn't fail when behavior is LOG_ERROR
    }

    @Test
    public void testMinimumVersion() {
        NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_0);

        try {
            IidmXmlUtil.assertMinimumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.0. IIDM-XML version should be >= 1.1", e.getMessage());
        }

        try {
            IidmXmlUtil.assertMinimumVersion(ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("element is not supported for IIDM-XML version 1.0. IIDM-XML version should be >= 1.1", e.getMessage());
        }
    }

    @Test
    public void testReadStrictMaximumVersion() {
        NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);

        try {
            IidmXmlUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be < 1.1", e.getMessage());
        }
    }

    @Test
    public void testWriteStrictMaximumVersion() {
        NetworkXmlWriterContext context = Mockito.mock(NetworkXmlWriterContext.class);
        Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);
        ExportOptions options = new ExportOptions();
        Mockito.when(context.getOptions()).thenReturn(options);

        try {
            IidmXmlUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be < 1.1", e.getMessage());
        }

        options.setIidmVersionIncompatibilityBehavior(ExportOptions.IidmVersionIncompatibilityBehavior.LOG_ERROR);
        IidmXmlUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context); // check it doesn't fail when behavior is LOG_ERROR
    }
}
