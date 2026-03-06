/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.*;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 */
public class DetailedHvdcConverterTest {

    // test tolerance for double values.
    // Only makes sense for values that are neither too big or too small.
    // This is twice machine epsilon for single precision
    static final double ABSOLUTE_DELTA = 1.2e-7;
    static final double RELATIVE_DELTA = 1.2e-7;

    @Test
    void testCreate1() {
        // 2 VSCs directly connected by their DC. No DC line or AC network.
        Network network = importDgs("MTDC-2-VSC");

        VoltageSourceConverter vsc1 = network.getVoltageSourceConverter("HVDC Converter 1");
        VoltageSourceConverter vsc2 = network.getVoltageSourceConverter("HVDC Converter 2");

        final double nominalDcV = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(0, network.getDcGroundCount());
        assertEquals(2, network.getDcNodeCount());
        assertTrue(Double.isNaN(vsc1.getTargetVdc()));
        final double vDcSetPointPu = 1.01;
        final double targetDcV2 = nominalDcV * vDcSetPointPu;
        assertEquals(targetDcV2, vsc2.getTargetVdc(), RELATIVE_DELTA * targetDcV2);
        assertEquals(-600.0, vsc1.getTargetP());
        assertEquals(-0.0, vsc2.getTargetP());
        assertEquals(-0.0, vsc1.getReactivePowerSetpoint());
        assertEquals(-100.0, vsc2.getReactivePowerSetpoint());
        assertEquals(10.0, vsc1.getIdleLoss()); // unit change from kW to MW
        final double idleLoss2 = 10.0 * vDcSetPointPu * vDcSetPointPu;
        assertEquals(idleLoss2, vsc2.getIdleLoss(), RELATIVE_DELTA * idleLoss2); // unit change from kW to MW
        assertEquals(0.0, vsc1.getResistiveLoss());
        assertEquals(0.0, vsc2.getResistiveLoss());
        assertEquals(0.0, vsc1.getSwitchingLoss());
        assertEquals(0.0, vsc2.getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(nominalDcV, node.getNominalV());
        }

    }

    @Test
    void testCreate2() {
        // 2 VSCs connected by DC lines.
        // VSC 'HVDC Converter 2' is also connected to a dangling AC line.
        // This tests that there is no confusion between AC and DC lines.
        Network network = importDgs("MTDC-2-VSC-ACDC-links");

        VoltageSourceConverter vsc1 = network.getVoltageSourceConverter("HVDC Converter 1");
        VoltageSourceConverter vsc2 = network.getVoltageSourceConverter("HVDC Converter 2");

        final double nominalDcV = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(0, network.getDcGroundCount());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertTrue(Double.isNaN(vsc1.getTargetVdc()));
        final double vDcSetPointPu = 1.01;
        final double targetDcV2 = nominalDcV * vDcSetPointPu;
        assertEquals(targetDcV2, vsc2.getTargetVdc(), RELATIVE_DELTA * targetDcV2);
        assertEquals(-600.0, vsc1.getTargetP(), ABSOLUTE_DELTA);
        assertEquals(-0.0, vsc2.getTargetP());
        assertEquals(-0.0, vsc1.getReactivePowerSetpoint());
        assertEquals(-100.0, vsc2.getReactivePowerSetpoint(), ABSOLUTE_DELTA);
        assertEquals(10.0, vsc1.getIdleLoss()); // unit change from kW to MW
        final double idleLoss2 = 10.0 * vDcSetPointPu * vDcSetPointPu;
        assertEquals(idleLoss2, vsc2.getIdleLoss(), RELATIVE_DELTA * idleLoss2); // unit change from kW to MW
        assertEquals(0.0, vsc1.getResistiveLoss());
        assertEquals(0.0, vsc2.getResistiveLoss());
        assertEquals(0.0, vsc1.getSwitchingLoss());
        assertEquals(0.0, vsc2.getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(nominalDcV, node.getNominalV(), ABSOLUTE_DELTA);
        }

        final double resistanceDcLine = 50.0 * 0.1;

        for (DcLine line : network.getDcLines()) {
            assertEquals(resistanceDcLine, line.getR(), ABSOLUTE_DELTA);
        }

        DcLine dcLine1 = network.getDcLine("DC-Line_pos");
        assertSame(dcLine1.getDcTerminal1().getDcNode(), vsc1.getDcTerminal1().getDcNode());
        assertSame(dcLine1.getDcTerminal2().getDcNode(), vsc2.getDcTerminal1().getDcNode());

    }

