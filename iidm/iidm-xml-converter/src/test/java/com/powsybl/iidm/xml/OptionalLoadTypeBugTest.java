/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.IidmXmlVersion;
import org.junit.Test;

import static com.powsybl.iidm.xml.AbstractXmlConverterTest.getVersionDir;
import static com.powsybl.commons.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OptionalLoadTypeBugTest {

    @Test
    public void shouldNotThrowNullPointerExceptionTest() {
        assertNotNull(NetworkXml.read(getClass().getResourceAsStream(getVersionDir(IidmXmlVersion.V_1_0) + "optionalLoadTypeBug.xml")));
        assertNotNull(NetworkXml.read(getClass().getResourceAsStream(getVersionDir(CURRENT_IIDM_XML_VERSION) + "optionalLoadTypeBug.xml")));
    }
}
