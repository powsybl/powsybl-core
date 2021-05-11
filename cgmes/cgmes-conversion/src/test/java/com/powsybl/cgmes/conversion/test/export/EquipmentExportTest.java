/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void microGridBE() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        test(new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void nodeBreakerHvdc() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        test(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2Inverter1Rectifier2().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void nordic32() throws IOException, XMLStreamException {
        test(new XMLImporter().importData(new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm")), null));
    }

    private void test(Network expected) throws IOException, XMLStreamException {

        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = Files.newOutputStream(exportedEq)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(expected);
            //context.getEqModelDescription().setVersion(eqVersion);
            EquipmentExport.write(expected, writer, context);
        }

        // Import the exported EQ file
        Properties properties = new Properties();
        Network actual = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), properties);

        // Export original XIIDM and with exported EQ file
        NetworkXml.writeAndValidate(expected, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"));
    }
}
