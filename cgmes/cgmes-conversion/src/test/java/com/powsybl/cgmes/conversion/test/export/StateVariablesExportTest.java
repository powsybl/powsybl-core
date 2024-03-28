/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conformity.*;
import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.export.CgmesExportContext;
import com.powsybl.cgmes.conversion.export.StateVariablesExport;
import com.powsybl.cgmes.conversion.export.TopologyExport;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.cgmes.model.PowerFlow;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.computation.DefaultComputationManagerConfig;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferenceTerminals;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
class StateVariablesExportTest extends AbstractSerDeTest {

    private Properties importParams;

    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
    }

    @Test
    void microGridBE() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(),
                false,
                2,
                false,
                StateVariablesExportTest::addRepackagerFiles));
    }

    @Test
    void microGridBEFlowsForSwitches() throws IOException, XMLStreamException {
        // Activate export of flows for switches on a small network with few switches,
        // Writing flows for all switches has impact on performance
        assertTrue(test(CgmesConformity1Catalog.microGridBaseCaseNL().dataSource(),
                false,
                2,
                true,
                StateVariablesExportTest::addRepackagerFiles));
    }

    @Test
    void minimalNodeBreakerFlowsForSwitches() throws XMLStreamException {
        Network n = Network.create("minimal", "iidm");
        Substation s1 = n.newSubstation().setId("S1").add();
        Substation s2 = n.newSubstation().setId("S2").add();
        VoltageLevel vl1 = s1.newVoltageLevel().setId("VL1").setNominalV(100).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.newLoad().setId("LOAD").setNode(0)
                .setP0(10).setQ0(1).add();
        vl1.getNodeBreakerView().newBreaker().setId("BK11").setNode1(0).setNode2(1).add();
        vl1.getNodeBreakerView().newBusbarSection().setId("BBS1").setNode(1).add();
        vl1.getNodeBreakerView().newBreaker().setId("BK12").setNode1(1).setNode2(2).add();
        VoltageLevel vl2 = s2.newVoltageLevel().setId("VL2").setNominalV(100).setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl2.newGenerator().setId("GEN").setNode(0)
                .setTargetP(10.01).setTargetQ(1.10)
                .setMinP(0).setMaxP(20)
                .setVoltageRegulatorOn(false).add();
        vl2.getNodeBreakerView().newBreaker().setId("BK21").setNode1(0).setNode2(1).add();
        vl2.getNodeBreakerView().newBusbarSection().setId("BBS2").setNode(1).add();
        vl2.getNodeBreakerView().newBreaker().setId("BK22").setNode1(1).setNode2(2).add();
        n.newLine().setId("LINE").setVoltageLevel1("VL1").setVoltageLevel2("VL2").setNode1(2).setNode2(2)
                .setR(1).setX(10).add();

        Bus b1 = vl1.getBusView().getBuses().iterator().next();
        Bus b2 = vl2.getBusView().getBuses().iterator().next();
        b2.setV(100.0).setAngle(0);
        b1.setV(99.7946677).setAngle(-0.568404640);

        new LoadFlowResultsCompletion(new LoadFlowResultsCompletionParameters(), new LoadFlowParameters()).run(n, null);
        String sv = exportSvAsString(n, 1, true);

        assertEqualsPowerFlow(new PowerFlow(10, 1), extractSvPowerFlow(sv, cgmesTerminal(n, "LOAD", 1)));
        assertEqualsPowerFlow(new PowerFlow(-10, -1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK11", 1)));
        assertEqualsPowerFlow(new PowerFlow(10, 1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK11", 2)));
        assertEqualsPowerFlow(new PowerFlow(-10, -1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK12", 1)));
        assertEqualsPowerFlow(new PowerFlow(10, 1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK12", 2)));

        assertEqualsPowerFlow(new PowerFlow(-10, -1), extractSvPowerFlow(sv, cgmesTerminal(n, "LINE", 1)));
        assertEqualsPowerFlow(new PowerFlow(10.01, 1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "LINE", 2)));

        assertEqualsPowerFlow(new PowerFlow(-10.01, -1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "GEN", 1)));
        assertEqualsPowerFlow(new PowerFlow(10.01, 1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK21", 1)));
        assertEqualsPowerFlow(new PowerFlow(-10.01, -1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK21", 2)));
        assertEqualsPowerFlow(new PowerFlow(10.01, 1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK22", 1)));
        assertEqualsPowerFlow(new PowerFlow(-10.01, -1.1), extractSvPowerFlow(sv, cgmesTerminal(n, "BK22", 2)));
    }

    private static void assertEqualsPowerFlow(PowerFlow expected, PowerFlow actual) {
        final double epsilon = 1e-2;
        assertEquals(expected.p(), actual.p(), epsilon);
        assertEquals(expected.q(), actual.q(), epsilon);
    }

    private static String cgmesTerminal(Network n, String id, int terminal) {
        return n.getIdentifiable(id)
                .getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL + terminal)
                .orElseThrow();
    }

    private static PowerFlow extractSvPowerFlow(String sv, String terminalId) {
        Pattern p = Pattern.compile(
                "<cim:SvPowerFlow.p>([\\d.\\-]*)</cim:SvPowerFlow.p>\\s*.\\s*" +
                "<cim:SvPowerFlow.q>([\\d.\\-]*)</cim:SvPowerFlow.q>\\s*.\\s*" +
                "<cim:SvPowerFlow.Terminal.rdf.resource..#_" + terminalId,
                Pattern.DOTALL);
        Matcher m = p.matcher(sv);
        assertTrue(m.find());
        return new PowerFlow(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
    }

    @Test
    void microGridAssembled() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource(),
                false,
                4,
                false,
            r -> {
                addRepackagerFiles("NL", r);
                addRepackagerFiles("BE", r);
            }));
    }

    @Test
    void smallGridBusBranch() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallBusBranch().dataSource(),
                false,
                4,
                false,
                StateVariablesExportTest::addRepackagerFiles));
    }

    @Test
    void smallGridNodeBreakerHVDC() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreakerHvdc().dataSource(),
                true,
                4,
                false,
                StateVariablesExportTest::addRepackagerFilesExcludeTp));
    }

    @Test
    void smallGridNodeBreaker() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1Catalog.smallNodeBreaker().dataSource(),
                true,
                4,
                false,
                StateVariablesExportTest::addRepackagerFilesExcludeTp));
    }

    @Test
    void miniBusBranchWithSvInjection() throws IOException, XMLStreamException {
        assertTrue(test(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource(),
                false,
                4,
                false,
                StateVariablesExportTest::addRepackagerFiles));
    }

    @Test
    void miniBusBranchWithSvInjectionExportPQ() throws XMLStreamException {

        Network network = importNetwork(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource());
        String loadId = "0448d86a-c766-11e1-8775-005056c00008";
        Load load = network.getLoad(loadId);
        String cgmesTerminal = getCgmesTerminal(load.getTerminal());

        // Only when P and Q are NaN is not exported

        load.getTerminal().setP(-0.12);
        load.getTerminal().setQ(-13.03);
        String sv = exportSvAsString(network, 4);
        assertTrue(sv.contains(cgmesTerminal));

        load.getTerminal().setP(Double.NaN);
        load.getTerminal().setQ(-13.03);
        String sv1 = exportSvAsString(network, 4);
        assertTrue(sv1.contains(cgmesTerminal));

        load.getTerminal().setP(-0.12);
        load.getTerminal().setQ(Double.NaN);
        String sv2 = exportSvAsString(network, 4);
        assertTrue(sv2.contains(cgmesTerminal));
    }

    @Test
    void miniBusBranchWithSvInjectionExportQ() throws XMLStreamException {

        Network network = importNetwork(CgmesConformity1ModifiedCatalog.smallBusBranchWithSvInjection().dataSource());
        String shuntCompensatorId = "04553478-c766-11e1-8775-005056c00008";
        ShuntCompensator shuntCompensator = network.getShuntCompensator(shuntCompensatorId);
        String cgmesTerminal = getCgmesTerminal(shuntCompensator.getTerminal());

        // If P and Q both are NaN is not exported

        shuntCompensator.getTerminal().setQ(-13.03);
        String sv = exportSvAsString(network, 4);
        assertTrue(sv.contains(cgmesTerminal));
    }

    @Test
    void microGridBEWithHiddenTapChangers() throws XMLStreamException {
        Network network = importNetwork(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEHiddenTapChangers().dataSource());
        String sv = exportSvAsString(network, 2);
        String hiddenTapChangerId = "_6ebbef67-3061-4236-a6fd-6ccc4595f6c3-x";
        assertTrue(sv.contains(hiddenTapChangerId));
    }

    @Test
    void equivalentShuntTest() throws XMLStreamException {
        ReadOnlyDataSource ds = CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentShunt().dataSource();
        Network network = new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);

        String sv = exportSvAsString(network, 2);

        String equivalentShuntId = "d771118f-36e9-4115-a128-cc3d9ce3e3da";
        assertNotNull(network.getShuntCompensator(equivalentShuntId));
        SvShuntCompensatorSections svShuntCompensatorSections = readSvShuntCompensatorSections(sv);
        assertFalse(svShuntCompensatorSections.map.isEmpty());
        assertFalse(svShuntCompensatorSections.map.containsKey(equivalentShuntId));
    }

    private static SvShuntCompensatorSections readSvShuntCompensatorSections(String sv) {
        final String svShuntCompensatorSections = "SvShuntCompensatorSections";
        final String svShuntCompensatorSectionsSections = "SvShuntCompensatorSections.sections";
        final String svShuntCompensatorSectionsShuntCompensator = "SvShuntCompensatorSections.ShuntCompensator";
        final String attrResource = "resource";

        SvShuntCompensatorSections svdata = new SvShuntCompensatorSections();
        try (InputStream is = new ByteArrayInputStream(sv.getBytes(StandardCharsets.UTF_8))) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            Integer sections = null;
            String shuntCompensatorId = null;
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(svShuntCompensatorSections)) {
                        sections = null;
                        shuntCompensatorId = null;
                    } else if (reader.getLocalName().equals(svShuntCompensatorSectionsSections)) {
                        String text = reader.getElementText();
                        sections = Integer.parseInt(text);
                    } else if (reader.getLocalName().equals(svShuntCompensatorSectionsShuntCompensator)) {
                        shuntCompensatorId = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, attrResource).substring(2);
                    }
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getLocalName().equals(svShuntCompensatorSections) && sections != null) {
                        svdata.add(shuntCompensatorId, sections);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return svdata;
    }

    private static final class SvShuntCompensatorSections {
        private final Map<String, Integer> map = new HashMap<>();

        void add(String shuntCompensatorId, int sections) {
            map.put(shuntCompensatorId, sections);
        }
    }

    @Test
    void cgmes3MiniGridwithTransformersWithRtcAndPtc() throws XMLStreamException {
        Network network = ConversionUtil.networkModel(Cgmes3Catalog.miniGrid(), new Conversion.Config());

        // Add a PTC
        TwoWindingsTransformer t2wt = network.getTwoWindingsTransformer("813365c3-5be7-4ef0-a0a7-abd1ae6dc174");
        t2wt.newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(0)
                .beginStep()
                .setR(0.0)
                .setX(0.0)
                .setB(0)
                .setG(0)
                .setRho(1.0)
                .setAlpha(20)
                .endStep()
                .add();
        // We have added a PTC, we add a known alias for it
        // This is not mandatory, but it simplifies comparing the data in the network and the exported SV file
        // If we do not add it in advance, a proper CGMES ID for the new PTC would have been generated during export
        String t2ptcId = "ptc2w";
        t2wt.addAlias(t2ptcId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1);

        // Also, change the RTC tap position and add a PTC to a three-winding transformer
        ThreeWindingsTransformer t3wt = network.getThreeWindingsTransformer("411b5401-0a43-404a-acb4-05c3d7d0c95c");
        t3wt.getLeg1().getRatioTapChanger()
                .setTapPosition(16);
        t3wt.getLeg1().newPhaseTapChanger()
                .setLowTapPosition(0)
                .setTapPosition(1)
                .beginStep()
                .setR(0.0)
                .setX(0.0)
                .setB(0)
                .setG(0)
                .setRho(1.0)
                .setAlpha(10)
                .endStep()
                .beginStep()
                .setR(0.0)
                .setX(0.0)
                .setB(0)
                .setG(0)
                .setRho(1.0)
                .setAlpha(20)
                .endStep()
                .add();
        String t3ptcId = "ptc3w";
        t3wt.addAlias(t3ptcId, Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.PHASE_TAP_CHANGER + 1);

        String expected = buildNetworkSvTapStepsString(network);
        String sv = exportSvAsString(network, 2);
        String actual = readSvTapSteps(sv).toSortedString();
        assertEquals(expected, actual);
    }

    @Test
    void testTopologicalIslandSolvedNodeBreaker() {
        Network network = Network.read(CgmesConformity3Catalog.microGridBaseCaseNL().dataSource());

        Path outputPath = fileSystem.getPath("tmp-grid");
        Path outputSv = fileSystem.getPath("tmp-grid_SV.xml");
        Properties parameters = new Properties();
        parameters.setProperty(CgmesExport.EXPORT_LOAD_FLOW_STATUS, "true");

        setReferenceTerminalsFromReferencePriority(network);

        network.write("CGMES", parameters, outputPath);
        assertEquals("converged", readFirstTopologicalIslandDescription(outputSv));
    }

    @Test
    void testTopologicalIslandSolvedBusBranch() {
        Network network = Network.read(CgmesConformity1Catalog.miniBusBranch().dataSource());
        new LoadFlowResultsCompletion().run(network, null);

        Path outputPath = fileSystem.getPath("tmp-grid");
        Path outputSv = fileSystem.getPath("tmp-grid_SV.xml");
        Properties parameters = new Properties();
        parameters.setProperty(CgmesExport.EXPORT_LOAD_FLOW_STATUS, "true");

        setReferenceTerminalsFromReferencePriority(network);

        network.write("CGMES", parameters, outputPath);
        assertEquals("converged", readFirstTopologicalIslandDescription(outputSv));

        parameters.setProperty(CgmesExport.MAX_P_MISMATCH_CONVERGED, "0.000001");
        network.write("CGMES", parameters, outputPath);
        assertEquals("diverged", readFirstTopologicalIslandDescription(outputSv));
    }

    @Test
    void testDisconnectedGeneratorWithReferenceTerminal() {
        // Create a small network
        Network network = Network.create("network", "iidm");
        Substation s = network.newSubstation().setId("S").add();
        VoltageLevel vl = s.newVoltageLevel().setId("VL").setNominalV(400.0).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("B").add().setV(400).setAngle(0);
        vl.newLoad().setId("L").setConnectableBus("B").setBus("B").setP0(100.0).setQ0(0.0).add();
        Generator g = vl.newGenerator().setId("G").setBus("B").setMaxP(100.0).setMinP(50.0).setTargetP(100.0).setTargetV(400.0).setVoltageRegulatorOn(true).add();

        // Set reference terminal
        ReferenceTerminals.addTerminal(g.getTerminal());

        // Disconnect the generator
        Terminal t = g.getTerminal();
        assertTrue(t.disconnect());
        assertFalse(t.isConnected());
        assertNull(t.getBusView().getBus());

        // Verify in the output file that no TopologicalIsland was created
        Path outputPath = fileSystem.getPath("tmp-referenceTerminal");
        Path outputSv = fileSystem.getPath("tmp-referenceTerminal_SV.xml");
        network.write("CGMES", new Properties(), outputPath);
        assertEquals("", readFirstTopologicalIslandDescription(outputSv));
    }

    private static String readFirstTopologicalIslandDescription(Path sv) {
        String description = "";
        boolean insideTopologicalIsland = false;
        try (InputStream is = Files.newInputStream(sv)) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            while (reader.hasNext()) {
                int token = reader.next();
                if (token == XMLStreamConstants.START_ELEMENT) {
                    // Retrieve the TopologicalIsland node
                    if (reader.getLocalName().equals(CgmesNames.TOPOLOGICAL_ISLAND)) {
                        insideTopologicalIsland = true;
                    }
                    if (insideTopologicalIsland && reader.getLocalName().equals(CgmesNames.IDENTIFIED_OBJECT_DESCRIPTION)) {
                        description = reader.getElementText();
                    }
                } else if (token == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals(CgmesNames.TOPOLOGICAL_ISLAND)) {
                    break;
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return description;
    }

    private static void setReferenceTerminalsFromReferencePriority(Network network) {
        // Assume all terminals with reference priority > 0 are set as angle reference terminals by the load flow
        ReferencePriorities.get(network)
                .stream()
                .filter(p -> p.getPriority() > 0)
                .forEach(p -> ReferenceTerminals.addTerminal(p.getTerminal()));
    }

    private static String buildNetworkSvTapStepsString(Network network) {
        SvTapSteps svTapSteps = new SvTapSteps();
        network.getTwoWindingsTransformers().forEach(twt -> {
            twt.getOptionalRatioTapChanger().ifPresent(rtc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER), rtc.getTapPosition()));
            twt.getOptionalPhaseTapChanger().ifPresent(ptc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER), ptc.getTapPosition()));
        });
        network.getThreeWindingsTransformers().forEach(twt -> {
            twt.getLeg1().getOptionalRatioTapChanger().ifPresent(rtc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 1), rtc.getTapPosition()));
            twt.getLeg1().getOptionalPhaseTapChanger().ifPresent(ptc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 1), ptc.getTapPosition()));
            twt.getLeg2().getOptionalRatioTapChanger().ifPresent(rtc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 2), rtc.getTapPosition()));
            twt.getLeg2().getOptionalPhaseTapChanger().ifPresent(ptc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 2), ptc.getTapPosition()));
            twt.getLeg3().getOptionalRatioTapChanger().ifPresent(rtc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.RATIO_TAP_CHANGER, 3), rtc.getTapPosition()));
            twt.getLeg3().getOptionalPhaseTapChanger().ifPresent(ptc -> svTapSteps.add(getTapChangerId(twt, CgmesNames.PHASE_TAP_CHANGER, 3), ptc.getTapPosition()));
        });
        return svTapSteps.toSortedString();
    }

    private static String getTapChangerId(TwoWindingsTransformer twt, String baseAliasType) {
        // For two winding transformers the CGMES tap changer id may be stored with suffix 1 or 2,
        // depending on its original location in CGMES model
        String aliasType1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + baseAliasType + 1;
        String aliasType2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + baseAliasType + 2;
        return twt.getAliasFromType(aliasType1)
                .or(() -> twt.getAliasFromType(aliasType2))
                .orElseThrow(() -> new PowsyblException("Missing alias " + aliasType1 + " or " + aliasType2));
    }

    private static String getTapChangerId(ThreeWindingsTransformer twt, String baseAliasType, int leg) {
        // For three winding transformers, the tap changer id has to be stored in the alias number corresponding to the leg
        String aliasType = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + baseAliasType + leg;
        return twt.getAliasFromType(aliasType)
                .orElseThrow(() -> new PowsyblException("Missing alias " + aliasType));
    }

    private static SvTapSteps readSvTapSteps(String sv) {
        final String svTapStep = "SvTapStep";
        final String svTapStepPosition = "SvTapStep.position";
        final String svTapStepTapChanger = "SvTapStep.TapChanger";
        final String attrResource = "resource";

        SvTapSteps svTapSteps = new SvTapSteps();
        try (InputStream is = new ByteArrayInputStream(sv.getBytes(StandardCharsets.UTF_8))) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);
            Integer position = null;
            String tapChangerId = null;
            while (reader.hasNext()) {
                int next = reader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equals(svTapStep)) {
                        position = null;
                        tapChangerId = null;
                    } else if (reader.getLocalName().equals(svTapStepPosition)) {
                        String text = reader.getElementText();
                        position = Integer.parseInt(text);
                    } else if (reader.getLocalName().equals(svTapStepTapChanger)) {
                        tapChangerId = reader.getAttributeValue(CgmesNamespace.RDF_NAMESPACE, attrResource).substring(2);
                    }
                } else if (next == XMLStreamConstants.END_ELEMENT) {
                    if (reader.getLocalName().equals(svTapStep) && position != null) {
                        svTapSteps.add(tapChangerId, position);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
        return svTapSteps;
    }

    private static final class SvTapSteps {
        private final Map<String, Integer> svTapSteps = new HashMap<>();

        void add(String tapChangerId, int position) {
            svTapSteps.put(tapChangerId, position);
        }

        String toSortedString() {
            return svTapSteps.entrySet().stream()
                    .map(e -> String.format("%50s %05d", e.getKey(), e.getValue()))
                    .sorted()
                    .collect(Collectors.joining("\n"));
        }
    }

    private static Network importNetwork(ReadOnlyDataSource ds) {
        Properties importParams = new Properties();
        importParams.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        return new CgmesImport().importData(ds, NetworkFactory.findDefault(), importParams);
    }

    private String exportSvAsString(Network network, int svVersion) throws XMLStreamException {
        return exportSvAsString(network, svVersion, false);
    }

    private String exportSvAsString(Network network, int svVersion, boolean exportFlowsForSwitches) throws XMLStreamException {
        CgmesExportContext context = new CgmesExportContext(network);
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", stringWriter);
        context.getExportedSVModel().setVersion(svVersion);
        context.setExportBoundaryPowerFlows(true);
        context.setExportFlowsForSwitches(exportFlowsForSwitches);
        StateVariablesExport.write(network, writer, context);

        return stringWriter.toString();
    }

    private static String getCgmesTerminal(Terminal terminal) {
        return ((Connectable<?>) terminal.getConnectable()).getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1).orElse(null);
    }

    private static void addRepackagerFiles(String tso, Repackager repackager) {
        repackager.with("test_" + tso + "_EQ.xml", name -> name.contains(tso) && name.contains("EQ"))
                .with("test_" + tso + "_TP.xml", name -> name.contains(tso) && name.contains("TP"))
                .with("test_" + tso + "_SSH.xml", name -> name.contains(tso) && name.contains("SSH"));
    }

    private static void addRepackagerFiles(Repackager repackager) {
        repackager.with("test_EQ.xml", Repackager::eq)
                .with("test_TP.xml", Repackager::tp)
                .with("test_SSH.xml", Repackager::ssh);
    }

    private static void addRepackagerFilesExcludeTp(Repackager repackager) {
        repackager.with("test_EQ.xml", Repackager::eq)
                .with("test_SSH.xml", Repackager::ssh);
    }

    private boolean test(ReadOnlyDataSource dataSource, boolean exportTp, int svVersion, boolean exportFlowsForSwitches, Consumer<Repackager> repackagerConsumer) throws XMLStreamException, IOException {
        // Import original
        importParams.put("iidm.import.cgmes.create-cgmes-export-mapping", "true");
        Network expected0 = new CgmesImport().importData(dataSource, NetworkFactory.findDefault(), importParams);

        // Ensure all information in IIDM mapping extensions is created
        // Some mappings are not built until export is requested
        new CgmesExportContext().addIidmMappings(expected0);

        // Export to XIIDM and re-import to test serialization of CGMES-IIDM extension
        NetworkSerDe.write(expected0, tmpDir.resolve("temp.xiidm"));
        Network expected = NetworkSerDe.read(tmpDir.resolve("temp.xiidm"));

        // Export SV
        CgmesExportContext context = new CgmesExportContext(expected);
        context.getExportedSVModel().setVersion(svVersion);
        context.setExportBoundaryPowerFlows(true);
        context.setExportFlowsForSwitches(exportFlowsForSwitches);
        context.setExportSvInjectionsForSlacks(false);
        Path exportedSv = tmpDir.resolve("exportedSv.xml");
        try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedSv))) {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
            StateVariablesExport.write(expected, writer, context);
        }
        // Export TP if required (node/breaker models require an export of TP in addition to SV file)
        Path exportedTp = tmpDir.resolve("exportedTp.xml");
        if (exportTp) {
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(exportedTp))) {
                XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", os);
                TopologyExport.write(expected, writer, context);
            }
        }

        // Zip with new SV (and eventually a new TP)
        Path repackaged = tmpDir.resolve("repackaged.zip");
        Repackager r = new Repackager(dataSource)
                .with("test_SV.xml", exportedSv)
                .with("test_EQ_BD.xml", Repackager::eqBd)
                .with("test_TP_BD.xml", Repackager::tpBd);
        if (exportTp) {
            r.with("test_TP.xml", exportedTp);
        }
        repackagerConsumer.accept(r);
        r.zip(repackaged);

        // Import with new SV
        Network actual = Network.read(repackaged,
                DefaultComputationManagerConfig.load().createShortTimeExecutionComputationManager(), ImportConfig.load(), importParams);

        // Before comparison, set undefined p/q in expected network at 0.0
        expected.getConnectableStream()
                .filter(c -> !(c instanceof BusbarSection))
                .filter(c -> !(c instanceof HvdcConverterStation))
                .flatMap(c -> (Stream<Terminal>) c.getTerminals().stream())
                .filter(t -> Double.isNaN(t.getP()) && Double.isNaN(t.getQ()))
                .forEach(t -> t.setP(0.0).setQ(0.0));

        // Export original and with new SV
        // comparison without extensions, only Networks
        ExportOptions exportOptions = new ExportOptions().setSorted(true);
        exportOptions.setExtensions(Collections.emptySet());
        Path expectedPath = tmpDir.resolve("expected.xml");
        Path actualPath = tmpDir.resolve("actual.xml");
        NetworkSerDe.write(expected, exportOptions, expectedPath);
        NetworkSerDe.write(actual, exportOptions, actualPath);
        NetworkSerDe.validate(actualPath);

        // Compare
        return ExportXmlCompare.compareNetworks(expectedPath, actualPath);
    }
}
