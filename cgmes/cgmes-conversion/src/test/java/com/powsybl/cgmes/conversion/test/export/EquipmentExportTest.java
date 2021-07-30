/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.Test;
import org.xmlunit.diff.DifferenceEvaluators;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Properties;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void smallGridHVDC() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void miniGrid() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1Catalog.miniNodeBreaker().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void microGrid() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void nordic32() throws IOException, XMLStreamException {
        test(new XMLImporter().importData(new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm")), null));
    }

    private void test(Network network) throws IOException, XMLStreamException {

        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = Files.newOutputStream(exportedEq)) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            EquipmentExport.write(network, writer, context);
        }

        Network expected = prepareNetwork(network);
        Network actual = prepareNetwork(new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), new Properties()));

        // Export original and only EQ
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        NetworkXml.writeAndValidate(expected, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actual, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareEQNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"), DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ));
    }

    private Network prepareNetwork(Network network) {
        network.getAliases().forEach(alias -> network.removeAlias(alias));
        network.getIdentifiables().forEach(identifiable -> identifiable.getAliases().forEach(alias -> identifiable.removeAlias(alias)));

        network.getVoltageLevels().forEach(vl -> {
            vl.getBusView().getBuses().forEach(bus -> {
                bus.setV(Double.NaN);
                bus.setAngle(Double.NaN);
            });
        });
        network.getIdentifiables().forEach(identifiable -> {
            if (identifiable instanceof Bus) {
            } else if (identifiable instanceof BusbarSection) {
            } else if (identifiable instanceof ShuntCompensator) {
                ShuntCompensator shuntCompensator = (ShuntCompensator) identifiable;
                shuntCompensator.setVoltageRegulatorOn(false);
                shuntCompensator.setTargetV(Double.NaN);
                shuntCompensator.setTargetDeadband(Double.NaN);
                shuntCompensator.getTerminal().setQ(0.0);
            } else if (identifiable instanceof Generator) {
                Generator generator = (Generator) identifiable;
                generator.setVoltageRegulatorOn(false);
                generator.setTargetV(Double.NaN);
                generator.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof StaticVarCompensator) {
                StaticVarCompensator staticVarCompensator = (StaticVarCompensator) identifiable;
                staticVarCompensator.setRegulationMode(StaticVarCompensator.RegulationMode.OFF);
                staticVarCompensator.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof VscConverterStation) {
                VscConverterStation converter = (VscConverterStation) identifiable;
                converter.setVoltageRegulatorOn(false);
                converter.setLossFactor(0.8f);
                converter.setVoltageSetpoint(Double.NaN);
                converter.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof LccConverterStation) {
                LccConverterStation converter = (LccConverterStation) identifiable;
                converter.setPowerFactor(0.8f);
                converter.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof Injection) {
                Injection injection = (Injection) identifiable;
                injection.getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof HvdcLine) {
                HvdcLine hvdcLine = (HvdcLine) identifiable;
                hvdcLine.setActivePowerSetpoint(0.0);
                hvdcLine.setMaxP(0.0);
                hvdcLine.getConverterStation1().getTerminal().setP(0.0).setQ(0.0);
                hvdcLine.getConverterStation2().getTerminal().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof Branch) {
                Branch branch = (Branch) identifiable;
                branch.getTerminal1().setP(0.0).setQ(0.0);
                branch.getTerminal2().setP(0.0).setQ(0.0);
            } else if (identifiable instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer threeWindingsTransformer = (ThreeWindingsTransformer) identifiable;
                threeWindingsTransformer.getLeg1().getTerminal().setP(0.0).setQ(0.0);
                threeWindingsTransformer.getLeg2().getTerminal().setP(0.0).setQ(0.0);
                threeWindingsTransformer.getLeg3().getTerminal().setP(0.0).setQ(0.0);
            }
        });
        for (Load load : network.getLoads()) {
            load.setP0(0.0).setQ0(0.0);
        }

        network.removeExtension(CgmesModelExtension.class);
        network.removeExtension(CgmesSshMetadata.class);
        network.removeExtension(CgmesSvMetadata.class);
        network.removeExtension(CgmesIidmMapping.class);
        network.removeExtension(CimCharacteristics.class);

        return network;
    }
}