    @Test
    void testCreate3() {
        // 3 VSCs connected by DC lines in a triangle configuration.
        Network network = importDgs("MTDC-3-VSC-ACDC-links");

        assertEquals(6, network.getDcNodeCount());
        assertEquals(3, network.getVoltageSourceConverterCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(0, network.getDcGroundCount());

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

    @Test
    void testCreate4() {
        // Test with 2 pairs of VSCs + DC lines connected by an AC line.
        // The purpose of the test is to check that 2 DC subnetworks are indeed created.
        // Each pair of VSCs have distinct nominal DC voltage level. We then count
        // the number of DC voltage levels to verify there are 2 DC subnetworks.
        Network network = importDgs("MTDC-4-VSC-ACDC-links");

        assertEquals(8, network.getDcNodeCount());
        assertEquals(4, network.getVoltageSourceConverterCount());
        assertEquals(4, network.getDcLineCount());
        assertEquals(0, network.getDcGroundCount());
        assertEquals(1, network.getLineCount());

        assertEquals(3, network.getVoltageLevelCount());
        assertEquals(2, network.getDcNodeStream().map(DcNode::getNominalV).distinct().count());
    }

    @Test
    void testVsc1() {
        // check i_acdc = 3
        Network network = importDgs("MTDCVscVariants1");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        assertEquals(AcDcConverter.ControlMode.V_DC, vsc.getControlMode());
        assertFalse(vsc.isVoltageRegulatorOn());
        assertTrue(Double.isNaN(vsc.getVoltageSetpoint()));
        assertEquals(420.0, vsc.getTargetVdc(), RELATIVE_DELTA * 420.0);
    }

    @Test
    void testVsc2() {
        // check i_acdc = 4
        Network network = importDgs("MTDCVscVariants2");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        assertEquals(AcDcConverter.ControlMode.P_PCC, vsc.getControlMode());
        assertTrue(vsc.isVoltageRegulatorOn());
        assertEquals(-1234., vsc.getTargetP(), RELATIVE_DELTA * 1234.);
        double acVoltageSetPoint = 0.8 * 300.;
        assertEquals(acVoltageSetPoint, vsc.getVoltageSetpoint(), RELATIVE_DELTA * acVoltageSetPoint);

        // while we are there, check that some values are indeed unspecified
        assertTrue(Double.isNaN(vsc.getReactivePowerSetpoint()));
        assertEquals(0.0, vsc.getIdleLoss());
        assertTrue(Double.isNaN(vsc.getTargetVdc()));
        assertEquals(0.0, vsc.getSwitchingLoss());
        assertEquals(0.0, vsc.getResistiveLoss());
    }

    @Test
    void testVsc3() {
        // check i_acdc = 5
        Network network = importDgs("MTDCVscVariants3");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        assertEquals(AcDcConverter.ControlMode.P_PCC, vsc.getControlMode());
        assertFalse(vsc.isVoltageRegulatorOn());
        assertEquals(-1234., vsc.getTargetP(), RELATIVE_DELTA * 1234.);
        assertEquals(-4321., vsc.getReactivePowerSetpoint(), RELATIVE_DELTA * 4321.);

        // while we are there, check that some values are indeed unspecified
        assertTrue(Double.isNaN(vsc.getVoltageSetpoint()));
        assertEquals(0.0, vsc.getIdleLoss());
        assertTrue(Double.isNaN(vsc.getTargetVdc()));
        assertEquals(0.0, vsc.getSwitchingLoss());
        assertEquals(0.0, vsc.getResistiveLoss());
    }

    @Test
    void testVsc4() {
        // check i_acdc = 6
        Network network = importDgs("MTDCVscVariants4");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        assertEquals(AcDcConverter.ControlMode.V_DC, vsc.getControlMode());
        assertTrue(vsc.isVoltageRegulatorOn());
        double acVoltageSetPoint = 1.2 * 300.;
        assertEquals(acVoltageSetPoint, vsc.getVoltageSetpoint(), RELATIVE_DELTA * acVoltageSetPoint);
        double dcVoltageSetPoint = 0.8 * 525.;
        assertEquals(dcVoltageSetPoint, vsc.getTargetVdc(), RELATIVE_DELTA * dcVoltageSetPoint);

        // while we are there, check that some values are indeed unspecified
        assertTrue(Double.isNaN(vsc.getTargetP()));
        assertTrue(Double.isNaN(vsc.getReactivePowerSetpoint()));
        assertEquals(0.0, vsc.getIdleLoss());
        assertEquals(0.0, vsc.getSwitchingLoss());
        assertEquals(0.0, vsc.getResistiveLoss());
    }

    @Test
    void testVsc5() {
        // check i_acdc = 0 end in error
        try {
            importDgs("MTDCVscVariants5");
            fail("Expected exception not thrown");
        } catch (PowerFactoryException e) {
            assertEquals("Unsupported value 0 for VSC 6.", e.getMessage());
        }
    }

    @Test
    void testVscLoss1() {
        // check value of idle loss when DC voltage is controlled
        Network network = importDgs("MTDCVscLoss1");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        double idleLoss = 1.234 / 1000 * 0.8 * 0.8;
        assertEquals(idleLoss, vsc.getIdleLoss(), RELATIVE_DELTA * idleLoss);
    }

    @Test
    void testVscLoss2() {
        // check value of idle loss when DC voltage is _not_ controlled
        Network network = importDgs("MTDCVscLoss2");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        double idleLoss = 1.234 / 1000; // convert from kW to MW.
        assertEquals(idleLoss, vsc.getIdleLoss(), RELATIVE_DELTA * idleLoss);
    }

    @Test
    void testVscLoss3() {
        // check values of switching loss and resistive loss
        Network network = importDgs("MTDCVscLoss3");
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("HVDC Converter 1");

        double switchingLoss = 1.234 / 1000; // MW / A (as opposed to kW / A in PowerFactory)
        double resistiveLoss = 4.321; // Ohm
        assertEquals(switchingLoss, vsc.getSwitchingLoss(), RELATIVE_DELTA * switchingLoss);
        assertEquals(resistiveLoss, vsc.getResistiveLoss(), RELATIVE_DELTA * resistiveLoss);
    }

    @Test
    void testGround() {
        Network network = importDgs("MTDC-ElmGndswt");

        assertEquals(2, network.getDcGroundCount());

        DcGround ground25 = network.getDcGround("Grounding Switch grounded");

        assertNotNull(ground25);
        assertEquals(0.0, ground25.getR());
        assertEquals("Node_dc_r_ground 5", ground25.getDcTerminal().getDcNode().getId());

        DcGround ground26 = network.getDcGround("Grounding Switch not grounded not off");
        assertNull(ground26);

        DcGround ground27 = network.getDcGround("Grounding Switch off");
        assertNull(ground27);

        DcGround ground28 = network.getDcGround("Grounding not grounded");
        assertNull(ground28);

        DcGround ground29 = network.getDcGround("Grounding Switch default values");
        assertNotNull(ground29);
        assertEquals(0.0, ground29.getR());
        assertEquals("Node_dc_r_ground 9", ground29.getDcTerminal().getDcNode().getId());
    }

    private Network importDgs(String id) {

        Properties importParams = new Properties();
        importParams.put(PowerFactoryImporter.HVDC_IMPORT_MT, true);
        final String fileName = id + ".dgs";
        PowerFactoryImporter importer = new PowerFactoryImporter();
        ResourceSet resourceSet = new ResourceSet("/", fileName);
        ResourceDataSource dataSource = new ResourceDataSource(id, resourceSet);

        return importer.importData(dataSource, NetworkFactory.findDefault(), importParams);
    }

}
