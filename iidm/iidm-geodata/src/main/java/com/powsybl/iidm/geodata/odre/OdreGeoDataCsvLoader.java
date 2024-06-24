/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.elements.LineGeoData;
import com.powsybl.iidm.geodata.elements.SubstationGeoData;
import com.powsybl.iidm.geodata.utils.InputUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataCsvLoader {

    protected OdreGeoDataCsvLoader() {
    }

    public static Collection<SubstationGeoData> getSubstationsGeoData(Path path, OdreConfig odreConfig) throws IOException {
        try (Reader reader = InputUtils.toReader(path)) {
            return GeographicDataParser.parseSubstations(reader, odreConfig).values();
        }
    }

    public static Collection<LineGeoData> getLinesGeoData(Path aerialLinesFilePath, Path undergroundLinesFilePath,
                                                    Path substationPath, OdreConfig odreConfig) throws IOException {
        try (Reader substationReader = InputUtils.toReader(substationPath)) {
            return getLinesGeoData(aerialLinesFilePath, undergroundLinesFilePath, GeographicDataParser.parseSubstations(substationReader, odreConfig), odreConfig);
        }
    }

    public static Collection<LineGeoData> getLinesGeoData(Path aerialLinesFilePath, Path undergroundLinesFilePath,
                                                          Map<String, SubstationGeoData> substations, OdreConfig odreConfig) throws IOException {
        try (Reader aerialReader = InputUtils.toReader(aerialLinesFilePath);
             Reader undergroundReader = InputUtils.toReader(undergroundLinesFilePath)) {

            Map<String, LineGeoData> lineGeoDataMap = GeographicDataParser.parseLines(aerialReader, undergroundReader, substations, odreConfig);
            return lineGeoDataMap.values();
        }
    }
}
