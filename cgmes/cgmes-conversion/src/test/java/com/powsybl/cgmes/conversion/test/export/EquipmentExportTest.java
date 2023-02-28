/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.EquipmentExport;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.cgmes.extensions.*;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractConverterTest;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.local.LocalComputationManager;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.xml.ExportOptions;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLImporter;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
class EquipmentExportTest extends AbstractConverterTest {

    @Test
    void smallGridHvdc() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallNodeBreaker() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallBusBranch() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.smallBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void smallGridHvdcWithCapabilityCurve() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void miniNodeBreaker() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniNodeBreaker().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportNodeBreaker(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void miniBusBranch() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.miniBusBranch().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportBusBranchNoBoundaries(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridWithTieFlowMappedToEquivalentInjection() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEWithTieFlowMappedToEquivalentInjection().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridBaseCaseAssembledSwitchAtBoundary() throws XMLStreamException, IOException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseAssembledSwitchAtBoundary().dataSource();
        Network network = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);

        network.newExtension(CgmesControlAreasAdder.class).add();
        CgmesControlAreas cgmesControlAreas = network.getExtension(CgmesControlAreas.class);
        CgmesControlArea cgmesControlArea = cgmesControlAreas.newCgmesControlArea()
                .setId("controlAreaId")
                .setName("controlAreaName")
                .setEnergyIdentificationCodeEic("energyIdentCodeEic")
                .setNetInterchange(Double.NaN)
                .add();
        TieLine tieLine = (TieLine) network.getLine("78736387-5f60-4832-b3fe-d50daf81b0a6 + 7f43f508-2496-4b64-9146-0a40406cbe49");
        cgmesControlArea.add(tieLine.getHalf2().getBoundary());

        // TODO(Luma) updated expected result after halves of tie lines are exported as equipment
        //  instead of an error logged and the tie flow ignored,
        //  the reimported network control area should contain one tie flow
        Network actual = exportImportNodeBreaker(network, dataSource);
        CgmesControlArea actualCgmesControlArea = actual.getExtension(CgmesControlAreas.class).getCgmesControlArea("controlAreaId");
        boolean tieFlowsAtTieLinesAreSupported = false;
        if (tieFlowsAtTieLinesAreSupported) {
            assertEquals(1, actualCgmesControlArea.getBoundaries().size());
            assertEquals("7f43f508-2496-4b64-9146-0a40406cbe49", actualCgmesControlArea.getBoundaries().iterator().next().getConnectable().getId());
        } else {
            assertEquals(0, actualCgmesControlArea.getBoundaries().size());
        }
    }

