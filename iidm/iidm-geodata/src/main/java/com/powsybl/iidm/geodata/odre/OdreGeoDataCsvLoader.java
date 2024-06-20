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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
public class OdreGeoDataCsvLoader {

    protected OdreGeoDataCsvLoader() {
    }

    public static List<SubstationGeoData> getSubstationsGeoData(Path path, OdreConfig odreConfig) {
        try (Reader reader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path))))) {
            if (FileValidator.validateSubstations(path, odreConfig)) {
                return new ArrayList<>(GeographicDataParser.parseSubstations(reader, odreConfig).values());
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<LineGeoData> getLinesGeoData(Path aerialLinesFilePath, Path undergroundLinesFilePath,
                                                    Path substationPath, OdreConfig odreConfig) {
        Map<String, Reader> mapValidation = FileValidator.validateLines(List.of(substationPath,
                aerialLinesFilePath, undergroundLinesFilePath), odreConfig);
        List<LineGeoData> result = Collections.emptyList();
        try {
            if (mapValidation.size() == 3) {
                result = new ArrayList<>(GeographicDataParser.parseLines(mapValidation.get(FileValidator.AERIAL_LINES),
                        mapValidation.get(FileValidator.UNDERGROUND_LINES),
                        GeographicDataParser.parseSubstations(mapValidation.get(FileValidator.SUBSTATIONS), odreConfig), odreConfig).values());
            }
        } finally {
            mapValidation.values().forEach(IOUtils::closeQuietly);
        }
        return result;
    }
}
