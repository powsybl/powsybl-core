/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;

import java.util.regex.Pattern;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public enum FileFormat {
    LEGACY_TEXT,
    JSON;

    // The order of delimiters is relevant
    public static final String VALID_DELIMITERS = ", ";

    public static char getQuote(FileFormat fileFormat) {
        switch (fileFormat) {
            case LEGACY_TEXT:
                return '\'';
            case JSON:
                return '"';
            default:
                throw new PsseException("Unexpected fileFormat " + fileFormat);
        }
    }

    public static char getDefaultDelimiter(FileFormat fileFormat) {
        switch (fileFormat) {
            case LEGACY_TEXT:
            case JSON:
                return ',';
            default:
                throw new PsseException("Unexpected fileFormat " + fileFormat);
        }
    }

    public static final Pattern LEGACY_TEXT_QUOTED_OR_WHITESPACE = Pattern.compile("('[^']*')|( )+");
    public static final Pattern LEGACY_TEXT_UNQUOTED_OR_QUOTED = Pattern.compile("([^']+)|('([^']*)')");
}
