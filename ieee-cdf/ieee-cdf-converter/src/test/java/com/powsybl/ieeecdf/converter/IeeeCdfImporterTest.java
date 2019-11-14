/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.converter;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class IeeeCdfImporterTest extends AbstractConverterTest {

    @Test
    public void baseTest() {
        Importer importer = new IeeeCdfImporter();
        assertEquals("IEEE CDF", importer.getFormat());
        assertEquals("IEEE Common Data Format to IIDM converter", importer.getComment());
        assertTrue(importer.getParameters().isEmpty());
    }

    @Test
    public void copyTest() {
        new IeeeCdfImporter().copy(new ResourceDataSource("ieee14cdf", new ResourceSet("/", "ieee14cdf.txt")),
                                   new FileDataSource(fileSystem.getPath("/work"), "copy"));
        assertTrue(Files.exists(fileSystem.getPath("/work").resolve("copy.txt")));
    }

    @Test
    public void existsTest() {
        assertTrue(new IeeeCdfImporter().exists(new ResourceDataSource("ieee14cdf", new ResourceSet("/", "ieee14cdf.txt"))));
    }

    private void testNetwork(Network network) throws IOException {
        Path file = fileSystem.getPath("/work/" + network.getId() + ".xiidm");
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + network.getId() + ".xiidm"), is);
        }
    }

    @Test
    public void testIeee14() throws IOException {
        testNetwork(IeeeCdfNetworkFactory.create14());
    }
}
