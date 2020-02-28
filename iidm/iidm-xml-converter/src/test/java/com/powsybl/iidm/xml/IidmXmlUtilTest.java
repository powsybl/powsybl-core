/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.xml.util.IidmXmlUtil;
import org.junit.Test;
import org.mockito.Mockito;

import javax.xml.stream.XMLStreamReader;

import static org.junit.Assert.*;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class IidmXmlUtilTest {

    @Test
    public void testMaximumVersion() {
        try {
            NetworkXmlReaderContext context = Mockito.mock(NetworkXmlReaderContext.class);
            Mockito.when(context.getVersion()).thenReturn(IidmXmlVersion.V_1_1);

            IidmXmlUtil.assertMaximumVersion("root", "element", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_0, context);
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

            IidmXmlUtil.assertMinimumVersion("root", "element", IidmXmlUtil.ErrorMessage.NOT_SUPPORTED, IidmXmlVersion.V_1_1, context);
            fail();
        } catch (PowsyblException e) {
            assertEquals("root.element is not supported for IIDM-XML version 1.0. IIDM-XML version should be >= 1.1", e.getMessage());
        }
    }

    @Test
    public void testReadOptionalDoubleAttributeUntilMaximumVersion() {
        try {
            XMLStreamReader reader = Mockito.mock(XMLStreamReader.class);
            Mockito.when(reader.getAttributeValue(Mockito.any(), Mockito.any())).thenReturn("0.0");

            NetworkXmlReaderContext context = new NetworkXmlReaderContext(Mockito.mock(Anonymizer.class), reader);
            IidmXmlUtil.readOptionalDoubleAttributeUntilMaximumVersion("root", "attribute", IidmXmlVersion.V_1_0, context);

            fail();
        } catch (PowsyblException e) {
            assertEquals("root.attribute is not supported for IIDM-XML version 1.1. IIDM-XML version should be <= 1.0", e.getMessage());
        }
    }
}
