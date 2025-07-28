/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */

class AcDcConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/hvdc/";
    private static final Properties IMPORT_PARAMS = new Properties();
    private static final String ALIAS_TERMINAL1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL1;
    private static final String ALIAS_TERMINAL2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.TERMINAL2;
    private static final String ALIAS_DC_TERMINAL1 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL1;
    private static final String ALIAS_DC_TERMINAL2 = Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + CgmesNames.DC_TERMINAL2;

    @BeforeAll
    static void setUpProperties() {
        IMPORT_PARAMS.put(CgmesImport.USE_DETAILED_DC_MODEL, "true");
    }

    @Test
    void mixedBipoleEqTest() {
        // CGMES network:
        //   A mixed HVDC bipole:
        //   - Pole 1 is LCC based, has 2 AC connections and is rated at 250MW/250kV.
        //   - Pole 2 is VSC based, has 1 AC connection and is rated at 500MW/500kV.
        //   - There is 1 dc line per pole plus a dedicated metallic return line which is grounded.
        //   - DCBreakers can short-circuit the converters if they are closed (in case of a converter outage).
        // IIDM network:
        //   - All objects (nodes, switches, grounds, lines, converters) have been imported in their dedicated class.
        //   - Without SSH input, all switches are closed, all converters target power are 0, lcc power factor is calculated with Q = 0,5P.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml");

        // Verify all objects have been imported.
        assertEquals(14, network.getDcNodeCount());
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getLineCommutatedConverterCount());

        // Verify objects properties.
        // DCNode.
        DcNode dcNode11 = network.getDcNode("DCN_1_1");
        assertNotNull(dcNode11);
        assertEquals("DC node 1 1", dcNode11.getNameOrId());
        assertEquals(250.0, dcNode11.getNominalV());

        DcNode dcNode12 = network.getDcNode("DCN_1_2");
        assertNotNull(dcNode12);
        assertEquals("DC node 1 2", dcNode12.getNameOrId());
        assertEquals(500.0, dcNode12.getNominalV());

        // DCSwitch.
        DcSwitch dcBreaker = network.getDcSwitch("DCSW_1_1");
        assertNotNull(dcBreaker);
        assertEquals("DC breaker 1 1", dcBreaker.getNameOrId());
        assertFalse(dcBreaker.isOpen());
        assertEquals(DcSwitchKind.BREAKER, dcBreaker.getKind());
        assertEquals("DCN_1_1", dcBreaker.getDcNode1().getId());
        assertEquals("DCN_1_G", dcBreaker.getDcNode2().getId());
        assertEquals("T_DCSW_1_1_1", dcBreaker.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());
        assertEquals("T_DCSW_1_1_2", dcBreaker.getAliasFromType(ALIAS_DC_TERMINAL2).orElseThrow());

        DcSwitch dcDisconnector = network.getDcSwitch("DCSW_1_1P");
        assertNotNull(dcDisconnector);
        assertEquals("DC disconnector 1 1P", dcDisconnector.getNameOrId());
        assertFalse(dcDisconnector.isOpen());
        assertEquals(DcSwitchKind.DISCONNECTOR, dcDisconnector.getKind());
        assertEquals("DCN_1_1P", dcDisconnector.getDcNode1().getId());
        assertEquals("DCN_1_G", dcDisconnector.getDcNode2().getId());
        assertEquals("T_DCSW_1_1P_1", dcDisconnector.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());
        assertEquals("T_DCSW_1_1P_2", dcDisconnector.getAliasFromType(ALIAS_DC_TERMINAL2).orElseThrow());

        // DCGround.
        DcGround dcGround = network.getDcGround("DCGRND");
        assertNotNull(dcGround);
        assertEquals("DC ground", dcGround.getNameOrId());
        assertEquals(0.1, dcGround.getR());
        assertEquals("DCN_1_G", dcGround.getDcTerminal().getDcNode().getId());
        assertEquals("T_DCGRND", dcGround.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());

        // DCLine
        DcLine dcLine = network.getDcLine("DCL_1");
        assertNotNull(dcLine);
        assertEquals("DC line 1", dcLine.getNameOrId());
        assertEquals(0.5, dcLine.getR());
        assertEquals("DCN_1_1", dcLine.getDcTerminal1().getDcNode().getId());
        assertEquals("DCN_2_1", dcLine.getDcTerminal2().getDcNode().getId());
        assertEquals("T_DCL_1_1", dcLine.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());
        assertEquals("T_DCL_1_2", dcLine.getAliasFromType(ALIAS_DC_TERMINAL2).orElseThrow());

        // LineCommutatedConverter
        LineCommutatedConverter lcc = network.getLineCommutatedConverter("CSC_1_1");
        assertNotNull(lcc);
        assertEquals("Current source converter 1 1", lcc.getNameOrId());
        assertEquals(0.1, lcc.getIdleLoss());
        assertEquals(0.0003, lcc.getSwitchingLoss());
        assertEquals(0.6, lcc.getResistiveLoss());
        assertEquals(AcDcConverter.ControlMode.P_PCC, lcc.getControlMode());
        assertEquals(0.0, lcc.getTargetP());
        assertEquals(Double.NaN, lcc.getTargetVdc());
        assertEquals("ACL_1", lcc.getPccTerminal().getConnectable().getId());
        assertEquals(LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR, lcc.getReactiveModel());
        assertEquals(0.8944271909999159, lcc.getPowerFactor());
        assertEquals("DCN_1_1P", lcc.getDcTerminal1().getDcNode().getId());
        assertEquals("DCN_1_1N", lcc.getDcTerminal2().getDcNode().getId());
        assertEquals("T_CSC_1_1_1", lcc.getAliasFromType(ALIAS_TERMINAL1).orElseThrow());
        assertEquals("T_CSC_1_1_2", lcc.getAliasFromType(ALIAS_TERMINAL2).orElseThrow());
        assertEquals("T_CSC_1_1_3", lcc.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());
        assertEquals("T_CSC_1_1_4", lcc.getAliasFromType(ALIAS_DC_TERMINAL2).orElseThrow());

        // VoltageSourceConverter
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VSC_1_2");
        assertNotNull(vsc);
        assertEquals("Voltage source converter 1 2", vsc.getNameOrId());
        assertEquals(0.2, vsc.getIdleLoss());
        assertEquals(0.0006, vsc.getSwitchingLoss());
        assertEquals(1.2, vsc.getResistiveLoss());
        assertEquals(AcDcConverter.ControlMode.P_PCC, vsc.getControlMode());
        assertEquals(0.0, vsc.getTargetP());
        assertEquals(Double.NaN, vsc.getTargetVdc());
        assertEquals("PT_1_2", vsc.getPccTerminal().getConnectable().getId());
        assertFalse(vsc.isVoltageRegulatorOn());
        assertEquals(0.0, vsc.getReactivePowerSetpoint());
        assertEquals(Double.NaN, vsc.getVoltageSetpoint());
        assertEquals("DCN_1_2P", vsc.getDcTerminal1().getDcNode().getId());
        assertEquals("DCN_1_2N", vsc.getDcTerminal2().getDcNode().getId());
        assertEquals("T_VSC_1_2_1", vsc.getAliasFromType(ALIAS_TERMINAL1).orElseThrow());
        assertEquals("T_VSC_1_2_2", vsc.getAliasFromType(ALIAS_DC_TERMINAL1).orElseThrow());
        assertEquals("T_VSC_1_2_3", vsc.getAliasFromType(ALIAS_DC_TERMINAL2).orElseThrow());

        // ReactiveLimits
        ReactiveLimits limits = vsc.getReactiveLimits();
        assertNotNull(limits);
        assertEquals(ReactiveLimitsKind.CURVE, limits.getKind());
        List<ReactiveCapabilityCurve.Point> points = ((ReactiveCapabilityCurve) limits).getPoints().stream()
                .sorted(Comparator.comparingDouble(ReactiveCapabilityCurve.Point::getP))
                .toList();
        assertEquals(3, points.size());
        assertEquals(-525.0, points.get(0).getP());
        assertEquals(-100.0, points.get(0).getMinQ());
        assertEquals(100.0, points.get(0).getMaxQ());
        assertEquals(0.0, points.get(1).getP());
        assertEquals(-315.0, points.get(1).getMinQ());
        assertEquals(315.0, points.get(1).getMaxQ());
        assertEquals(525.0, points.get(2).getP());
        assertEquals(-100.0, points.get(2).getMinQ());
        assertEquals(100.0, points.get(2).getMaxQ());
    }

    @Test
    void mixedBipoleSshTest() {
        // CGMES network:
        //   SSH data brings switches statuses and converter state values.
        // IIDM network:
        //   Switches are open and converters have their rated state values.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");

        // Verify all objects have been imported.
        assertEquals(14, network.getDcNodeCount());
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getLineCommutatedConverterCount());

        // Verify objects properties.
        // DCSwitch.
        DcSwitch dcBreaker = network.getDcSwitch("DCSW_1_1");
        assertTrue(dcBreaker.isOpen());

        DcSwitch dcDisconnector = network.getDcSwitch("DCSW_1_1P");
        assertFalse(dcDisconnector.isOpen());

        // LineCommutatedConverter
        LineCommutatedConverter lcc = network.getLineCommutatedConverter("CSC_1_1");
        assertEquals(AcDcConverter.ControlMode.P_PCC, lcc.getControlMode());
        assertEquals(250.0, lcc.getTargetP());
        assertEquals(Double.NaN, lcc.getTargetVdc());
        assertEquals(0.876215908676647, lcc.getPowerFactor());

        LineCommutatedConverter lcc2 = network.getLineCommutatedConverter("CSC_2_1");
        assertEquals(AcDcConverter.ControlMode.V_DC, lcc2.getControlMode());
        assertEquals(Double.NaN, lcc2.getTargetP());
        assertEquals(248.5, lcc2.getTargetVdc());
        assertEquals(0.8944271909999159, lcc2.getPowerFactor());

        // VoltageSourceConverter
        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VSC_1_2");
        assertEquals(AcDcConverter.ControlMode.P_PCC, vsc.getControlMode());
        assertEquals(500.0, vsc.getTargetP());
        assertEquals(Double.NaN, vsc.getTargetVdc());
        assertFalse(vsc.isVoltageRegulatorOn());
        assertEquals(1.0, vsc.getReactivePowerSetpoint());
        assertEquals(Double.NaN, vsc.getVoltageSetpoint());

        VoltageSourceConverter vsc2 = network.getVoltageSourceConverter("VSC_2_2");
        assertEquals(AcDcConverter.ControlMode.V_DC, vsc2.getControlMode());
        assertEquals(Double.NaN, vsc2.getTargetP());
        assertEquals(497.0, vsc2.getTargetVdc());
        assertTrue(vsc2.isVoltageRegulatorOn());
        assertEquals(Double.NaN, vsc2.getReactivePowerSetpoint());
        assertEquals(400.0, vsc2.getVoltageSetpoint());
    }
}
