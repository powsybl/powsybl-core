/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class CsvReducerNamingStrategy implements ReducerNamingStrategy {
    private Map<String, String> idMapping = new HashMap<>();

    public CsvReducerNamingStrategy(Reader reader) throws IOException {
        try (ICsvListReader csvReader = new CsvListReader(reader, CsvPreference.STANDARD_PREFERENCE)) {
            List<String> row;
            while ((row = csvReader.read()) != null) {
                if (row.size() != 2) {
                    throw new PowsyblException("Invalid row length in reducer naming strategy CSV mapping file");
                }
                idMapping.put(row.get(0), row.get(1));
            }
        }
    }

    @Override
    public String getReplacementId(Branch<?> branch) {
        String branchId = branch.getId();
        return idMapping.getOrDefault(branchId, branchId);
    }
}
