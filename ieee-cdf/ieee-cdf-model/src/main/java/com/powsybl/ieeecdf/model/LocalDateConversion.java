/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.conversions.ObjectConversion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LocalDateConversion extends ObjectConversion<LocalDate> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("M/d/")
            .optionalStart()
            .appendPattern("uuuu")
            .optionalEnd()
            .optionalStart()
            .appendValueReduced(ChronoField.YEAR, 2, 2, 1970)
            .optionalEnd()
            .toFormatter();

    private static final String INVALID_DATE = "0 /0 /0 ";

    @Override
    protected LocalDate fromString(String str) {
        if (!str.equals(INVALID_DATE)) {
            return LocalDate.parse(str, FORMATTER);
        }
        return null;
    }

    @Override
    public String revert(LocalDate date) {
        return date != null ? FORMATTER.format(date) : INVALID_DATE;
    }
}
