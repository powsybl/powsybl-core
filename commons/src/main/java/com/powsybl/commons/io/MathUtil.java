/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.io;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class MathUtil {

    private MathUtil() {
    }

    public static Table<String, String, Float> parseMatrix(Reader reader) throws IOException {
        Table<String, String, Float> table = HashBasedTable.create();
        try (ICsvListReader csvReader = new CsvListReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
            List<String> columnHeaders = csvReader.read();
            List<String> row;
            while ((row = csvReader.read()) != null) {
                String rowHeader = row.get(0);
                for (int i = 1; i < row.size(); i++) {
                    String columnHeader = columnHeaders.get(i);
                    String value = row.get(i);
                    table.put(rowHeader, columnHeader, value == null ? Float.NaN : Float.parseFloat(value));
                }
            }
        }
        return table;
    }

}
