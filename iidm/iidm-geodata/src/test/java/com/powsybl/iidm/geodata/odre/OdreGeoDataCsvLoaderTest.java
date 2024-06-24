/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import com.powsybl.iidm.geodata.elements.LineGeoData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Hugo Kulesza {@literal <hugo.kulesza at rte-france.com>}
 */
class OdreGeoDataCsvLoaderTest {

    @Test
    void validSubstationsLineParsing() throws URISyntaxException, IOException {
        Path substationsPath = Paths.get(getClass()
                .getClassLoader().getResource("valid-line-name/substations.csv").toURI());
        Path undergroundLinesPath = Paths.get(getClass()
                .getClassLoader().getResource("valid-line-name/underground-lines.csv").toURI());
        Path linesPath = Paths.get(getClass()
                .getClassLoader().getResource("valid-line-name/aerial-lines.csv").toURI());

        List<LineGeoData> linesGeodata = OdreGeoDataCsvLoader.getLinesGeoData(linesPath, undergroundLinesPath, substationsPath, AbstractOdreTest.ODRE_CONFIG1)
                .stream().toList();

        assertEquals(2, linesGeodata.size());
        LineGeoData line1Position = linesGeodata.get(0);
        assertEquals("POST1L71POST3", line1Position.id());
        assertEquals("POST1", line1Position.substationEnd());
        assertEquals("", line1Position.substationStart());

        LineGeoData line2Position = linesGeodata.get(1);
        assertEquals("POST1L71POST2", line2Position.id());
        assertEquals("POST1", line2Position.substationEnd());
        assertEquals("POST2", line2Position.substationStart());
    }

}
