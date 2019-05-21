/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.*;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.DanglingLineNetworkFactory;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.ucte.network.*;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest {

    private static Network transfomerRegulationNetwork;
    private static Network exportTestNetwork;
    private static Network iidmNetwork;
    private static Network iidmTieLineNetwork;
    private static Network iidmSwitchNetwork;
    private UcteExporter ucteExporter = new UcteExporter();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("transformerRegulation", new ResourceSet("/", "transformerRegulation.uct"));
        transfomerRegulationNetwork = new UcteImporter().importData(dataSource, null);
        dataSource = new ResourceDataSource("exportTest", new ResourceSet("/", "exportTest.uct"));
        exportTestNetwork = new UcteImporter().importData(dataSource, null);
        iidmNetwork = EurostagTutorialExample1Factory.create();
        iidmNetwork.getLine("NHV1_NHV2_1").getProperties().setProperty(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY, "geographicalName");
        createTieLineNetwork();
        createNetworkWithSwitch();
    }

    @Test
    public void exportUcteTest() throws IOException {
        MemDataSource exportedDataSource = new MemDataSource();
        new UcteExporter().export(exportTestNetwork, null, exportedDataSource);
        try (Reader exportedData = new InputStreamReader(new ByteArrayInputStream(exportedDataSource.getData(null, "uct")))) {
            Reader expectedData = new InputStreamReader(
                    new FileInputStream(FileSystems.getDefault().getPath("./src/test/resources/expectedExport.uct").toFile()));
            assertTrue(IOUtils.contentEqualsIgnoreEOL(expectedData, exportedData));
        }

        exception.expect(IllegalArgumentException.class);
        new UcteExporter().export(null, null, null);
    }

    @Test
    public void createLineFromNonCompliantIdDanglingLineTest() {
        Network networkWithDL = DanglingLineNetworkFactory.create();
        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        //no ucteXnodeCode
        ucteExporter.createLineFromNonCompliantIdDanglingLine(ucteNetwork, networkWithDL.getDanglingLine("DL"));
        assertEquals(0, ucteNetwork.getLines().size());
        //with ucteXnodeCode
        // ucteExporter.createLineFromNonCompliantIdDanglingLine(ucteNetwork, networkWithDL.getDanglingLine("DL")); //todo : finish test
        networkWithDL.getDanglingLine("DL").getUcteXnodeCode();

    }

    @Test
    public void generateUcteNodeCodeTest() {
        ucteExporter.generateUcteNodeCode("test", iidmNetwork.getVoltageLevel("VLHV1"), UcteCountryCode.FR.toString());
        assertEquals("FP1aaa1b", ucteExporter.iidmIdToUcteNodeCodeId.get("test").toString());
        ucteExporter.generateUcteNodeCode("test", iidmNetwork.getVoltageLevel("VLHV1"), UcteCountryCode.FR.toString());
        assertEquals("FP1aaa1c", ucteExporter.iidmIdToUcteNodeCodeId.get("test").toString());
        for (int i = 0; i < 150; i++) {
            ucteExporter.generateUcteNodeCode("test", iidmNetwork.getVoltageLevel("VLHV1"), UcteCountryCode.FR.toString());
        }
        ucteExporter.generateUcteNodeCode("test", iidmNetwork.getVoltageLevel("VLHV1"), UcteCountryCode.FR.toString());
        assertEquals("FP1aac1F", ucteExporter.iidmIdToUcteNodeCodeId.get("test").toString());
    }

    @Test
    public void incrementGeneratedGeographicalNameTest() {
        assertEquals("aaab", ucteExporter.incrementGeneratedGeographicalName("aaaa"));
        assertEquals("aaaA", ucteExporter.incrementGeneratedGeographicalName("aaaz"));
        assertEquals("aaba", ucteExporter.incrementGeneratedGeographicalName("aaa9"));
        assertEquals("tryc", ucteExporter.incrementGeneratedGeographicalName("trybr"));
        assertEquals("zaaa", ucteExporter.incrementGeneratedGeographicalName("y999"));
        assertEquals("zfaa", ucteExporter.incrementGeneratedGeographicalName("ze99"));
        try {
            ucteExporter.incrementGeneratedGeographicalName("aze");
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals("The string to increment is not long enough (should be at least 4)", exception.getMessage());
        }

    }

    @Test
    public void energySourceToUctePowerPlantTypeTest() {
        assertSame(UctePowerPlantType.H, ucteExporter.energySourceToUctePowerPlantType(EnergySource.HYDRO));
        assertSame(UctePowerPlantType.N, ucteExporter.energySourceToUctePowerPlantType(EnergySource.NUCLEAR));
        assertSame(UctePowerPlantType.C, ucteExporter.energySourceToUctePowerPlantType(EnergySource.THERMAL));
        assertSame(UctePowerPlantType.W, ucteExporter.energySourceToUctePowerPlantType(EnergySource.WIND));
        assertSame(UctePowerPlantType.F, ucteExporter.energySourceToUctePowerPlantType(EnergySource.OTHER));
        assertNotSame(UctePowerPlantType.W, ucteExporter.energySourceToUctePowerPlantType(EnergySource.THERMAL));
        assertNotSame(UctePowerPlantType.H, ucteExporter.energySourceToUctePowerPlantType(EnergySource.NUCLEAR));
    }

    @Test
    public void createUcteNodeCodeTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("countryIssue", new ResourceSet("/", "countryIssue.uct"));
        Network network = new UcteImporter().importData(dataSource, null);
        UcteExporter ucteExporter = new UcteExporter();
        assertEquals(new UcteNodeCode(UcteCountryCode.ES, "HORTA", UcteVoltageLevelCode.VL_220, '1'), ucteExporter.createUcteNodeCode("EHORTA21", network.getVoltageLevel("EHORTA2"), "ES"));
        assertNotEquals(new UcteNodeCode(UcteCountryCode.ES, "HORTA", UcteVoltageLevelCode.VL_110, '1'), ucteExporter.createUcteNodeCode("EHORTA21", network.getVoltageLevel("EHORTA2"), "ES"));
        assertNotEquals(new UcteNodeCode(UcteCountryCode.ES, "HORT", UcteVoltageLevelCode.VL_220, '1'), ucteExporter.createUcteNodeCode("EHORTA21", network.getVoltageLevel("EHORTA2"), "ES"));
        assertNotEquals(new UcteNodeCode(UcteCountryCode.BE, "HORTA", UcteVoltageLevelCode.VL_220, '1'), ucteExporter.createUcteNodeCode("EHORTA21", network.getVoltageLevel("EHORTA2"), "ES"));

    }

    @Test
    public void calculatePhaseDuTest() {
        assertEquals(
                2.0000,
                ucteExporter.calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.00001);
        assertNotEquals(
                2.0001,
                ucteExporter.calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.00001);
    }

    @Test
    public void convertUcteElementIdTest() {
        TwoWindingsTransformer twoWindingsTransformer = transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1");

        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        UcteNodeCode ucteNodeCode1 = new UcteNodeCode(UcteCountryCode.ME, "BBBBB", UcteVoltageLevelCode.VL_110, ' ');
        UcteNodeCode ucteNodeCode2 = new UcteNodeCode(UcteCountryCode.ME, "AAAAA", UcteVoltageLevelCode.VL_220, ' ');

        UcteElementId ucteElementId1 = new UcteElementId(ucteNodeCode1, ucteNodeCode2, '1');
        UcteElementId ucteElementId2 = new UcteElementId(ucteNodeCode2, ucteNodeCode1, '1');

        assertEquals(ucteElementId1, ucteExporter.convertUcteElementId(ucteNodeCode1, ucteNodeCode2, twoWindingsTransformer.getId(), terminal1, terminal2));
        assertNotEquals(ucteElementId2, ucteExporter.convertUcteElementId(ucteNodeCode1, ucteNodeCode2, twoWindingsTransformer.getId(), terminal1, terminal2));
    }

    @Test
    public void isUcteTieLineIdTest() {
        Line line = exportTestNetwork.getLine("XB__F_21 B_SU1_21 1 + XB__F_21 F_SU1_21 1");
        MergedXnode mergedXnode =
                new MergedXnode(line, 0f, 0f, 0d, 0d, 0d, 0d, "");
        line.addExtension(MergedXnode.class, mergedXnode);
        assertFalse(ucteExporter.isUcteTieLineId(line));
        line.getExtension(MergedXnode.class).setLine1Name("tooShort");
        line.getExtension(MergedXnode.class).setLine2Name("B_SU1_21 1 F_SU1_21 1");
        assertFalse(ucteExporter.isUcteTieLineId(line));
        line.getExtension(MergedXnode.class).setLine1Name("notUcteCompliantId1111111111111");
        assertFalse(ucteExporter.isUcteTieLineId(line));
        line.getExtension(MergedXnode.class).setLine1Name("B_SU1_21 1 F_SU1_21 2");
        assertTrue(ucteExporter.isUcteTieLineId(line));
    }

    @Test
    public void generateUcteElementIdTest() {
        UcteNodeCode ucteNodeCode1 = new UcteNodeCode(UcteCountryCode.AL, "geographicalTest", UcteVoltageLevelCode.VL_110, '1');
        UcteNodeCode ucteNodeCode2 = new UcteNodeCode(UcteCountryCode.AL, "geographical2Test", UcteVoltageLevelCode.VL_150, '2');
        ucteExporter.generateUcteElementId("testId", ucteNodeCode1, ucteNodeCode2, null);
        assertEquals("AgeographicalTest51 Ageographical2Test32 a", ucteExporter.generateUcteElementId("testId", ucteNodeCode1, ucteNodeCode2, null).toString());
        assertEquals(ucteExporter.iidmIdToUcteElementId.get("testId"), ucteExporter.generateUcteElementId("testId", ucteNodeCode1, ucteNodeCode2, null));
        assertEquals("AgeographicalTest51 Ageographical2Test32 a", ucteExporter.generateUcteElementId("testId", ucteNodeCode1, ucteNodeCode2, null).toString());
        ucteExporter.generateUcteElementId("testId2", ucteNodeCode1, ucteNodeCode2, null);
        assertEquals("AgeographicalTest51 Ageographical2Test32 b", ucteExporter.generateUcteElementId("testId2", ucteNodeCode1, ucteNodeCode2, null).toString());
        assertEquals(ucteExporter.iidmIdToUcteElementId.get("testId"), ucteExporter.generateUcteElementId("testId", ucteNodeCode1, ucteNodeCode2, null));
        assertEquals("AgeographicalTest51 Ageographical2Test32 b", ucteExporter.generateUcteElementId("testId2", ucteNodeCode1, ucteNodeCode2, null).toString());

    }

    @Test
    public void convertIidmIdToUcteNodeCodeTest() {
        VoltageLevel voltageLevel = iidmNetwork.getVoltageLevel("VLHV1");
        String country = "FR";
        ucteExporter.convertIidmIdToUcteNodeCode("idtest", voltageLevel, country);
        assertEquals("FVLHV11a", ucteExporter.iidmIdToUcteNodeCodeId.get("idtest").toString());
        assertEquals(ucteExporter.iidmIdToUcteNodeCodeId.get("idtest"), ucteExporter.iidmIdToUcteNodeCodeId.get("idtest"));
        ucteExporter.convertIidmIdToUcteNodeCode("idtest2", voltageLevel, country);
        assertEquals("FVLHV11b", ucteExporter.iidmIdToUcteNodeCodeId.get("idtest2").toString());
        assertEquals(ucteExporter.iidmIdToUcteNodeCodeId.get("idtest2"), ucteExporter.iidmIdToUcteNodeCodeId.get("idtest2"));

    }

    @Test
    public void createTieLineWithGeneratedIdsTest() {
        UcteNetworkImpl ucteNetwork = new UcteNetworkImpl();
        assertEquals(0, ucteNetwork.getLines().size());
        ucteExporter.createTieLineWithGeneratedIds(ucteNetwork, iidmTieLineNetwork.getLine("l1 + l2"));
        assertEquals(2, ucteNetwork.getLines().size());
        Collection<UcteLine> ucteLines = ucteNetwork.getLines();
        assertEquals("Xvl1  1a Fvl1  1a a", ucteLines.toArray()[0].toString());
        assertEquals("Xvl1  1a Bvl2  1a a", ucteLines.toArray()[1].toString());
    }

    @Test
    public void convertSwitchTest() {
        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        assertEquals(0, ucteNetwork.getLines().size());
        ucteExporter.convertSwitches(ucteNetwork, iidmSwitchNetwork.getVoltageLevel("VL1"));
        assertEquals(1, ucteNetwork.getLines().size());
        assertEquals("EVL1  1a EVL1  1b a", ucteNetwork.getLines().toArray()[0].toString());
        ucteExporter.convertSwitches(ucteNetwork, iidmSwitchNetwork.getVoltageLevel("VL2"));
        assertEquals(2, ucteNetwork.getLines().size());
        assertEquals("FVL2  1a FVL2  1b a", ucteNetwork.getLines().toArray()[1].toString());
    }

    @Test
    public void getGeographicalNamePropertyTest() {
        assertEquals("geographicalName", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_1")));
        assertNotEquals("wrong", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_1")));
        assertEquals("", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_2")));
        assertNotEquals("false", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_2")));
    }

    @Test
    public void getFormatTest() {
        assertEquals("UCTE", ucteExporter.getFormat());
        assertNotEquals("IIDM", ucteExporter.getFormat());
    }

    @Test
    public void getCommentTest() {
        assertEquals("IIDM to UCTE converter", ucteExporter.getComment());
        assertNotEquals("UCTE to IIDM converter", ucteExporter.getComment());
    }

    private static void createTieLineNetwork() {
        iidmTieLineNetwork = NetworkFactory.create("iidmTieLineNetwork", "test");
        Substation s1 = iidmTieLineNetwork.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Bus b1 = vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();
        vl1.newGenerator()
                .setId("g1")
                .setBus("b1")
                .setConnectableBus("b1")
                .setTargetP(100.0)
                .setTargetV(400.0)
                .setVoltageRegulatorOn(true)
                .setMinP(50.0)
                .setMaxP(150.0)
                .add();
        Substation s2 = iidmTieLineNetwork.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
        vl2.newLoad()
                .setId("ld1")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .add();
        iidmTieLineNetwork.newTieLine()
                .setId("l1 + l2")
                .setVoltageLevel1("vl1")
                .setConnectableBus1("b1")
                .setBus1("b1")
                .setVoltageLevel2("vl2")
                .setConnectableBus2("b2")
                .setBus2("b2")
                .line1()
                .setId("l1")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .setXnodeP(0.0)
                .setXnodeQ(0.0)
                .line2()
                .setId("l2")
                .setR(1.0)
                .setX(1.0)
                .setG1(0.0)
                .setG2(0.0)
                .setB1(0.0)
                .setB2(0.0)
                .setXnodeP(0.0)
                .setXnodeQ(0.0)
                .setUcteXnodeCode("XNODE")
                .add().addExtension(MergedXnode.class,
                new MergedXnode(iidmTieLineNetwork.getLine("l1 + l2"), 1, 1, 1, 0, 0, 0,
                        "l1", "l2", "testXnode"));

        iidmTieLineNetwork.getLine("l1 + l2").newCurrentLimits1()
                 .setPermanentLimit(100)
                 .beginTemporaryLimit()
                 .setName("5'")
                 .setAcceptableDuration(5 * 60)
                 .setValue(1400)
                 .endTemporaryLimit()
                 .add();
    }

    private static void createNetworkWithSwitch() {
        // For the buses to be valid they have to be connected to at least one branch
        iidmSwitchNetwork = NetworkFactory.create("iidmSwitchNetwork", "test");
        Substation s1 = iidmSwitchNetwork.newSubstation()
                .setId("S1")
                .setCountry(Country.ES)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VL1")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        Substation s2 = iidmSwitchNetwork.newSubstation()
                .setId("S2")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("VL2")
                .setNominalV(400f)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1a")
                .add();
        vl1.newLoad()
                .setId("L1")
                .setBus("B1a")
                .setP0(1)
                .setQ0(0)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("B1b")
                .add();
        vl1.newGenerator()
                .setId("G1")
                .setBus("B1b")
                .setMinP(0)
                .setMaxP(1)
                .setTargetP(1)
                .setTargetQ(0)
                .setVoltageRegulatorOn(false)
                .add();
        vl1.getBusBreakerView().newSwitch()
                .setId("SW")
                .setOpen(false)
                .setBus1("B1a")
                .setBus2("B1b")
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2")
                .add();
        vl2.newLoad()
                .setId("L2")
                .setBus("B2")
                .setP0(1)
                .setQ0(0)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2a")
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("B2b")
                .add();
        vl2.getBusBreakerView().newSwitch()
                .setId("IdWithMoreThan18CharacterButStillNonUcteCompliant")
                .setOpen(false)
                .setBus1("B2a")
                .setBus2("B2b")
                .add();
    }
}
