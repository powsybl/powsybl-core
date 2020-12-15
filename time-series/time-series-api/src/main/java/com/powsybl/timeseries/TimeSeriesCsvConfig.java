/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.timeseries;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.powsybl.timeseries.TimeSeries.TimeFormat;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class TimeSeriesCsvConfig {
    private final DateTimeFormatter dateTimeFormatter;
    private final boolean versioned;
    private final TimeFormat timeFormat;
    private final char separator;

    public TimeSeriesCsvConfig() {
        this(ZoneId.systemDefault(), TimeSeriesConstants.DEFAULT_SEPARATOR, true, TimeFormat.DATE_TIME);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId) {
        this(zoneId, TimeSeriesConstants.DEFAULT_SEPARATOR, true, TimeFormat.DATE_TIME);
    }

    public TimeSeriesCsvConfig(char separator, boolean versioned, TimeFormat timeFormat) {
        this(ZoneId.systemDefault(), separator, versioned, timeFormat);
    }

    public TimeSeriesCsvConfig(ZoneId zoneId, char separator, boolean versioned, TimeFormat timeFormat) {
        this.dateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
        this.separator = separator;
        this.versioned = versioned;
        this.timeFormat = timeFormat;
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

}
