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
    public String[] parseLine(String line) {
        if (line == null || line.trim().isEmpty())
            return new String[0];

        CharacterIterator it = new StringCharacterIterator(line.trim());
        List<String> tokens = new ArrayList<>();

        if (it.current() == COMMA)
            tokens.add(""); // Leading comma = first token is empty

        while (hasMoreCharacters(it)) {
            switch (getTokenType(it.current())) {
                case SEPARATOR -> parseSeparator(it, tokens);
                case STRING -> parseQuotedString(it, tokens);
                case COMMENT -> parseComment(it);
                default -> parseToken(it, tokens);
            }
        }

        return tokens.toArray(new String[0]);
    }

    private boolean hasMoreCharacters(CharacterIterator it) {
        return it.current() != CharacterIterator.DONE;
    }

    private TokenType getTokenType(char c) {
        if (isSeparator(c)) return TokenType.SEPARATOR;
        if (isString(c)) return TokenType.STRING;
        if (isComment(c)) return TokenType.COMMENT;
        return TokenType.TOKEN;
    }

    private boolean isSeparator(char c) {
        return c == COMMA || c == SPACE || c == TAB;
    }


    private boolean isString(char c) {
        return c == SINGLE_QUOTE || c == DOUBLE_QUOTE;
    }


    private boolean isComment(char c) {
        return c == COMMENT_SLASH;
    }

    private void parseSeparator(CharacterIterator it, List<String> tokens) {
        boolean wasComma = false;
        while (hasMoreCharacters(it) && isSeparator(it.current())) {
            if (it.current() == COMMA) {
                if (wasComma) {
                    tokens.add("");
                }
                wasComma = true;
            }
            it.next();
        }
    }

    private void parseQuotedString(CharacterIterator it, List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        it.next(); //skip quote
        while (hasMoreCharacters(it) && !isString(it.current())) {
            sb.append(it.current());
            it.next();
        }
        it.next(); //skip quote
        tokens.add(sb.toString());
    }

    private void parseToken(CharacterIterator it, List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        while (hasMoreCharacters(it) && !isSeparator(it.current())) {
            sb.append(it.current());
            it.next();
        }
        tokens.add(sb.toString());
    }

    private void parseComment(CharacterIterator it) {
        while (hasMoreCharacters(it)) {
            it.next();
        }
    }

    private static final char COMMA = ',';
    private static final char SINGLE_QUOTE = '\'';
    private static final char DOUBLE_QUOTE = '"';
    private static final char COMMENT_SLASH = '/';
    private static final char TAB = '\t';
    private static final char SPACE = ' ';

    private enum TokenType {
        SEPARATOR, STRING, COMMENT, TOKEN
    }
}
