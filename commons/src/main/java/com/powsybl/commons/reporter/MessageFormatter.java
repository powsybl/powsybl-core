/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.reporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public final class MessageFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFormatter.class);

    static final char DELIM_START = '{';
    static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';
    private static final Object UNKNOWN_KEY = "[unknown key value]";

    private MessageFormatter() {
    }

    public static String format(String msgPattern, Map<String, Object> values, Map<String, Object> taskValues) {
        Objects.requireNonNull(msgPattern);
        Objects.requireNonNull(values);

        StringBuilder sbuf = new StringBuilder(msgPattern.length() + 20);

        int i = 0;
        while (true) {
            int varStart = msgPattern.indexOf(DELIM_START, i);
            int varStop = msgPattern.indexOf(DELIM_STOP, varStart + 2);

            if (varStart == -1 || varStop == -1) { // no more variables
                break;
            } else {
                if (isEscapedDelimiter(msgPattern, varStart)) { // not supporting double escaped delimiter start
                    sbuf.append(msgPattern, i, varStart - 1).append(DELIM_START);
                    i = varStart + 1;
                } else {
                    sbuf.append(msgPattern, i, varStart);
                    String varKey = msgPattern.substring(varStart + 1, varStop); // not supporting (escaped) delimiter in variable key
                    safeObjectAppend(sbuf, varKey, values, taskValues);
                    i = varStop + 1;
                }
            }
        }

        // append the characters following the last {} pair.
        return sbuf.append(msgPattern, i, msgPattern.length()).toString();
    }

    private static void safeObjectAppend(StringBuilder sbuf, String varKey, Map<String, Object> values, Map<String, Object> taskValues) {
        Object o = values.getOrDefault(varKey, taskValues.getOrDefault(varKey, UNKNOWN_KEY));
        if (o == UNKNOWN_KEY) {
            LOGGER.debug("Failed to format message pattern, unknown key value {}", varKey);
        }
        try {
            String oAsString = o.toString();
            sbuf.append(oAsString);
        } catch (Exception e) {
            LOGGER.debug("Failed toString() invocation on an object of type [{}]", o.getClass().getName(), e);
            sbuf.append("[FAILED toString()]");
        }
    }

    static boolean isEscapedDelimiter(String messagePattern, int delimiterStartIndex) {
        if (delimiterStartIndex == 0) {
            return false;
        }
        char potentialEscape = messagePattern.charAt(delimiterStartIndex - 1);
        return potentialEscape == ESCAPE_CHAR;
    }

}
