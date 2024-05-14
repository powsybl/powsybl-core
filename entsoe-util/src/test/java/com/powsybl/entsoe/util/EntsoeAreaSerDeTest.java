/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.entsoe.util;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.ImportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class EntsoeAreaSerDeTest extends AbstractSerDeTest {

    private static Network createTestNetwork() {
        Network network = Network.create("test", "test");
        network.setCaseDate(ZonedDateTime.parse("2016-06-27T12:27:58.535+02:00"));
        Substation s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add();
        return network;
    }

    @Test
    void test() throws IOException {
        Network network = createTestNetwork();

        // extends substation
        Substation s = network.getSubstation("S");
        s.newExtension(EntsoeAreaAdder.class).withCode(EntsoeGeographicalCode.BE).add();
        EntsoeArea country = s.getExtension(EntsoeArea.class);

        Network network2 = roundTripXmlTest(network,
                this::jsonWriteAndRead,
                NetworkSerDe::write,
                NetworkSerDe::validateAndRead,
                "/entsoeAreaRef.xml");

        Substation s2 = network2.getSubstation("S");
        EntsoeArea country2 = s2.getExtension(EntsoeArea.class);
        assertNotNull(country2);
        assertEquals(country.getCode(), country2.getCode());
    }

    private Network jsonWriteAndRead(Network network, Path path) {
        var anonymizer = NetworkSerDe.write(network, new ExportOptions().setFormat(TreeDataFormat.JSON), path);
        try (InputStream is = Files.newInputStream(path)) {
            return NetworkSerDe.read(is, new ImportOptions().setFormat(TreeDataFormat.JSON), anonymizer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
