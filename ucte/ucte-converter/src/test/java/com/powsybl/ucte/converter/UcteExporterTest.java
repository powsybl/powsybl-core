/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.MemDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest extends AbstractConverterTest {

    /**
     * Utility method to load a network file from resource directory without calling
     * @param filePath path of the file relative to resources directory
     * @return imported network
     */
    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    private static void testExporter(Network network, String reference) throws IOException {
        MemDataSource dataSource = new MemDataSource();

        UcteExporter exporter = new UcteExporter();
        exporter.export(network, new Properties(), dataSource);

        try (InputStream actual = dataSource.newInputStream(null, "uct");
             InputStream expected = UcteExporterTest.class.getResourceAsStream(reference)) {
            compareTxt(expected, actual, Arrays.asList(1, 2));
        }
    }

    @Test
    public void testMerge() throws IOException {
        Network networkFR = loadNetworkFromResourceFile("/frTestGridForMerging.uct");
        testExporter(networkFR, "/frTestGridForMerging.uct");

        Network networkBE = loadNetworkFromResourceFile("/beTestGridForMerging.uct");
        testExporter(networkBE, "/beTestGridForMerging.uct");

        Network merge = Network.create("merge", "UCT");
        merge.merge(networkBE, networkFR);
        testExporter(merge, "/uxTestGridForMerging.uct");
    }

    @Test
    public void testExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/expectedExport.uct");
        testExporter(network, "/expectedExport.uct");
    }

    @Test
    public void getFormatTest() {
        UcteExporter exporter = new UcteExporter();
        assertEquals("UCTE", exporter.getFormat());
        assertNotEquals("IIDM", exporter.getFormat());
    }

    @Test
    public void getCommentTest() {
        UcteExporter exporter = new UcteExporter();
        assertEquals("IIDM to UCTE converter", exporter.getComment());
        assertNotEquals("UCTE to IIDM converter", exporter.getComment());
    }

    @Test
    public void testCouplerToXnodeImport() throws IOException {
        Network network = loadNetworkFromResourceFile("/couplerToXnodeExample.uct");
        testExporter(network, "/couplerToXnodeExample.uct");
    }

    @Test
    public void shouldNotUseScientificalNotationForExport() throws IOException {
        Network network = loadNetworkFromResourceFile("/testGridNoScientificNotation.uct");
        testExporter(network, "/testGridNoScientificNotation.uct");
    }

    @Test
    public void testDefaultOneNamingStrategy() {
        NamingStrategy defaultNamingStrategy = UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy()));
        assertEquals("Default", defaultNamingStrategy.getName());
    }

    @Test
    public void testDefaultTwoNamingStrategies() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testDefaultNoNamingStrategy() {
        try {
            UcteExporter.findNamingStrategy(null, ImmutableList.of());
            fail();
        } catch (PowsyblException ignored) {
        }
    }

    @Test
    public void testChosenTwoNamingStrategies() {
        NamingStrategy namingStrategy = UcteExporter.findNamingStrategy("Default", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("Default", namingStrategy.getName());
        namingStrategy = UcteExporter.findNamingStrategy("OtherNamingStrategy", ImmutableList.of(new DefaultNamingStrategy(), new OtherNamingStrategy()));
        assertEquals("OtherNamingStrategy", namingStrategy.getName());
    }

    @Test
    public void testWithIdDuplicationBetweenLineAndTransformer() throws IOException {
        Network network = loadNetworkFromResourceFile("/id_duplication_test.uct");
        testExporter(network, "/id_duplication_test.uct");
    }
}
