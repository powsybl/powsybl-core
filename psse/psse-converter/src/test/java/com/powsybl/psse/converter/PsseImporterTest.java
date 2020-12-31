/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.PssePowerFlowModel;
import com.powsybl.psse.model.pf.io.PowerFlowRawData33;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.psse.model.PsseVersion.fromRevision;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.*;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
public class PsseImporterTest extends AbstractConverterTest {

    @Test
    public void baseTest() {
        Importer importer = new PsseImporter();
        assertEquals("PSS/E", importer.getFormat());
        assertEquals("PSS/E Format to IIDM converter", importer.getComment());
        assertEquals(1, importer.getParameters().size());
        assertEquals("psse.import.ignore-base-voltage", importer.getParameters().get(0).getName());
    }

    private void testNetwork(Network network) throws IOException {
        Path file = fileSystem.getPath("/work/" + network.getId() + ".xiidm");
        network.setCaseDate(DateTime.parse("2016-01-01T10:00:00.000+02:00"));
        NetworkXml.write(network, file);
        try (InputStream is = Files.newInputStream(file)) {
            compareTxt(getClass().getResourceAsStream("/" + network.getId() + ".xiidm"), is);
        }
    }

    @Test
    public void existsTest() {
        PsseImporter importer = new PsseImporter();

        // test with a valid raw/RAW file
        assertTrue(importer.exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"))));
        assertTrue(importer.exists(new ResourceDataSource("IEEE_30_bus", new ResourceSet("/", "IEEE_30_bus.RAW"))));

        // test with an invalid extension
        assertFalse(importer.exists(new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.json"))));

        // test with not supported content
        ResourceDataSource dsCaseFlag = new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"));
        Assert.assertFalse(importer.exists(dsCaseFlag));

        ResourceDataSource dsCaseVersion = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        Assert.assertFalse(importer.exists(dsCaseVersion));

        // test with a valid extension and an invalid content
        ResourceDataSource dsCaseInvalid = new ResourceDataSource("fake", new ResourceSet("/", "fake.raw"));
        Assert.assertFalse(importer.exists(dsCaseInvalid));

        // test with a valid extension and an invalid content
        ResourceDataSource dsCaseInvalidx = new ResourceDataSource("fake", new ResourceSet("/", "fake.rawx"));
        Assert.assertFalse(importer.exists(dsCaseInvalidx));
    }

    public void importTest(String basename, String filename, boolean ignoreBaseVoltage) throws IOException {
        Properties properties = new Properties();
        properties.put("psse.import.ignore-base-voltage", ignoreBaseVoltage);

        ReadOnlyDataSource dataSource = new ResourceDataSource(basename, new ResourceSet("/", filename));
        Network network = new PsseImporter().importData(dataSource, new NetworkFactoryImpl(), properties);
        testNetwork(network);
    }

    @Test
    public void importTest14() throws IOException {
        importTest("IEEE_14_bus", "IEEE_14_bus.raw", false);
    }

    @Test
    public void importTest24() throws IOException {
        importTest("IEEE_24_bus", "IEEE_24_bus.raw", false);
    }

    @Test
    public void importTest57() throws IOException {
        importTest("IEEE_57_bus", "IEEE_57_bus.RAW", false);
    }

    @Test
    public void importTest118() throws IOException {
        importTest("IEEE_118_bus", "IEEE_118_bus.RAW", false);
    }

    @Test
    public void importTestT3W() throws IOException {
        importTest("ThreeMIB_T3W_modified", "ThreeMIB_T3W_modified.RAW", false);
    }

    @Test
    public void testRates() throws IOException {
        Context context = new Context();
        ReadOnlyDataSource ds = new ResourceDataSource("ThreeMIB_T3W_modified", new ResourceSet("/", "ThreeMIB_T3W_modified.RAW"));
        PssePowerFlowModel model = new PowerFlowRawData33().read(ds, "RAW", context);
        assertEquals(10451.0, model.getNonTransformerBranches().get(0).getRates().getRatea(), 0);
        assertEquals(10452.0, model.getNonTransformerBranches().get(0).getRates().getRateb(), 0);
        assertEquals(10453.0, model.getNonTransformerBranches().get(0).getRates().getRatec(), 0);
        assertEquals(10561.0, model.getNonTransformerBranches().get(1).getRates().getRatea(), 0);
        assertEquals(10562.0, model.getNonTransformerBranches().get(1).getRates().getRateb(), 0);
        assertEquals(10563.0, model.getNonTransformerBranches().get(1).getRates().getRatec(), 0);
        assertEquals(10140.0, model.getTransformers().get(0).getWinding1Rates().getRatea(), 0);
        assertEquals(10141.0, model.getTransformers().get(0).getWinding1Rates().getRateb(), 0);
        assertEquals(10142.0, model.getTransformers().get(0).getWinding1Rates().getRatec(), 0);
        assertEquals(101.0, model.getTransformers().get(1).getWinding1Rates().getRatea(), 0);
        assertEquals(102.0, model.getTransformers().get(1).getWinding1Rates().getRateb(), 0);
        assertEquals(103.0, model.getTransformers().get(1).getWinding1Rates().getRatec(), 0);
        assertEquals(201.0, model.getTransformers().get(1).getWinding2Rates().getRatea(), 0);
        assertEquals(202.0, model.getTransformers().get(1).getWinding2Rates().getRateb(), 0);
        assertEquals(203.0, model.getTransformers().get(1).getWinding2Rates().getRatec(), 0);
        assertEquals(301.0, model.getTransformers().get(1).getWinding3Rates().getRatea(), 0);
        assertEquals(302.0, model.getTransformers().get(1).getWinding3Rates().getRateb(), 0);
        assertEquals(303.0, model.getTransformers().get(1).getWinding3Rates().getRatec(), 0);
    }

    private static String toJson(PssePowerFlowModel model) throws JsonProcessingException {
        PsseVersion version = fromRevision(model.getCaseIdentification().getRev());
        SimpleBeanPropertyFilter filter = new SimpleBeanPropertyFilter() {
            @Override
            protected boolean include(PropertyWriter writer) {
                Revision rev = writer.getAnnotation(Revision.class);
                return rev == null || PsseVersioned.isValidVersion(version, rev);
            }
        };
        FilterProvider filters = new SimpleFilterProvider().addFilter("PsseVersionFilter", filter);
        return new ObjectMapper().writerWithDefaultPrettyPrinter().with(filters).writeValueAsString(model);
    }

    @Test()
    public void badModeTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("case-flag-not-supported", new ResourceSet("/", "case-flag-not-supported.raw"));
        PsseImporter psseImporter = new PsseImporter();
        NetworkFactory networkFactory = new NetworkFactoryImpl();
        assertThatExceptionOfType(PsseException.class)
                .isThrownBy(() -> psseImporter.importData(dataSource, networkFactory, null))
                .withMessage("Incremental load of data option (IC = 1) is not supported");
    }

    @Test
    public void badVersionTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        PsseImporter psseImporter = new PsseImporter();
        NetworkFactory networkFactory = new NetworkFactoryImpl();
        assertThatExceptionOfType(PsseException.class)
                .isThrownBy(() -> psseImporter.importData(dataSource, networkFactory, null))
                .withMessage("Version 29 not supported. Supported versions are: " + PsseVersion.supportedVersions());
    }

    @Test
    public void dataSourceExistsTest() {
        ReadOnlyDataSource dataSource;

        dataSource = new ResourceDataSource("version-not-supported", new ResourceSet("/", "version-not-supported.raw"));
        assertFalse(new PsseImporter().exists(dataSource));

        dataSource = new ResourceDataSource("IEEE_14_bus", new ResourceSet("/", "IEEE_14_bus.raw"));
        assertTrue(new PsseImporter().exists(dataSource));

        dataSource = new ResourceDataSource("IEEE_14_bus_rev35", new ResourceSet("/", "IEEE_14_bus_rev35.rawx"));
        assertTrue(new PsseImporter().exists(dataSource));
    }
}
