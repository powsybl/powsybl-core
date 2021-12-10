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
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.cgmes.extensions.CgmesIidmMapping;
import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void smallGridHvdc() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(), NetworkFactory.findDefault(), properties));
    }

    @Test
    public void smallGridHvdcWithCapabilityCurve() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        test(new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve().dataSource(), NetworkFactory.findDefault(), properties));
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
        test(new XMLImporter().importData(new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm")), NetworkFactory.findDefault(), null));
    }

    @Test
    public void bPerSectionTest() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.microGridType4BE().dataSource(), NetworkFactory.findDefault(), properties);
        ShuntCompensatorLinearModel model = (ShuntCompensatorLinearModel) network.getShuntCompensator("_d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        model.setBPerSection(1E-14);

        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            EquipmentExport.write(network, writer, context);
        }

        Network actual = prepareNetwork(new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), new Properties()));
        model = (ShuntCompensatorLinearModel) actual.getShuntCompensator("_d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(1E-14, model.getBPerSection(), 0.0);
    }

    private void test(Network network) throws IOException, XMLStreamException {

        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
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

        compareTemporaryLimits(xiidm(tmpDir, "expected"), xiidm(tmpDir, "actual"));
    }

    private Network xiidm(Path basePath, String baseName) {
        XMLImporter xmli = new XMLImporter();
        ReadOnlyDataSource ds = new FileDataSource(basePath, baseName);
        Network n = xmli.importData(ds, NetworkFactory.findDefault(), null);
        return n;
    }

    private void compareTemporaryLimits(Network expected, Network actual) {
        for (Line line : actual.getLines()) {
            Identifiable identifiable = expected.getIdentifiable(line.getId());
            if (identifiable instanceof Branch) {
                compareBranchLimits((Branch) identifiable, line);
            } else {
                compareFlowBranchLimits((FlowsLimitsHolder) identifiable, line);
            }
        }
        for (TwoWindingsTransformer twt : actual.getTwoWindingsTransformers()) {
            compareBranchLimits((Branch) expected.getIdentifiable(twt.getId()), twt);
        }
        for (ThreeWindingsTransformer twt : actual.getThreeWindingsTransformers()) {
            ThreeWindingsTransformer expectedTwt = (ThreeWindingsTransformer) expected.getIdentifiable(twt.getId());
            compareFlowLimits(expectedTwt.getLeg1(), twt.getLeg1());
            compareFlowLimits(expectedTwt.getLeg2(), twt.getLeg2());
            compareFlowLimits(expectedTwt.getLeg3(), twt.getLeg3());
        }
        for (DanglingLine danglingLine : actual.getDanglingLines()) {
            compareFlowLimits((FlowsLimitsHolder) expected.getIdentifiable(danglingLine.getId()), danglingLine);
        }
    }

    private void compareBranchLimits(Branch<?> expected, Branch<?> actual) {
        if (actual.getActivePowerLimits1() != null) {
            compareLoadingLimits(expected.getActivePowerLimits1(), actual.getActivePowerLimits1());
        }
        if (actual.getActivePowerLimits2() != null) {
            compareLoadingLimits(expected.getActivePowerLimits2(), actual.getActivePowerLimits2());
        }
        if (actual.getApparentPowerLimits1() != null) {
            compareLoadingLimits(expected.getApparentPowerLimits1(), actual.getApparentPowerLimits1());
        }
        if (actual.getApparentPowerLimits2() != null) {
            compareLoadingLimits(expected.getApparentPowerLimits2(), actual.getApparentPowerLimits2());
        }
        if (actual.getCurrentLimits1() != null) {
            compareLoadingLimits(expected.getCurrentLimits1(), actual.getCurrentLimits1());
        }
        if (actual.getCurrentLimits2() != null) {
            compareLoadingLimits(expected.getCurrentLimits2(), actual.getCurrentLimits2());
        }
    }

    private void compareFlowBranchLimits(FlowsLimitsHolder expected, Line actual) {
        if (actual.getActivePowerLimits1() != null) {
            compareLoadingLimits(expected.getActivePowerLimits(), actual.getActivePowerLimits1());
        }
        if (actual.getActivePowerLimits2() != null) {
            compareLoadingLimits(expected.getActivePowerLimits(), actual.getActivePowerLimits2());
        }
        if (actual.getApparentPowerLimits1() != null) {
            compareLoadingLimits(expected.getApparentPowerLimits(), actual.getApparentPowerLimits1());
        }
        if (actual.getApparentPowerLimits2() != null) {
            compareLoadingLimits(expected.getApparentPowerLimits(), actual.getApparentPowerLimits2());
        }
        if (actual.getCurrentLimits1() != null) {
            compareLoadingLimits(expected.getCurrentLimits(), actual.getCurrentLimits1());
        }
        if (actual.getCurrentLimits2() != null) {
            compareLoadingLimits(expected.getCurrentLimits(), actual.getCurrentLimits2());
        }
    }

    private void compareFlowLimits(FlowsLimitsHolder expected, FlowsLimitsHolder actual) {
        if (actual.getActivePowerLimits() != null) {
            compareLoadingLimits(expected.getActivePowerLimits(), actual.getActivePowerLimits());
        }
        if (actual.getApparentPowerLimits() != null) {
            compareLoadingLimits(expected.getApparentPowerLimits(), actual.getApparentPowerLimits());
        }
        if (actual.getCurrentLimits() != null) {
            compareLoadingLimits(expected.getCurrentLimits(), actual.getCurrentLimits());
        }
    }

    private void compareLoadingLimits(LoadingLimits expected, LoadingLimits actual) {
        if (!actual.getTemporaryLimits().isEmpty()) {
            assertTrue(!expected.getTemporaryLimits().isEmpty());
            Iterator<LoadingLimits.TemporaryLimit> iterator = actual.getTemporaryLimits().iterator();
            while (iterator.hasNext()) {
                LoadingLimits.TemporaryLimit temporaryLimit = iterator.next();
                int acceptableDuration = temporaryLimit.getAcceptableDuration();
                assertEquals(expected.getTemporaryLimit(acceptableDuration).getValue(), temporaryLimit.getValue(), 0.0);
            }
        } else {
            assertTrue(expected.getTemporaryLimits().isEmpty());
        }
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
                staticVarCompensator.setRegulationMode(StaticVarCompensator.RegulationMode.OFF).setVoltageSetpoint(0.0);
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
