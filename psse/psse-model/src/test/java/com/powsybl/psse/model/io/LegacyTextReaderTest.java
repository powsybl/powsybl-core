/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class LegacyTextReaderTest {

    @Test
    void noBacktrackingOnProcessTextTest() {
        String whiteChars = StringUtils.repeat(" ", 100_000);
        String line = whiteChars + "a" + whiteChars + "b" + whiteChars;
        long start = System.currentTimeMillis();
        String result = LegacyTextReader.processText(line);
        assertEquals("a,b", result);
        long end = System.currentTimeMillis();
        if (end - start > 500) {
            fail("Timeout");
        }
    }

}
