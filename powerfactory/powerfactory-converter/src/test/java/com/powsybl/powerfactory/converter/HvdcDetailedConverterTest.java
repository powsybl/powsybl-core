package com.powsybl.powerfactory.converter;

// import static org.mockito.Mockito.when;

import java.util.Properties;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HvdcDetailedConverterTest {

    // test tolerance for double values.
    // Only makes sense for values that are neither too big or too small.
    // This is twice machine epsilon for single precision
    static final double ABSOLUTE_DELTA = 1.2e-7;

    @Test
    void testCreate1() {
        Network network = importDgs("hvdc-2-VSC");

        final double nominalDcV = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(2, network.getDcNodeCount());
        assertEquals(nominalDcV, network.getVoltageSourceConverter("HVDC Converter 1").getTargetVdc());
        assertEquals(nominalDcV, network.getVoltageSourceConverter("HVDC Converter 2").getTargetVdc());
        assertEquals(600.0, network.getVoltageSourceConverter("HVDC Converter 1").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getReactivePowerSetpoint());
        assertEquals(100.0, network.getVoltageSourceConverter("HVDC Converter 2").getReactivePowerSetpoint());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 1").getIdleLoss());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 2").getIdleLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getSwitchingLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(nominalDcV, node.getNominalV());
        }

    }

    @Test
    void testCreate2() {
        Network network = importDgs("hvdc-2-VSC-ACDC-links");

        final double nominalDcV = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(nominalDcV, network.getVoltageSourceConverter("HVDC Converter 1").getTargetVdc());
        assertEquals(nominalDcV, network.getVoltageSourceConverter("HVDC Converter 2").getTargetVdc());
        assertEquals(600.0, network.getVoltageSourceConverter("HVDC Converter 1").getTargetP(), ABSOLUTE_DELTA);
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getReactivePowerSetpoint());
        assertEquals(100.0, network.getVoltageSourceConverter("HVDC Converter 2").getReactivePowerSetpoint(), ABSOLUTE_DELTA);
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 1").getIdleLoss(), ABSOLUTE_DELTA);
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 2").getIdleLoss(), ABSOLUTE_DELTA);
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getSwitchingLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(nominalDcV, node.getNominalV(), ABSOLUTE_DELTA);
        }

        final double resistanceDcLine = 50.0 * 0.1;

        for (DcLine line : network.getDcLines()) {
            assertEquals(resistanceDcLine, line.getR(), ABSOLUTE_DELTA);
        }

        DcLine dcLine1 = network.getDcLine("DC-Line_pos");
        VoltageSourceConverter vsc1 = network.getVoltageSourceConverter("HVDC Converter 1");
        VoltageSourceConverter vsc2 = network.getVoltageSourceConverter("HVDC Converter 2");
        assertSame(dcLine1.getDcTerminal1().getDcNode(), vsc1.getDcTerminal1().getDcNode());
        assertSame(dcLine1.getDcTerminal2().getDcNode(), vsc2.getDcTerminal1().getDcNode());

    }

    @Test
    void testCreate3() {
        Network network = importDgs("hvdc-3-VSC-ACDC-links");

        assertEquals(6, network.getDcNodeCount());
        assertEquals(3, network.getVoltageSourceConverterCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(1, network.getDcGroundCount());

        DcLine dcLine21 = network.getDcLine("DC line 0 31-32");
        DcLine dcLine22 = network.getDcLine("DC line - 32-33");
        DcLine dcLine23 = network.getDcLine("DC line + 31-33");

        assertNotNull(dcLine21);
        assertNotNull(dcLine22);
        assertNotNull(dcLine23);

        assertNotSame(dcLine21, dcLine22);
        assertNotSame(dcLine22, dcLine23);
        assertNotSame(dcLine23, dcLine21);

    }

    private Network importDgs(String id) {

        Properties importParams = new Properties();
        importParams.put(PowerFactoryImporter.HVDC_IMPORT_DETAILED, true);
        final String fileName = id + ".dgs";
        PowerFactoryImporter importer = new PowerFactoryImporter();
        ResourceSet resourceSet = new ResourceSet("/", fileName);
        ResourceDataSource dataSource = new ResourceDataSource(id, resourceSet);

        return importer.importData(dataSource, NetworkFactory.findDefault(), importParams);
    }

}
