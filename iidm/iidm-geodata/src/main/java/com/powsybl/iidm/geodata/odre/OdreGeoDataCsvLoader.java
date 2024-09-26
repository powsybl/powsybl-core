/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.utils.InputUtils;
import com.powsybl.iidm.network.extensions.Coordinate;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataCsvLoader {

    protected OdreGeoDataCsvLoader() {
    }

    public static Map<String, Coordinate> getSubstationsCoordinates(Path path, OdreConfig odreConfig) throws IOException {
        try (Reader reader = InputUtils.toReader(path)) {
            return GeographicDataParser.parseSubstations(reader, odreConfig);
        }
    }

    public static Map<String, List<Coordinate>> getLinesCoordinates(Path aerialLinesFilePath, Path undergroundLinesFilePath, OdreConfig odreConfig) throws IOException {
        try (Reader aerialReader = InputUtils.toReader(aerialLinesFilePath);
             Reader undergroundReader = InputUtils.toReader(undergroundLinesFilePath)) {

            return GeographicDataParser.parseLines(aerialReader, undergroundReader, odreConfig);
        }
    }
}
