/**
 * Copyright (c) 2025, University of West Bohemia (https://www.zcu.cz)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Petr Janecek {@literal <pjanecek at ntis.zcu.cz>}
 */
public class LineParser {
    public LineParser() {
    }

    public String[] parseLine(String line) {
        if (line == null || line.trim().isEmpty())
            return new String[0];

        CharacterIterator it = new StringCharacterIterator(line.trim());
        List<String> tokens = new ArrayList<>();

        if (it.current() == ',')
            tokens.add(""); // Leading comma = first token is empty

        while (!isIterationEnd(it)) {
            if (isSeparator(it.current()))
                parseSeparator(it, tokens);
            else if (isString(it.current()))
                parseString(it, tokens);
            else
                parseToken(it, tokens);
        }

        return tokens.toArray(new String[0]);
    }

    private boolean isSeparator(char c) {
        return c == ',' || c == ' ' || c == '\t';
    }

    private boolean isIterationEnd(CharacterIterator it) {
        return it.current() == CharacterIterator.DONE;
    }

    private boolean isString(char c) {
        return c == '\'';
    }

    private void parseSeparator(CharacterIterator it, List<String> tokens) {
        boolean wasComma = false;
        while (!isIterationEnd(it) && isSeparator(it.current())) {
            if (it.current() == ',') {
                if (wasComma) {
                    tokens.add("");
                }
                wasComma = true;
            }
            it.next();
        }
    }

    private void parseString(CharacterIterator it, List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        it.next(); //skip '
        while (!isIterationEnd(it) && !isString(it.current())) {
            sb.append(it.current());
            it.next();
        }
        it.next(); //skip '
        tokens.add(sb.toString());
    }

    private void parseToken(CharacterIterator it, List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        while (!isIterationEnd(it) && !isSeparator(it.current())) {
            sb.append(it.current());
            it.next();
        }
        tokens.add(sb.toString());
    }
}
