/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata;

import com.powsybl.iidm.geodata.dto.LineGeoData;
import com.powsybl.iidm.geodata.dto.SubstationGeoData;
import com.powsybl.iidm.geodata.utils.GeographicDataParser;
import com.powsybl.iidm.geodata.utils.InputUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hugo Kulesza <hugo.kulesza at rte-france.com>
 */
public class OdreGeoDataCsvLoader {

    public static List<SubstationGeoData> getSubstationsGeoData(Path path) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(path))))) {
            return new ArrayList<>(GeographicDataParser.parseSubstations(bufferedReader).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<LineGeoData> getLinesGeoData(Path aerialLinesFilePath, Path undergroundLinesFilePath, Path substationPath) {
        try (BufferedReader aerialBufferedReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(aerialLinesFilePath))));
            BufferedReader undergroundBufferedReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(undergroundLinesFilePath))));
            BufferedReader substationBufferedReader = new BufferedReader(new InputStreamReader(InputUtils.toBomInputStream(Files.newInputStream(substationPath))));
            ) {
            return new ArrayList<>(GeographicDataParser.parseLines(aerialBufferedReader, undergroundBufferedReader,
                GeographicDataParser.parseSubstations(substationBufferedReader)).values());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
