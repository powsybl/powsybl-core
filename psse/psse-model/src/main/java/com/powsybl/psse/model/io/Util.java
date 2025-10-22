/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.io;

import com.powsybl.psse.model.PsseException;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public final class Util {

    private Util() {
    }

    public static String[] retainAll(String[] strings, String[] stringsToKeep) {
        if (strings == null || stringsToKeep == null) {
            return new String[0];
        }
        Set<String> setStringsToKeep = new HashSet<>(Arrays.asList(stringsToKeep));
        List<String> kept = new ArrayList<>();
        for (String s : strings) {
            if (setStringsToKeep.contains(s)) {
                kept.add(s);
            }
        }
        return kept.toArray(new String[0]);
    }

    public static String defaultIfEmpty(String value, String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    public static Integer parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null")) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    public static Integer parseIntOrNull(String value, String... nullValues) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null") || Arrays.asList(nullValues).contains(value)) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public static Double parseDoubleOrDefault(String value, Double defaultValue) {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("null")) {
            return defaultValue;
        }
        return Double.parseDouble(value);
    }

    public static String getFieldFromMultiplePotentialHeaders(NamedCsvRecord rec, String... headers) {
        return rec.getField(Arrays.stream(headers).filter(header -> rec.findField(header).isPresent()).findFirst().orElseThrow(NoSuchElementException::new));
    }

    public static Double parseDoubleFromRecord(CsvRecord rec, Double defaultValue, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value != null) {
            return Double.parseDouble(value);
        } else {
            return defaultValue;
        }
    }

    public static Double parseDoubleFromRecord(CsvRecord rec, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value == null) {
            throwIfHeaderNotFound(rec, potentialHeaders);
        }
        return Double.parseDouble(value);
    }

    public static Float parseFloatFromRecord(CsvRecord rec, Float defaultValue, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value != null) {
            return Float.parseFloat(value);
        } else {
            return defaultValue;
        }
    }

    public static Float parseFloatFromRecord(CsvRecord rec, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value == null) {
            throwIfHeaderNotFound(rec, potentialHeaders);
        }
        return Float.parseFloat(value);
    }

    public static Integer parseIntFromRecord(CsvRecord rec, Integer defaultValue, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return defaultValue;
        }
    }

    public static Integer parseIntFromRecord(CsvRecord rec, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = manageNumericalNullValues(parseValueFromRecord(rec, headers, potentialHeaders));

        // Manage null values
        if (value == null) {
            throwIfHeaderNotFound(rec, potentialHeaders);
        }
        return Integer.parseInt(value);
    }

    public static String parseStringFromRecord(CsvRecord rec, String defaultValue, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = parseValueFromRecord(rec, headers, potentialHeaders);

        // Manage null values
        if (value != null && !value.isEmpty()) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public static String parseStringFromRecord(CsvRecord rec, String[] headers, String... potentialHeaders) {
        // Parse the value from the record
        String value = parseValueFromRecord(rec, headers, potentialHeaders);

        // Manage null values
        if (value == null) {
            throwIfHeaderNotFound(rec, potentialHeaders);
        }
        return value;
    }

    private static String parseValueFromRecord(CsvRecord rec, String[] headers, String... potentialHeaders) {
        String header = Arrays.stream(potentialHeaders)
            .filter(h -> ArrayUtils.contains(headers, h))
            .findFirst()
            .orElse(null);

        // If the header is not found, return null
        if (header == null) {
            return null;
        }

        // Parse the value from the record
        int index = ArrayUtils.indexOf(headers, header);
        return index < rec.getFieldCount() ? rec.getField(ArrayUtils.indexOf(headers, header)) : null;
    }

    private static String manageNumericalNullValues(String value) {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("null") ? null : value;
    }

    private static void throwIfHeaderNotFound(CsvRecord rec, String... potentialHeaders) {
        throw new PsseException(String.format("Column [%s] not found in record: %s", String.join(",", potentialHeaders), rec.toString()));
    }
}
