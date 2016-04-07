/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.ucte.util;

import eu.itesla_project.iidm.network.Horizon;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteFileName {

    private static final Pattern DATE_REGEX = Pattern.compile(".*(\\d{4})[- /._]?(\\d{2})[- /._]?(\\d{2})[- /._]?(\\d{2})[- /._]?(\\d{2}).*");

    private final DateTime date;

    private final Horizon horizon;

    private final int forecastDistance;

    private final UcteGeographicalCode geographicalCode;

    public static UcteFileName parse(String str) {
        DateTime date = DateTime.now();
        Horizon horizon = Horizon.OTHER;
        int forecastDistance = 0;
        UcteGeographicalCode geographicalCode = null;
        Matcher m = DATE_REGEX.matcher(str);
        if (m.matches()) {
            // time zone is Europe/Paris
            int year = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int dayOfMonth = Integer.parseInt(m.group(3));
            int hourOfDay = Integer.parseInt(m.group(4));
            int minute = Integer.parseInt(m.group(5));
            date = new DateTime(year, month, dayOfMonth, hourOfDay, minute, DateTimeZone.forID("Europe/Paris"));

            // extract horizon and forecast distance
            if (str.contains("FO")) {
                horizon = Horizon.DACF;
                forecastDistance = 60 * (6 + hourOfDay) + minute; // DACF generated at 18:00 one day ahead7
            } else if (str.contains("SN")) {
                horizon = Horizon.SN;
                forecastDistance = 0;
            }
        }
        if (str.length() >= 19) {
            geographicalCode = UcteGeographicalCode.valueOf(str.substring(18, 20));
        }

        return new UcteFileName(date, horizon, forecastDistance, geographicalCode);
    }

    private UcteFileName(DateTime date, Horizon horizon, int forecastDistance, UcteGeographicalCode geographicalCode) {
        this.date = date;
        this.horizon = horizon;
        this.forecastDistance = forecastDistance;
        this.geographicalCode = geographicalCode;
    }

    public DateTime getDate() {
        return date;
    }

    public Horizon getHorizon() {
        return horizon;
    }

    public int getForecastDistance() {
        return forecastDistance;
    }

    public UcteGeographicalCode getGeographicalCode() {
        return geographicalCode;
    }

    public String getCountry() {
        return geographicalCode != null ? geographicalCode.getCountry().toString() : null;
    }

}
