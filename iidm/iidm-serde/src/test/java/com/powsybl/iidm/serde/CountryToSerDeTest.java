/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.BoundaryLineNetworkFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Clement Philipot {@literal <clement.philipot at rte-france.com>}
 */

class CountryToSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void countryToRoundTripTest() throws IOException {
        Network network = BoundaryLineNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2020-01-01T00:00:00Z"));
        BoundaryLine boundaryLine = network.getBoundaryLine("BL");
        boundaryLine.setCountryTo(Country.GB);

        Boundary boundary = boundaryLine.getBoundary();
        assertEquals(Country.FR, boundary.getCountry());
        assertEquals(Country.GB, boundary.getCountryTo());

        Path path = Files.createTempFile("countryTo", ".xiidm");
        try {
            NetworkSerDe.write(network, new ExportOptions().setVersion(IidmVersion.V_1_19.toString(".")), path);
            Network read = NetworkSerDe.read(Files.newInputStream(path), new ImportOptions(), null);
            BoundaryLine readBoundaryLine = read.getBoundaryLine("BL");
            assertEquals(Country.FR, readBoundaryLine.getBoundary().getCountry());
            assertEquals(Country.GB, readBoundaryLine.getBoundary().getCountryTo());
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    void backwardCompatibleV118ReadTest() throws IOException {
        Network network = BoundaryLineNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2020-01-01T00:00:00Z"));

        Path path = Files.createTempFile("countryTo-v1_18", ".xiidm");
        try {
            NetworkSerDe.write(network, new ExportOptions().setVersion(IidmVersion.V_1_18.toString(".")), path);
            Network read = NetworkSerDe.read(Files.newInputStream(path), new ImportOptions(), null);
            BoundaryLine boundaryLine = read.getBoundaryLine("BL");
            assertEquals(Country.FR, boundaryLine.getBoundary().getCountry());
            assertNull(boundaryLine.getBoundary().getCountryTo());
            assertNull(boundaryLine.getCountryTo());
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
