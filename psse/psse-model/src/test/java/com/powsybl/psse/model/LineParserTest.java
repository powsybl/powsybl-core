/**
 * Copyright (c) 2025, University of West Bohemia (https://www.zcu.cz)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model;

import com.powsybl.psse.model.io.LineParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Petr Janecek {@literal <pjanecek at ntis.zcu.cz>}
 */
public class LineParserTest {
    @Test
    void testParseSimpleLine() {
        assertParsedLine(
                "7,'Bus 7       ', 138.0000,1    1,   1,   1,1.06152, -13.3596,1.458e+8",
                "7", "Bus 7       ", "138.0000", "1", "1", "1", "1", "1.06152", "-13.3596", "1.458e+8"
        );
    }

    @Test
    void testParseComplexLine() {
        assertParsedLine(
                "    4,    7,    0,'1 ',1 1, 1 ,  0.00000,  0.00000,2,'trasd2  ',1,   1,1.0000,   0,1.0000,   0,1.0000,   0,1.0000",
                "4", "7", "0", "1 ", "1", "1", "1", "0.00000", "0.00000", "2", "trasd2  ", "1",
                "1", "1.0000", "0", "1.0000", "0", "1.0000", "0", "1.0000"
        );
    }

    @Test
    void testParseLineWithEmptyTokens() {
        assertParsedLine(
                ", ,, 4 3 ,,5,'ahoj , svete'",
                "", "", "", "4", "3", "", "5", "ahoj , svete"
        );
    }

    private void assertParsedLine(String line, String... expectedTokens) {
        final LineParser parser = new LineParser();

        final String[] actualTokens = parser.parseLine(line);
        assertEquals(expectedTokens.length, actualTokens.length, "Expected token count does not match");

        for (int i = 0; i < expectedTokens.length; i++) {
            assertEquals(expectedTokens[i], actualTokens[i], "Mismatch at token index " + i);
        }
    }
}
