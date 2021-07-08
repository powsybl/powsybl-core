/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void smallGridHVDC() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), NetworkFactory.findDefault(), properties), getClass().getResourceAsStream("/smallGridHVDC.xml"));
    }

    @Test
    public void nordic32() throws IOException, XMLStreamException {
        test(new XMLImporter().importData(new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm")), null), getClass().getResourceAsStream("/nordic32.xml"));
    }

    private void test(Network network, InputStream expected) throws IOException, XMLStreamException {

        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = Files.newOutputStream(exportedEq)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            EquipmentExport.write(network, writer, context);
        }

        try (InputStream is = Files.newInputStream(exportedEq)) {
            ExportXmlCompare.compareNetworks(expected, is);
        }
    }
}