    @Test
    void microGrid() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridType4BE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void microGridCreateEquivalentInjectionAliases() throws IOException, XMLStreamException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1Catalog.microGridBaseCaseBE().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        // Remove aliases of equivalent injections, so they will have to be created during export
        for (DanglingLine danglingLine : expected.getDanglingLines()) {
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjection");
            danglingLine.removeProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "EquivalentInjectionTerminal");
        }
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    @Test
    void nordic32() throws IOException, XMLStreamException {
        ReadOnlyDataSource dataSource = new ResourceDataSource("nordic32", new ResourceSet("/cim14", "nordic32.xiidm"));
        Network network = new XMLImporter().importData(dataSource, NetworkFactory.findDefault(), null);
        exportToCgmesEQ(network);
        exportToCgmesTP(network);

        // Import EQ & TP file, no additional information (boundaries) are required
        Network actual = new CgmesImport().importData(new FileDataSource(tmpDir, "exported"), NetworkFactory.findDefault(), null);
        compareNetworksEQdata(network, actual);
    }

    @Test
    void bPerSectionTest() throws IOException, XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1Catalog.microGridType4BE().dataSource();

        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), properties);
        ShuntCompensatorLinearModel sh = (ShuntCompensatorLinearModel) network.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(0.024793, sh.getBPerSection(), 0.0);

        sh.setBPerSection(1E-14);

        Network reimported = exportImportBusBranch(network, ds);
        sh = (ShuntCompensatorLinearModel) reimported.getShuntCompensator("d771118f-36e9-4115-a128-cc3d9ce3e3da").getModel();
        assertEquals(1E-14, sh.getBPerSection(), 0.0);
    }

    @Test
    void threeWindingsTransformerTest() throws IOException, XMLStreamException {
        Network network = createThreeWindingTransformerNetwork();
        String t3id = "threeWindingsTransformer1";

        // Export an IIDM Network created from scratch, identifiers for tap changers will be created and stored in aliases
        exportToCgmesEQ(network);
        ThreeWindingsTransformer expected = network.getThreeWindingsTransformer(t3id);

        // The 3-winding transformer has a ratio and phase tap changer at every end
        Network network1 = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
        ThreeWindingsTransformer actual1 = network1.getThreeWindingsTransformer(t3id);
        for (int k = 1; k <= 3; k++) {
            String aliasType;
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual1.getAliasFromType(aliasType).get());
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual1.getAliasFromType(aliasType).get());
        }

        // Export an IIDM Network that has been imported from CGMES,
        // identifiers for tap changers must be preserved
        exportToCgmesEQ(network1);
        Network network2 = new CgmesImport().importData(new FileDataSource(tmpDir, "exportedEq"), NetworkFactory.findDefault(), null);
        ThreeWindingsTransformer actual2 = network2.getThreeWindingsTransformer(t3id);
        for (int k = 1; k <= 3; k++) {
            String aliasType;
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.RATIO_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual2.getAliasFromType(aliasType).get());
            aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + k;
            assertEquals(
                    expected.getAliasFromType(aliasType).get(),
                    actual2.getAliasFromType(aliasType).get());
        }
    }

    @Test
    void testLoadGroups() throws XMLStreamException, IOException {
        Properties properties = new Properties();
        properties.put(CgmesImport.CREATE_CGMES_EXPORT_MAPPING, "true");
        ReadOnlyDataSource dataSource = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEConformNonConformLoads().dataSource();
        Network expected = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), properties);
        Network actual = exportImportBusBranch(expected, dataSource);
        compareNetworksEQdata(expected, actual);
    }

    private Network exportImportNodeBreaker(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, false, true);
    }

    private Network exportImportBusBranch(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, true, true);
    }

    private Network exportImportBusBranchNoBoundaries(Network expected, ReadOnlyDataSource dataSource) throws IOException, XMLStreamException {
        return exportImport(expected, dataSource, true, false);
    }

    private Network createThreeWindingTransformerNetwork() {
        Network network = NetworkFactory.findDefault().createNetwork("network", "test");
        Substation substation1 = network.newSubstation()
                .setId("substation1")
                .setCountry(Country.FR)
                .setTso("TSO1")
                .setGeographicalTags("region1")
                .add();
        VoltageLevel voltageLevel1 = substation1.newVoltageLevel()
                .setId("voltageLevel1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel voltageLevel2 = substation1.newVoltageLevel()
                .setId("voltageLevel2")
                .setNominalV(220)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel voltageLevel3 = substation1.newVoltageLevel()
                .setId("voltageLevel3")
                .setNominalV(60)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();
        VoltageLevel.NodeBreakerView topology1 = voltageLevel1.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection1 = topology1.newBusbarSection()
                .setId("voltageLevel1BusbarSection1")
                .setNode(0)
                .add();
        VoltageLevel.NodeBreakerView topology2 = voltageLevel2.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection2 = topology2.newBusbarSection()
                .setId("voltageLevel1BusbarSection2")
                .setNode(0)
                .add();
        VoltageLevel.NodeBreakerView topology3 = voltageLevel3.getNodeBreakerView();
        BusbarSection voltageLevel1BusbarSection3 = topology3.newBusbarSection()
                .setId("voltageLevel1BusbarSection3")
                .setNode(0)
                .add();
        ThreeWindingsTransformerAdder threeWindingsTransformerAdder1 = substation1.newThreeWindingsTransformer()
                .setId("threeWindingsTransformer1")
                .setRatedU0(400);
        threeWindingsTransformerAdder1.newLeg1()
                .setNode(1)
                .setR(0.001)
                .setX(0.000001)
                .setB(0)
                .setG(0)
                .setRatedU(400)
                .setVoltageLevel("voltageLevel1")
                .add();
        threeWindingsTransformerAdder1.newLeg2()
                .setNode(1)
                .setR(0.1)
                .setX(0.00001)
                .setB(0)
                .setG(0)
                .setRatedU(220)
                .setVoltageLevel("voltageLevel2")
                .add();
        threeWindingsTransformerAdder1.newLeg3()
                .setNode(1)
                .setR(0.01)
                .setX(0.0001)
                .setB(0)
                .setG(0)
                .setRatedU(60)
                .setVoltageLevel("voltageLevel3")
                .add();
        ThreeWindingsTransformer threeWindingsTransformer1 = threeWindingsTransformerAdder1.add();
        threeWindingsTransformer1.getLeg1().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.01)
                    .setX(0.0001)
                    .setB(0)
                    .setG(0)
                    .setRho(1.1)
                    .endStep()
                .add();
        threeWindingsTransformer1.getLeg2().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.02)
                    .setX(0.0002)
                    .setB(0)
                    .setG(0)
                    .setRho(1.2)
                    .endStep()
                .add();
        threeWindingsTransformer1.getLeg3().newRatioTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.03)
                    .setX(0.0003)
                    .setB(0)
                    .setG(0)
                    .setRho(1.3)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.01)
                    .setX(0.0001)
                    .setB(0)
                    .setG(0)
                    .setRho(1.1)
                    .setAlpha(10)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg2().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.02)
                    .setX(0.0002)
                    .setB(0)
                    .setG(0)
                    .setRho(1.2)
                    .setAlpha(20)
                .endStep()
                .add();
        threeWindingsTransformer1.getLeg3().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                    .setR(0.03)
                    .setX(0.0003)
                    .setB(0)
                    .setG(0)
                    .setRho(1.3)
                    .setAlpha(30)
                .endStep()
                .add();

        topology1.newDisconnector()
                .setId("Disconnector1")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg1().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection1.getTerminal().getNodeBreakerView().getNode())
                .add();
        topology2.newDisconnector()
                .setId("Disconnector2")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg2().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection2.getTerminal().getNodeBreakerView().getNode())
                .add();
        topology3.newDisconnector()
                .setId("Disconnector3")
                .setOpen(false)
                .setNode1(threeWindingsTransformer1.getLeg3().getTerminal().getNodeBreakerView().getNode())
                .setNode2(voltageLevel1BusbarSection3.getTerminal().getNodeBreakerView().getNode())
                .add();

        return network;
    }

    private Network exportImport(Network expected, ReadOnlyDataSource dataSource, boolean importTP, boolean importBD) throws IOException, XMLStreamException {
        Path exportedEq = exportToCgmesEQ(expected);

        // From reference data source we use only boundaries
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_EQ.xml", exportedEq);
        if (importTP) {
            r.with("test_TP.xml", Repackager::tp);
        }
        if (importBD) {
            r.with("test_EQ_BD.xml", Repackager::eqBd)
                    .with("test_TP_BD.xml", Repackager::tpBd);
        }
        r.zip(repackaged);

        // Import with new EQ
        // There is no need to create the IIDM-CGMES mappings
        // We are reading only an EQ, we won't have TP data in the input
        // And to compare the expected and actual networks we are dropping all IIDM-CGMES mapping context information
        return Network.read(repackaged, LocalComputationManager.getDefault(), ImportConfig.load(), null);
    }

    private Path exportToCgmesEQ(Network network) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedEq = tmpDir.resolve("exportedEq.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedEq))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network).setExportEquipment(true);
            EquipmentExport.write(network, writer, context);
        }

        return exportedEq;
    }

    private Path exportToCgmesTP(Network network) throws IOException, XMLStreamException {
        // Export CGMES EQ file
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedTp))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            CgmesExportContext context = new CgmesExportContext(network);
            TopologyExport.write(network, writer, context);
        }

        return exportedTp;
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

        compareTemporaryLimits(Network.read(tmpDir.resolve("expected.xml")), Network.read(tmpDir.resolve("actual.xml")));
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
        actual.getActivePowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits1().orElse(null), lim));
        actual.getActivePowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits2().orElse(null), lim));
        actual.getApparentPowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits1().orElse(null), lim));
        actual.getApparentPowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits2().orElse(null), lim));
        actual.getCurrentLimits1().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits1().orElse(null), lim));
        actual.getCurrentLimits2().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits2().orElse(null), lim));
    }

    private void compareFlowBranchLimits(FlowsLimitsHolder expected, Line actual) {
        actual.getActivePowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getActivePowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits1().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits2().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getCurrentLimits1().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
        actual.getCurrentLimits2().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
    }

    private void compareFlowLimits(FlowsLimitsHolder expected, FlowsLimitsHolder actual) {
        actual.getActivePowerLimits().ifPresent(lim -> compareLoadingLimits(expected.getActivePowerLimits().orElse(null), lim));
        actual.getApparentPowerLimits().ifPresent(lim -> compareLoadingLimits(expected.getApparentPowerLimits().orElse(null), lim));
        actual.getCurrentLimits().ifPresent(lim -> compareLoadingLimits(expected.getCurrentLimits().orElse(null), lim));
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
                shuntCompensator.getTerminal().setP(0.0);
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
