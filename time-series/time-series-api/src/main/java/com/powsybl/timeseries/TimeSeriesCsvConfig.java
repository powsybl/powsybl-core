/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries;

import com.powsybl.timeseries.TimeSeries.TimeFormat;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class TimeSeriesCsvConfig {
    private static final int DEFAULT_MAX_COLUMNS = 20000;
    private static final boolean DEFAULT_SKIP_DUPLICATE = false;

    private final DateTimeFormatter dateTimeFormatter;
    private final boolean versioned;
    private final TimeFormat timeFormat;
    private final char separator;
    private final int maxColumns;
    private final boolean strictVersioningImport;
    private final boolean skipDuplicateTimeEntry;

    public TimeSeriesCsvConfig() {
        this(ZoneId.systemDefault(), TimeSeriesConstants.DEFAULT_SEPARATOR, true, TimeFormat.DATE_TIME, DEFAULT_MAX_COLUMNS, true, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId) {
        this(zoneId, TimeSeriesConstants.DEFAULT_SEPARATOR, true, TimeFormat.DATE_TIME, DEFAULT_MAX_COLUMNS, true, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(char separator, boolean versioned, TimeFormat timeFormat) {
        this(ZoneId.systemDefault(), separator, versioned, timeFormat, DEFAULT_MAX_COLUMNS, true, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(char separator, boolean versioned, TimeFormat timeFormat, boolean strictVersioningImport) {
        this(ZoneId.systemDefault(), separator, versioned, timeFormat, DEFAULT_MAX_COLUMNS, strictVersioningImport, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(char separator, boolean versioned, TimeFormat timeFormat, boolean strictVersioningImport,
                               boolean skipDuplicateTimeEntry) {
        this(ZoneId.systemDefault(), separator, versioned, timeFormat, DEFAULT_MAX_COLUMNS, strictVersioningImport, skipDuplicateTimeEntry);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId, char separator, boolean versioned, TimeFormat timeFormat) {
        this(zoneId, separator, versioned, timeFormat, DEFAULT_MAX_COLUMNS, true, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId, char separator, boolean versioned, TimeFormat timeFormat, boolean strictVersioningImport) {
        this(zoneId, separator, versioned, timeFormat, DEFAULT_MAX_COLUMNS, strictVersioningImport, DEFAULT_SKIP_DUPLICATE);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId, char separator, boolean versioned, TimeFormat timeFormat, int maxColumns,
                               boolean strictVersioningImport, boolean skipDuplicateTimeEntry) {
        this.dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
        this.separator = separator;
        this.versioned = versioned;
        this.timeFormat = timeFormat;
        this.maxColumns = maxColumns;
        this.strictVersioningImport = strictVersioningImport;
        this.skipDuplicateTimeEntry = skipDuplicateTimeEntry;
    }

    public char separator() {
        return separator;
    }

    public boolean versioned() {
        return versioned;
    }

    public TimeFormat timeFormat() {
        return timeFormat;
    }

    public DateTimeFormatter dateTimeFormatter() {
        return dateTimeFormatter;
    }

    public int getMaxColumns() {
        return maxColumns;
    }

    public boolean withStrictVersioningImport() {
        return strictVersioningImport;
    }

    public boolean isSkipDuplicateTimeEntry() {
        return skipDuplicateTimeEntry;
    }
}
