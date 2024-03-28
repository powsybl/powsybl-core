/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class EntsoeFileName {

    private static final Pattern DATE_REGEX = Pattern.compile("(\\d{4})[- /._]?(\\d{2})[- /._]?(\\d{2})[- /._]?(\\d{2})[- /._]?(\\d{2}).*");

    private final ZonedDateTime date;

    private final int forecastDistance;

    private final EntsoeGeographicalCode geographicalCode;

    public static EntsoeFileName parse(String str) {
        ZonedDateTime date = ZonedDateTime.now();
        int forecastDistance = 0;
        EntsoeGeographicalCode geographicalCode = null;
        Matcher m = DATE_REGEX.matcher(str);
        if (m.matches()) {
            // time zone is Europe/Paris
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int dayOfMonth = Integer.parseInt(m.group(3));
            int hourOfDay = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            date = ZonedDateTime.of(year, month, dayOfMonth, hourOfDay, minute, 0, 0, ZoneId.of("Europe/Paris"));

            // extract horizon and forecast distance
            if (str.contains("FO")) {
                forecastDistance = 60 * (6 + hourOfDay) + minute; // DACF generated at 18:00 one day ahead7
            } else if (str.contains("SN")) {
                forecastDistance = 0;
            }
        }
        if (str.length() >= 20) {
            try {
                geographicalCode = EntsoeGeographicalCode.valueOf(str.substring(18, 20));
            } catch (IllegalArgumentException x) {
                geographicalCode = null;
            }
        }

        return new EntsoeFileName(date, forecastDistance, geographicalCode);
    }

    protected EntsoeFileName(ZonedDateTime date, int forecastDistance, EntsoeGeographicalCode geographicalCode) {
        this.date = date;
        this.forecastDistance = forecastDistance;
        this.geographicalCode = geographicalCode;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public int getForecastDistance() {
        return forecastDistance;
    }

    public EntsoeGeographicalCode getGeographicalCode() {
        return geographicalCode;
    }

    public String getCountry() {
        return geographicalCode != null && geographicalCode.getCountry() != null ? geographicalCode.getCountry().toString() : null;
    }
}
