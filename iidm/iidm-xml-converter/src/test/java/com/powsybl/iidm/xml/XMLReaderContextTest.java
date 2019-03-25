/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.IidmImportExportMode;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class XMLReaderContextTest extends AbstractConverterTest {

    @Test(expected = NullPointerException.class)
    public void xmlReaderContextExceptionTest() {
        NetworkXmlReaderContext reader = new NetworkXmlReaderContext(null, null);
        assertEquals(IidmImportExportMode.UNIQUE_FILE, reader.getOptions().getMode());
    }
}
