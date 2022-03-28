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
import com.powsybl.cgmes.extensions.CgmesSshMetadata;
import com.powsybl.cgmes.extensions.CgmesSvMetadata;
import com.powsybl.cgmes.extensions.CimCharacteristics;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
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
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class EquipmentExportTest extends AbstractConverterTest {

    @Test
    public void smallGridHvdc() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        testExportReimport(network, dataSource);
    }

    @Test
    public void smallGridHvdcWithCapabilityCurve() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        testExportReimport(network, dataSource);
    }

    @Test
    public void miniGrid() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniNodeBreaker().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        testExportReimport(network, dataSource);
    }

    @Test
    public void microGrid() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridType4BE().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        testExportReimport(network, dataSource);
    }

    @Test
    public void nordic32() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        testExportReimport(network);
    }

    @Test
    public void bPerSectionTest() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridType4BE().dataSource();

        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), properties);
        ShuntCompensatorLinearModel sh = (ShuntCompensatorLinearModel) network.getShuntCompensator("_d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(0.024793, sh.getBPerSection(), 0.0);

        sh.setBPerSection(1E-14);

        Network reimported = exportReimport(network, ds);
        sh = (ShuntCompensatorLinearModel) reimported.getShuntCompensator("_d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(1E-14, sh.getBPerSection(), 0.0);
    }

    private void testExportReimport(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        Network actual = exportReimport(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    private Network exportReimport(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        Path exportedEq = exportToCgmesEQ(expected);

        // From reference data source we use only boundaries
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", exportedEq)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        r.zip(repackaged);

        // Import with new EQ
        // There is no need to create the IIDM-CGMES mappings
        // We are reading only an EQ, we won't have TP data in the input
        // And to compare the expected and actual networks we are dropping all IIDM-CGMES mapping context information
        return Importers.loadNetwork(repackaged, LocalComputationManager.getDefault(), ImportConfig.load(), null);
    }

    private void testExportReimport(Network network) throws IOException, XMLStreamException {
        exportToCgmesEQ(network);

        // Import just the EQ file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
        compareNetworksEQdata(network, actual);
    }

    private Path exportToCgmesEQ(Network network) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            EquipmentExport.write(network, writer, context);
        }

        return exportedEq;
    }

    private void compareNetworksEQdata(Network expected, Network actual) throws IOException {
        Network expectedNetwork = prepareNetworkForEQComparison(expected);
        Network actualNetwork = prepareNetworkForEQComparison(actual);

        // Export original and only EQ
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        exportOptions.setSorted(true);
        NetworkXml.writeAndValidate(expectedNetwork, exportOptions, tmpDir.resolve("expected.xml"));
        NetworkXml.writeAndValidate(actualNetwork, exportOptions, tmpDir.resolve("actual.xml"));

        // Compare
        ExportXmlCompare.compareEQNetworks(tmpDir.resolve("expected.xml"), tmpDir.resolve("actual.xml"), DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                ExportXmlCompare::numericDifferenceEvaluator,
                ExportXmlCompare::ignoringNonEQ));

        compareTemporaryLimits(Importers.loadNetwork(tmpDir.resolve("expected.xml")), Importers.loadNetwork(tmpDir.resolve("actual.xml")));
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
            assertFalse(expected.getTemporaryLimits().isEmpty());
            for (LoadingLimits.TemporaryLimit temporaryLimit : actual.getTemporaryLimits()) {
                int acceptableDuration = temporaryLimit.getAcceptableDuration();
                assertEquals(expected.getTemporaryLimit(acceptableDuration).getValue(), temporaryLimit.getValue(), 0.0);
            }
        } else {
            assertTrue(expected.getTemporaryLimits().isEmpty());
        }
    }

    private Network prepareNetworkForEQComparison(Network network) {
        network.getAliases().forEach(network::removeAlias);
        network.getIdentifiables().forEach(identifiable -> identifiable.getAliases().forEach(identifiable::removeAlias));

        network.getVoltageLevels().forEach(vl ->
                vl.getBusView().getBuses().forEach(bus -> {
                    bus.setV(Double.NaN);
                    bus.setAngle(Double.NaN);
                })
        );
        network.getIdentifiables().forEach(identifiable -> {
            if (identifiable instanceof Bus) {
                // Nothing to do
            } else if (identifiable instanceof BusbarSection) {
                // Nothing to do
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
        network.removeExtension(CimCharacteristics.class);

        return network;
    }
}
