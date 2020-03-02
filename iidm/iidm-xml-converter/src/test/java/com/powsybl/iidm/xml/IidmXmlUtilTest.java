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
    public void testMaximumVersion() {
        try {
            NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
            Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);

            IidmXmlUtil.assertMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }
    }

    @Test
    public void testMinimumVersion() {
        try {
            NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
            Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_0);

            IidmXmlUtil.assertMinimumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.0. IIDM-XML version should be >= 1.1", e.getMessage());
        }
    }

    @Test
    public void testStrictMaximumVersion() {
        try {
            NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
            Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);

            IidmXmlUtil.assertStrictMaximumVersion(ROOT, ELEMENT, IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.1. IIDM-XML version should be < 1.1", e.getMessage());
        }
    }
}
