package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.UctePowerPlantType;
import com.powsybl.ucte.network.UcteVoltageLevelCode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest {

    @Test
    public void exportUcte() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("transformerRegulation", new ResourceSet("/", "transformerRegulation.uct"));
        Network network = new UcteImporter().importData(dataSource, null);
        new UcteExporter().export(network, null, null);
    }

    @Test
    public void isUcteCountryCodeTest() {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isUcteCountryCode('A'));
        assertTrue(ucteExporter.isUcteCountryCode('1'));
        assertFalse(ucteExporter.isUcteCountryCode('_'));
        assertFalse(ucteExporter.isUcteCountryCode('&'));
    }

    @Test
    public void isVoltageLevelTest() {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isVoltageLevel('0'));
        assertTrue(ucteExporter.isVoltageLevel('9'));
        assertFalse(ucteExporter.isVoltageLevel('_'));
        assertFalse(ucteExporter.isVoltageLevel('&'));
    }

    @Test
    public void isUcteNodeId() {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isUcteNodeId("B_SU1_11"));
        assertTrue(ucteExporter.isUcteNodeId("B_SU1_1 "));
        assertTrue(ucteExporter.isUcteNodeId("7efG8411"));
        assertFalse(ucteExporter.isUcteNodeId("        "));
        assertFalse(ucteExporter.isUcteNodeId("B_SU1_"));
        assertFalse(ucteExporter.isUcteNodeId("&ezrt874g"));
    }

    @Test
    public void isUcteNodeTest() {
        UcteExporter ucteExporter = new UcteExporter();
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
        UcteExporter ucteExporter = new UcteExporter();
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
        UcteExporter ucteExporter = new UcteExporter();
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
        assertSame(null, ucteExporter.iidmVoltageToUcteVoltageLevelCode(15));
        assertNotSame(UcteVoltageLevelCode.VL_27, ucteExporter.iidmVoltageToUcteVoltageLevelCode(330));
    }

//    @Test //TODO : Fix this test
//    public void createUcteNodeCodeTest() {
//        ReadOnlyDataSource dataSource = new ResourceDataSource("countryIssue", new ResourceSet("/", "countryIssue.uct"));
//        Network network = new UcteImporter().importData(dataSource, null);
//        UcteExporter ucteExporter = new UcteExporter();
//        assertTrue(new UcteNodeCode(UcteCountryCode.ME, "BAR  ", UcteVoltageLevelCode.VL_110, ' ').equals(ucteExporter.createUcteNodeCode("0BAR  5 ", network.getVoltageLevel("0BAR  5"), "ME")));
//    }
}
