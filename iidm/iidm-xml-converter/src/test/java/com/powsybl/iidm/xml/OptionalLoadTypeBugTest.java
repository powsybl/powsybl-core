/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import org.junit.Test;

import static com.powsybl.iidm.xml.IidmXmlTestConstants.*;
import static org.junit.Assert.assertNotNull;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OptionalLoadTypeBugTest {

    @Test
    public void shouldNotThrowNullPointerExceptionTest() {
        assertNotNull(NetworkXml.read(getClass().getResourceAsStream(IIDM_VERSION_1_0_DIR_NAME + "optionalLoadTypeBug.xml")));
        assertNotNull(NetworkXml.read(getClass().getResourceAsStream(IIDM_CURRENT_VERSION_DIR_NAME + "optionalLoadTypeBug.xml")));
    }
}
