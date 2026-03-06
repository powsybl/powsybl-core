/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ContextTest {

    @ParameterizedTest
    @MethodSource("recordDelimiterAndQuoteProvider")
    void detectDelimiterTest(String line, char delimiter, char quote) {
        Context context = new Context();

        context.detectDelimiter(line);
        assertEquals(delimiter, context.getDelimiter());
        assertEquals(quote, context.getQuote());
    }

    private static Stream<Arguments> recordDelimiterAndQuoteProvider() {
        return Stream.of(
            Arguments.of("a,b,c,d", ',', '\''),
            Arguments.of("a b c d", ' ', '\''),
            Arguments.of("a 'b c' d", ' ', '\''),
            Arguments.of("a \"b c\" d", ' ', '\"'),
            Arguments.of("a,'b c',d", ',', '\''),
            Arguments.of("a,\"b c\",d", ',', '\"'),
            Arguments.of("a,\"b c,d", ',', '\''),
            Arguments.of("a,\"b c d e f\",g", ',', '\"')
        );
    }
}
