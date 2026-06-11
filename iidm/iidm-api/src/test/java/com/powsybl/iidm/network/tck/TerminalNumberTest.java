/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.TerminalNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
class TerminalNumberTest {

    @Test
    void terminalNumberValueOfTest() {
        assertEquals(TerminalNumber.ONE, TerminalNumber.valueOf(1));
        assertEquals(TerminalNumber.TWO, TerminalNumber.valueOf(2));
        assertThrows(PowsyblException.class, () -> TerminalNumber.valueOf(3));
    }

    @Test
    void terminalNumberGetNumTest() {
        assertEquals(1, TerminalNumber.ONE.getNum());
        assertEquals(2, TerminalNumber.TWO.getNum());
    }
}
