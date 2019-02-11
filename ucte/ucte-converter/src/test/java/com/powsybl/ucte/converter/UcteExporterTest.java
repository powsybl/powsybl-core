package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class UcteExporterTest {

    @Test
    public void exportUcte() {

        ReadOnlyDataSource dataSource = new ResourceDataSource("elementName", new ResourceSet("/", "elementName.uct"));
        Network network = new UcteImporter().importData(dataSource, null);

        new UcteExporter().export(network,null, null);
    }

    @Test
    public void isUcteCountryCodeTest()
    {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isUcteCountryCode('A'));
        assertTrue(ucteExporter.isUcteCountryCode('1'));
        assertFalse(ucteExporter.isUcteCountryCode('_'));
        assertFalse(ucteExporter.isUcteCountryCode('&'));
    }

    @Test
    public void isVoltageLevelTest()
    {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isVoltageLevel('0'));
        assertTrue(ucteExporter.isVoltageLevel('9'));
        assertFalse(ucteExporter.isVoltageLevel('_'));
        assertFalse(ucteExporter.isVoltageLevel('&'));
    }

    @Test
    public void isUcteId()
    {
        UcteExporter ucteExporter = new UcteExporter();
        assertTrue(ucteExporter.isUcteId("B_SU1_11"));
        assertTrue(ucteExporter.isUcteId("B_SU1_1"));
        assertTrue(ucteExporter.isUcteId("7efG8411"));
        assertFalse(ucteExporter.isUcteId("        "));
        assertFalse(ucteExporter.isUcteId("B_SU1_"));
        assertFalse(ucteExporter.isUcteId("&ezrt874g"));
    }
}
