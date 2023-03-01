/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class TerminalRefNotFoundTest extends AbstractXmlConverterTest {

    @Test
    void test() {
        PowsyblException exception = assertThrows(PowsyblException.class, () -> NetworkXml.read(getVersionedNetworkAsStream("terminalRefNotFound.xiidm", IidmXmlVersion.V_1_5)));
        assertEquals("Terminal reference identifiable not found: '????'", exception.getMessage());
    }
}
