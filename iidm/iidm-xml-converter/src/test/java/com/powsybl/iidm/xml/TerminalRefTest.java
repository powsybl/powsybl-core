/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class TerminalRefTest extends AbstractXmlConverterTest {

    @Test
    void roundTripTest() throws IOException {
        roundTripAllVersionedXmlTest("terminalRef.xiidm");
        roundTripAllVersionedXmlTest("regulatingTerminal.xml");
    }
}
