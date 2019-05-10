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

import static org.junit.Assert.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest {

    private static Network transfomerRegulationNetwork;
    private static Network exportTestNetwork;
    private static Network iidmNetwork;
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
        iidmNetwork.getLine("NHV1_NHV2_1").getProperties().setProperty(UcteImporter.REFERENCE_VOLTAGE, "15.8");
        iidmNetwork.getLine("NHV1_NHV2_1").getProperties().setProperty(UcteImporter.GEOGRAPHICAL_NAME_PROPERTY_KEY, "geographicalName");
    }

    @Test
    public void exportUcteTest() throws IOException {
//        ReadOnlyDataSource dataSource = new ResourceDataSource("20100505_0330_FO3_UX3",
//                new ResourceSet("/", "20100505_0330_FO3_UX3.uct"));
//        Network network = new UcteImporter().importData(dataSource, null);

        addThreeWindingTransformers(exportTestNetwork);

//        FileDataSource fds = new FileDataSource(FileSystems.getDefault().getPath("./"), "test"); //TODO remove this when ready to merge
//        new UcteExporter().export(exportTestNetwork, null, fds); //TODO remove this when ready to merge
//        new UcteExporter().export(network, null, fds); //TODO remove this when ready to merge
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

    private void addThreeWindingTransformers(Network network) {
        network.getSubstation("F_SU1_").newThreeWindingsTransformer()
                .setId("3WT")
                .newLeg1()
                .setVoltageLevel("F_SU1_1")
                .setBus("F_SU1_11")
                .setR(17.424)
                .setX(1.7424)
                .setG(0.00573921028466483)
                .setB(0.000573921028466483)
                .setRatedU(132.0)
                .add()
                .newLeg2()
                .setVoltageLevel("F_SU1_2")
                .setBus("F_SU1_21")
                .setR(1.089)
                .setX(0.1089)
                .setRatedU(33.0)
                .add()
                .newLeg3()
                .setVoltageLevel("F_SU1_3")
                .setBus("F_SU1_31")
                .setR(0.121)
                .setX(0.0121)
                .setRatedU(11.0)
                .add()
                .add();

        network.getThreeWindingsTransformer("3WT").getLeg1().newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();

        network.getThreeWindingsTransformer("3WT").getLeg2().newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();

        network.getThreeWindingsTransformer("3WT").getLeg3().newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
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
        assertEquals("FP1aaf1x", ucteExporter.iidmIdToUcteNodeCodeId.get("test").toString());
    }

    @Test
    public void incrementGeneratedGeographicalNameTest() {
        assertEquals("aaab", ucteExporter.incrementGeneratedGeographicalName("aaaa"));
        assertEquals("aaba", ucteExporter.incrementGeneratedGeographicalName("aaaz"));
        assertEquals("tryc", ucteExporter.incrementGeneratedGeographicalName("trybr"));
        assertEquals("zaaa", ucteExporter.incrementGeneratedGeographicalName("yzzz"));
        assertEquals("zfaa", ucteExporter.incrementGeneratedGeographicalName("zezz"));
        try {
            ucteExporter.incrementGeneratedGeographicalName("aze");
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals("The string to increment is not long enough (should be at least 4)", exception.getMessage());
        }
        try {
            ucteExporter.incrementGeneratedGeographicalName("zzzz");
            fail();
        } catch (UcteException exception) {
            assertEquals("This network cannot be exported, too much identical ids", exception.getMessage());
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
    public void getGeographicalNamePropertyTest() {
        assertEquals("geographicalName", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_1")));
        assertNotEquals("wrong", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_1")));
        assertEquals("", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_2")));
        assertNotEquals("false", ucteExporter.getGeographicalNameProperty(iidmNetwork.getLine("NHV1_NHV2_2")));
    }

    @Test
    public void getReferenceVoltagePropertyTest() {
        assertEquals(15.8, ucteExporter.getReferenceVoltageProperty(iidmNetwork.getLine("NHV1_NHV2_1")), 0.01);
        assertNotEquals(16, ucteExporter.getReferenceVoltageProperty(iidmNetwork.getLine("NHV1_NHV2_1")), 0.01);
        assertEquals(Float.NaN, ucteExporter.getReferenceVoltageProperty(iidmNetwork.getLine("NHV1_NHV2_2")), 0.01);
        assertNotEquals(1, ucteExporter.getReferenceVoltageProperty(iidmNetwork.getLine("NHV1_NHV2_2")), 0.01);
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
}
