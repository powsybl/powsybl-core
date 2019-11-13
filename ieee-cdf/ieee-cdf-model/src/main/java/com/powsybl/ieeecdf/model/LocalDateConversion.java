/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.model;

import com.univocity.parsers.conversions.ObjectConversion;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalDateConversion extends ObjectConversion<LocalDate> {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('/')
            .appendValue(YEAR, 2)
            .toFormatter();

    private static final String INVALID_DATE = "0 /0 /0 ";

    @Override
    protected LocalDate fromString(String input) {
        if (!input.equals(INVALID_DATE)) {
            return LocalDate.parse(input, FORMATTER);
        }
        return null;
    }

    @Override
    public String revert(LocalDate input) {
        return input != null ? FORMATTER.format(input) : INVALID_DATE;
    }
}
