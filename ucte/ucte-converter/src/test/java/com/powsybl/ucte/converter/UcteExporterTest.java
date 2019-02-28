/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.ucte.network.*;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest {

    private static Network transfomerRegulationNetwork;
    private static UcteExporter ucteExporter = new UcteExporter();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUpBeforeClass() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("transformerRegulation", new ResourceSet("/", "transformerRegulation.uct"));
        transfomerRegulationNetwork = new UcteImporter().importData(dataSource, null);
    }

    @Test
    public void exportUcteTest() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));
        Network network = new UcteImporter().importData(dataSource, null);
        Path path = FileSystems.getDefault().getPath("./");
        FileDataSource fds = new FileDataSource(path, "test");
        new UcteExporter().export(network, null, fds);
        exception.expect(IllegalArgumentException.class);
        new UcteExporter().export(null, null, null);

    }

    @Test
    public void isUcteCountryCodeTest() {
        assertTrue(ucteExporter.isUcteCountryCode('A'));
        assertTrue(ucteExporter.isUcteCountryCode('1'));
        assertFalse(ucteExporter.isUcteCountryCode('_'));
        assertFalse(ucteExporter.isUcteCountryCode('&'));
    }

    @Test
    public void isVoltageLevelTest() {
        assertTrue(ucteExporter.isVoltageLevel('0'));
        assertTrue(ucteExporter.isVoltageLevel('9'));
        assertFalse(ucteExporter.isVoltageLevel('_'));
        assertFalse(ucteExporter.isVoltageLevel('&'));
    }

    @Test
    public void isUcteNodeId() {
        assertTrue(ucteExporter.isUcteNodeId("B_SU1_11"));
        assertTrue(ucteExporter.isUcteNodeId("B_SU1_1 "));
        assertTrue(ucteExporter.isUcteNodeId("7efG8411"));
        assertFalse(ucteExporter.isUcteNodeId("        "));
        assertFalse(ucteExporter.isUcteNodeId("B_SU1_"));
        assertFalse(ucteExporter.isUcteNodeId("&ezrt874g"));
    }

    @Test
    public void isUcteNodeTest() {
        assertTrue(ucteExporter.isUcteId("F_SU1_11 F_SU1_21 1"));
        assertTrue(ucteExporter.isUcteId("F_SU1_1& F_SU1_21 Z"));
        assertTrue(ucteExporter.isUcteId("Fazert11 F_SU1_21 1"));
        assertFalse(ucteExporter.isUcteId("F_SU1_11F_SU1_21 1"));
        assertFalse(ucteExporter.isUcteId("F_SU1_11 F_SU1_2"));
        assertFalse(ucteExporter.isUcteId("F_SU1_1 F_SU1_21 1"));
        assertFalse(ucteExporter.isUcteId("F_SU1_&1 F_SU1_21 1"));
        assertFalse(ucteExporter.isUcteId("F_SU1_11TF_SU1_21 1"));
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
    public void iidmVoltageToUcteVoltageLevelCode() {
        assertSame(UcteVoltageLevelCode.VL_27, ucteExporter.iidmVoltageToUcteVoltageLevelCode(27));
        assertSame(UcteVoltageLevelCode.VL_70, ucteExporter.iidmVoltageToUcteVoltageLevelCode(70));
        assertSame(UcteVoltageLevelCode.VL_110, ucteExporter.iidmVoltageToUcteVoltageLevelCode(110));
        assertSame(UcteVoltageLevelCode.VL_120, ucteExporter.iidmVoltageToUcteVoltageLevelCode(120));
        assertSame(UcteVoltageLevelCode.VL_150, ucteExporter.iidmVoltageToUcteVoltageLevelCode(150));
        assertSame(UcteVoltageLevelCode.VL_220, ucteExporter.iidmVoltageToUcteVoltageLevelCode(220));
        assertSame(UcteVoltageLevelCode.VL_330, ucteExporter.iidmVoltageToUcteVoltageLevelCode(330));
        assertSame(UcteVoltageLevelCode.VL_380, ucteExporter.iidmVoltageToUcteVoltageLevelCode(380));
        assertSame(UcteVoltageLevelCode.VL_500, ucteExporter.iidmVoltageToUcteVoltageLevelCode(500));
        assertSame(UcteVoltageLevelCode.VL_750, ucteExporter.iidmVoltageToUcteVoltageLevelCode(750));
        assertNotSame(UcteVoltageLevelCode.VL_27, ucteExporter.iidmVoltageToUcteVoltageLevelCode(330));
        exception.expect(IllegalArgumentException.class);
        assertSame(new IllegalArgumentException(), ucteExporter.iidmVoltageToUcteVoltageLevelCode(15));
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
    public void voltageLevelCodeFromCharTest() {
        assertSame(UcteVoltageLevelCode.VL_750, ucteExporter.voltageLevelCodeFromChar('0'));
        assertSame(UcteVoltageLevelCode.VL_500, ucteExporter.voltageLevelCodeFromChar('9'));
        assertSame(UcteVoltageLevelCode.VL_380, ucteExporter.voltageLevelCodeFromChar('1'));
        assertSame(UcteVoltageLevelCode.VL_330, ucteExporter.voltageLevelCodeFromChar('8'));
        assertSame(UcteVoltageLevelCode.VL_220, ucteExporter.voltageLevelCodeFromChar('2'));
        assertSame(UcteVoltageLevelCode.VL_150, ucteExporter.voltageLevelCodeFromChar('3'));
        assertSame(UcteVoltageLevelCode.VL_120, ucteExporter.voltageLevelCodeFromChar('4'));
        assertSame(UcteVoltageLevelCode.VL_110, ucteExporter.voltageLevelCodeFromChar('5'));
        assertSame(UcteVoltageLevelCode.VL_70, ucteExporter.voltageLevelCodeFromChar('6'));
        assertSame(UcteVoltageLevelCode.VL_27, ucteExporter.voltageLevelCodeFromChar('7'));
        exception.expect(IllegalArgumentException.class);
        assertSame(new IllegalArgumentException(), ucteExporter.voltageLevelCodeFromChar('&'));
    }

    @Test
    public void iidmIdToUcteNodeCodeTest() {
        assertEquals(new UcteNodeCode(UcteCountryCode.ES, "HORTA", UcteVoltageLevelCode.VL_220, '1'),
                ucteExporter.iidmIdToUcteNodeCode("EHORTA21"));
        assertNotEquals(new UcteNodeCode(UcteCountryCode.ES, "HORTA", UcteVoltageLevelCode.VL_220, '1'),
                ucteExporter.iidmIdToUcteNodeCode("EHOARA21"));
        exception.expect(IllegalArgumentException.class);
        assertSame(new IllegalArgumentException(), ucteExporter.iidmIdToUcteNodeCode("EHOAA21"));
    }

    @Test
    public void calculatePhaseDuTest() {
        assertEquals(
                2.000000019938067,
                ucteExporter.calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.0000000000000001);
        assertNotEquals(
                2.000000019938066,
                ucteExporter.calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.0000000000000001);
    }

    @Test
    public void isNotAlreadyCreatedTest() {
        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        TwoWindingsTransformer twoWindingsTransformer = transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1");

        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        UcteNodeCode ucteNodeCode1 = new UcteNodeCode(UcteCountryCode.ME, "BBBBB", UcteVoltageLevelCode.VL_110, ' ');
        UcteNodeCode ucteNodeCode2 = new UcteNodeCode(UcteCountryCode.ME, "AAAAA", UcteVoltageLevelCode.VL_220, ' ');

        UcteElementId ucteElementId = new UcteElementId(ucteNodeCode1, ucteNodeCode2, '1');
        assertTrue(ucteExporter.isNotAlreadyCreated(ucteNetwork, ucteElementId));
        ucteNetwork.addTransformer(
                new UcteTransformer(
                        ucteElementId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION, 0f, 0f, 0f, 0,
                        null, 0f, 0f, 0f, 0f));
        assertFalse(ucteExporter.isNotAlreadyCreated(ucteNetwork, ucteElementId));
    }

    @Test
    public void createUcteElementIdTest() {
        TwoWindingsTransformer twoWindingsTransformer = transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1");

        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        UcteNodeCode ucteNodeCode1 = new UcteNodeCode(UcteCountryCode.ME, "BBBBB", UcteVoltageLevelCode.VL_110, ' ');
        UcteNodeCode ucteNodeCode2 = new UcteNodeCode(UcteCountryCode.ME, "AAAAA", UcteVoltageLevelCode.VL_220, ' ');

        UcteElementId ucteElementId1 = new UcteElementId(ucteNodeCode1, ucteNodeCode2, '1');
        UcteElementId ucteElementId2 = new UcteElementId(ucteNodeCode2, ucteNodeCode1, '1');

        assertEquals(ucteElementId1, ucteExporter.createUcteElementId(ucteNodeCode1, ucteNodeCode2, twoWindingsTransformer, terminal1, terminal2));
        assertNotEquals(ucteElementId2, ucteExporter.createUcteElementId(ucteNodeCode1, ucteNodeCode2, twoWindingsTransformer, terminal1, terminal2));
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
