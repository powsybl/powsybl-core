/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.google.re2j.Pattern;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public enum FileFormat {
    LEGACY_TEXT('\'', ','),
    JSON('"', ',');

    // The order of delimiters is relevant
    public static final String VALID_DELIMITERS = ", ";

    public static final Pattern LEGACY_TEXT_QUOTED_OR_WHITESPACE = Pattern.compile("('[^']*')|( )+");

    FileFormat(char quote, char defaultDelimiter) {
        this.quote = quote;
        this.defaultDelimiter = defaultDelimiter;
    }

    char getQuote() {
        return quote;
    }

    char getDefaultDelimiter() {
        return defaultDelimiter;
    }

    private final char quote;

    private final char defaultDelimiter;
}
