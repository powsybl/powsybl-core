/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.extensions.CgmesTopologyKind;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
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
    void dcNetworkEqImportTest() {
        // CGMES network:
        //   A mixed HVDC bipole:
        //   - Pole 1 is LCC based, has 2 AC connections and is rated at 250MW/250kV.
        //   - Pole 2 is VSC based, has 1 AC connection and is rated at 500MW/500kV.
        //   - There is 1 dc line per pole plus a dedicated metallic return line which is grounded.
        //   - DCBreakers can short-circuit the converters if they are closed (in case of a converter outage).
        // IIDM network:
        //   - All objects (nodes, switches, grounds, lines, converters) have been imported in their dedicated class.
        //   - Without SSH input, all switches are closed, all converters target power are 0, lcc power factor is calculated with Q = 0.5P.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml");

        // Verify all dc objects have been imported.
        assertEquals(14, network.getDcNodeCount());
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getLineCommutatedConverterCount());

        // Verify dc objects properties.
        // DCNode
        DcNode dcNode11 = network.getDcNode("DCN_1_1");
        assertNotNull(dcNode11);
        assertEquals("DC node 1 1", dcNode11.getNameOrId());
        assertEquals(250.0, dcNode11.getNominalV());

        DcNode dcNode12 = network.getDcNode("DCN_1_2");
        assertNotNull(dcNode12);
        assertEquals("DC node 1 2", dcNode12.getNameOrId());
        assertEquals(500.0, dcNode12.getNominalV());

        // DCSwitch
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

        // DCGround
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
    void dcNetworkSshImportTest() {
        // CGMES network:
        //   SSH data brings switches statuses and converter state values.
        // IIDM network:
        //   Switches are open and converters have their rated state values.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");

        // Verify all dc objects have been imported.
        assertEquals(14, network.getDcNodeCount());
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(3, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getLineCommutatedConverterCount());

        // Verify dc objects properties.
        // DCSwitch
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

    @Test
    void dcNetworkEqExportTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole with VSC, LCC, some DCSwitches and a DCGround.
        // CGMES network:
        //   All objects (nodes, switches, grounds, lines, converters) have been exported in their dedicated class.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml");
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, new Properties());

        // Verify all dc objects have been exported.
        assertEquals(4, getElementCount(eqFile, "DCConverterUnit"));
        assertEquals(14, getElementCount(eqFile, "DCNode"));
        assertEquals(31, getElementCount(eqFile, "DCTerminal"));
        assertEquals(8, getElementCount(eqFile, "ACDCConverterDCTerminal"));
        assertEquals(4, getElementCount(eqFile, "DCBreaker"));
        assertEquals(8, getElementCount(eqFile, "DCDisconnector"));
        assertEquals(1, getElementCount(eqFile, "DCGround"));
        assertEquals(3, getElementCount(eqFile, "DCLineSegment"));
        assertEquals(2, getElementCount(eqFile, "VsConverter"));
        assertEquals(2, getElementCount(eqFile, "CsConverter"));

        // Verify dc objects properties.
        // DCNode
        String dcNode11 = getElement(eqFile, "DCNode", "DCN_1_1");
        assertEquals("DC node 1 1", getAttribute(dcNode11, "IdentifiedObject.name"));

        // DCBreaker
        String dcBreaker = getElement(eqFile, "DCBreaker", "DCSW_1_1");
        assertEquals("DC breaker 1 1", getAttribute(dcBreaker, "IdentifiedObject.name"));

        String dcBreakerT1 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1_1");
        assertEquals("DC breaker 1 1 1", getAttribute(dcBreakerT1, "IdentifiedObject.name"));
        assertEquals("DCSW_1_1", getResource(dcBreakerT1, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_1", getResource(dcBreakerT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(dcBreakerT1, "ACDCTerminal.sequenceNumber"));

        String dcBreakerT2 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1_2");
        assertEquals("DC breaker 1 1 2", getAttribute(dcBreakerT2, "IdentifiedObject.name"));
        assertEquals("DCSW_1_1", getResource(dcBreakerT2, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_G", getResource(dcBreakerT2, "DCBaseTerminal.DCNode"));
        assertEquals("2", getAttribute(dcBreakerT2, "ACDCTerminal.sequenceNumber"));

        // DCDisconnector
        String dcDisconnector = getElement(eqFile, "DCDisconnector", "DCSW_1_1P");
        assertEquals("DC disconnector 1 1P", getAttribute(dcDisconnector, "IdentifiedObject.name"));

        String dcDisconnectorT1 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1P_1");
        assertEquals("DC disconnector 1 1P 1", getAttribute(dcDisconnectorT1, "IdentifiedObject.name"));
        assertEquals("DCSW_1_1P", getResource(dcDisconnectorT1, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_1P", getResource(dcDisconnectorT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(dcDisconnectorT1, "ACDCTerminal.sequenceNumber"));

        String dcDisconnectorT2 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1P_2");
        assertEquals("DC disconnector 1 1P 2", getAttribute(dcDisconnectorT2, "IdentifiedObject.name"));
        assertEquals("DCSW_1_1P", getResource(dcDisconnectorT2, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_G", getResource(dcDisconnectorT2, "DCBaseTerminal.DCNode"));
        assertEquals("2", getAttribute(dcDisconnectorT2, "ACDCTerminal.sequenceNumber"));

        // DCGround
        String dcGround = getElement(eqFile, "DCGround", "DCGRND");
        assertEquals("DC ground", getAttribute(dcGround, "IdentifiedObject.name"));
        assertEquals("0.1", getAttribute(dcGround, "DCGround.r"));

        String dcGroundT1 = getElement(eqFile, "DCTerminal", "T_DCGRND");
        assertEquals("DC ground 1", getAttribute(dcGroundT1, "IdentifiedObject.name"));
        assertEquals("DCGRND", getResource(dcGroundT1, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_G", getResource(dcGroundT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(dcGroundT1, "ACDCTerminal.sequenceNumber"));

        // DCLineSegment
        String dcLine = getElement(eqFile, "DCLineSegment", "DCL_1");
        assertEquals("DC line 1", getAttribute(dcLine, "IdentifiedObject.name"));
        assertEquals("0.5", getAttribute(dcLine, "DCLineSegment.resistance"));

        String dcLineT1 = getElement(eqFile, "DCTerminal", "T_DCL_1_1");
        assertEquals("DC line 1 1", getAttribute(dcLineT1, "IdentifiedObject.name"));
        assertEquals("DCL_1", getResource(dcLineT1, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_1", getResource(dcLineT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(dcLineT1, "ACDCTerminal.sequenceNumber"));

        String dcLineT2 = getElement(eqFile, "DCTerminal", "T_DCL_1_2");
        assertEquals("DC line 1 2", getAttribute(dcLineT2, "IdentifiedObject.name"));
        assertEquals("DCL_1", getResource(dcLineT2, "DCTerminal.DCConductingEquipment"));
        assertEquals("DCN_2_1", getResource(dcLineT2, "DCBaseTerminal.DCNode"));
        assertEquals("2", getAttribute(dcLineT2, "ACDCTerminal.sequenceNumber"));

        // CsConverter
        String lcc = getElement(eqFile, "CsConverter", "CSC_1_1");
        assertEquals("Current source converter 1 1", getAttribute(lcc, "IdentifiedObject.name"));
        assertEquals("250", getAttribute(lcc, "ACDCConverter.ratedUdc"));
        assertEquals("0.1", getAttribute(lcc, "ACDCConverter.idleLoss"));
        assertEquals("0.0003", getAttribute(lcc, "ACDCConverter.switchingLoss"));
        assertEquals("0.6", getAttribute(lcc, "ACDCConverter.resistiveLoss"));
        assertEquals("T_ACL_1_1", getResource(lcc, "ACDCConverter.PccTerminal"));
        assertEquals("DCCU_1_1", getResource(lcc, "Equipment.EquipmentContainer"));

        String lccT1 = getElement(eqFile, "ACDCConverterDCTerminal", "T_CSC_1_1_3");
        assertEquals("Current source converter 1 1 1", getAttribute(lccT1, "IdentifiedObject.name"));
        assertEquals("CSC_1_1", getResource(lccT1, "ACDCConverterDCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_1P", getResource(lccT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(lccT1, "ACDCTerminal.sequenceNumber"));

        String lccT2 = getElement(eqFile, "ACDCConverterDCTerminal", "T_CSC_1_1_4");
        assertEquals("Current source converter 1 1 2", getAttribute(lccT2, "IdentifiedObject.name"));
        assertEquals("CSC_1_1", getResource(lccT2, "ACDCConverterDCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_1N", getResource(lccT2, "DCBaseTerminal.DCNode"));
        assertEquals("2", getAttribute(lccT2, "ACDCTerminal.sequenceNumber"));

        // VsConverter
        String vsc = getElement(eqFile, "VsConverter", "VSC_1_2");
        assertEquals("Voltage source converter 1 2", getAttribute(vsc, "IdentifiedObject.name"));
        assertEquals("500", getAttribute(vsc, "ACDCConverter.ratedUdc"));
        assertEquals("0.2", getAttribute(vsc, "ACDCConverter.idleLoss"));
        assertEquals("0.0006", getAttribute(vsc, "ACDCConverter.switchingLoss"));
        assertEquals("1.2", getAttribute(vsc, "ACDCConverter.resistiveLoss"));
        assertEquals("DCCU_1_2", getResource(vsc, "Equipment.EquipmentContainer"));
        assertEquals("T_PTE_1_2_1", getResource(vsc, "ACDCConverter.PccTerminal"));
        assertEquals("VSC_1_2_VSC_RCC", getResource(vsc, "VsConverter.CapabilityCurve"));

        String vscT1 = getElement(eqFile, "ACDCConverterDCTerminal", "T_VSC_1_2_2");
        assertEquals("Voltage source converter 1 2 1", getAttribute(vscT1, "IdentifiedObject.name"));
        assertEquals("VSC_1_2", getResource(vscT1, "ACDCConverterDCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_2P", getResource(vscT1, "DCBaseTerminal.DCNode"));
        assertEquals("1", getAttribute(vscT1, "ACDCTerminal.sequenceNumber"));

        String vscT2 = getElement(eqFile, "ACDCConverterDCTerminal", "T_VSC_1_2_3");
        assertEquals("Voltage source converter 1 2 2", getAttribute(vscT2, "IdentifiedObject.name"));
        assertEquals("VSC_1_2", getResource(vscT2, "ACDCConverterDCTerminal.DCConductingEquipment"));
        assertEquals("DCN_1_2N", getResource(vscT2, "DCBaseTerminal.DCNode"));
        assertEquals("2", getAttribute(vscT2, "ACDCTerminal.sequenceNumber"));

        // VsCapabilityCurve
        String vscc = getElement(eqFile, "VsCapabilityCurve", "VSC_1_2_VSC_RCC");
        assertEquals("RCC_Voltage source converter 1 2", getAttribute(vscc, "IdentifiedObject.name"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#CurveStyle.straightLineYValues", getResource(vscc, "Curve.curveStyle"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.W", getResource(vscc, "Curve.xUnit"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.VAr", getResource(vscc, "Curve.y1Unit"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.VAr", getResource(vscc, "Curve.y2Unit"));

        String cd0 = getElement(eqFile, "CurveData", "VSC_1_2_VSC_0_RCC_CP");
        assertEquals("-525", getAttribute(cd0, "CurveData.xvalue"));
        assertEquals("-100", getAttribute(cd0, "CurveData.y1value"));
        assertEquals("100", getAttribute(cd0, "CurveData.y2value"));
        assertEquals("VSC_1_2_VSC_RCC", getResource(cd0, "CurveData.Curve"));
    }

    @Test
    void dcNetworkSshExportTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole with VSC, LCC, some DCSwitches and a DCGround.
        // CGMES network:
        //   DCTerminal connected status and ACDCConverter state data have been correctly exported.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");
        String sshFile = writeCgmesProfile(network, "SSH", tmpDir, new Properties());

        // Verify all dc objects have been exported.
        assertEquals(31, getElementCount(sshFile, "DCTerminal"));
        assertEquals(8, getElementCount(sshFile, "ACDCConverterDCTerminal"));
        assertEquals(2, getElementCount(sshFile, "VsConverter"));
        assertEquals(2, getElementCount(sshFile, "CsConverter"));

        // Verify dc objects properties.
        // DCTerminal
        String notConnectedDcTerminal = getElement(sshFile, "DCTerminal", "T_DCSW_1_1_1");
        assertEquals("false", getAttribute(notConnectedDcTerminal, "ACDCTerminal.connected"));

        String connectedDcTerminal = getElement(sshFile, "DCTerminal", "T_DCSW_1_1P_1");
        assertEquals("true", getAttribute(connectedDcTerminal, "ACDCTerminal.connected"));

        // ACDCConverterDCTerminal
        String acDcConverterDcTerminal = getElement(sshFile, "ACDCConverterDCTerminal", "T_CSC_1_1_3");
        assertEquals("true", getAttribute(acDcConverterDcTerminal, "ACDCTerminal.connected"));

        // CsConverter
        String lcc1 = getElement(sshFile, "CsConverter", "CSC_1_1");
        assertEquals("250", getAttribute(lcc1, "ACDCConverter.targetPpcc"));
        assertEquals("0", getAttribute(lcc1, "ACDCConverter.targetUdc"));
        assertEquals("0", getAttribute(lcc1, "ACDCConverter.p"));
        assertEquals("0", getAttribute(lcc1, "ACDCConverter.q"));
        assertEquals("0", getAttribute(lcc1, "CsConverter.targetAlpha"));
        assertEquals("0", getAttribute(lcc1, "CsConverter.targetGamma"));
        assertEquals("0", getAttribute(lcc1, "CsConverter.targetIdc"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#CsOperatingModeKind.rectifier", getResource(lcc1, "CsConverter.operatingMode"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#CsPpccControlKind.activePower", getResource(lcc1, "CsConverter.pPccControl"));

        String lcc2 = getElement(sshFile, "CsConverter", "CSC_2_1");
        assertEquals("0", getAttribute(lcc2, "ACDCConverter.targetPpcc"));
        assertEquals("248.5", getAttribute(lcc2, "ACDCConverter.targetUdc"));
        assertEquals("0", getAttribute(lcc2, "ACDCConverter.p"));
        assertEquals("0", getAttribute(lcc2, "ACDCConverter.q"));
        assertEquals("0", getAttribute(lcc2, "CsConverter.targetAlpha"));
        assertEquals("0", getAttribute(lcc2, "CsConverter.targetGamma"));
        assertEquals("0", getAttribute(lcc2, "CsConverter.targetIdc"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#CsOperatingModeKind.inverter", getResource(lcc2, "CsConverter.operatingMode"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#CsPpccControlKind.dcVoltage", getResource(lcc2, "CsConverter.pPccControl"));

        // VsConverter
        String vsc1 = getElement(sshFile, "VsConverter", "VSC_1_2");
        assertEquals("500", getAttribute(vsc1, "ACDCConverter.targetPpcc"));
        assertEquals("0", getAttribute(vsc1, "ACDCConverter.targetUdc"));
        assertEquals("0", getAttribute(vsc1, "ACDCConverter.p"));
        assertEquals("0", getAttribute(vsc1, "ACDCConverter.q"));
        assertEquals("0", getAttribute(vsc1, "VsConverter.droop"));
        assertEquals("0", getAttribute(vsc1, "VsConverter.droopCompensation"));
        assertEquals("0", getAttribute(vsc1, "VsConverter.qShare"));
        assertEquals("1", getAttribute(vsc1, "VsConverter.targetQpcc"));
        assertEquals("0", getAttribute(vsc1, "VsConverter.targetUpcc"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#VsPpccControlKind.pPcc", getResource(vsc1, "VsConverter.pPccControl"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#VsQpccControlKind.reactivePcc", getResource(vsc1, "VsConverter.qPccControl"));

        String vsc2 = getElement(sshFile, "VsConverter", "VSC_2_2");
        assertEquals("0", getAttribute(vsc2, "ACDCConverter.targetPpcc"));
        assertEquals("497", getAttribute(vsc2, "ACDCConverter.targetUdc"));
        assertEquals("0", getAttribute(vsc2, "ACDCConverter.p"));
        assertEquals("0", getAttribute(vsc2, "ACDCConverter.q"));
        assertEquals("0", getAttribute(vsc2, "VsConverter.droop"));
        assertEquals("0", getAttribute(vsc2, "VsConverter.droopCompensation"));
        assertEquals("0", getAttribute(vsc2, "VsConverter.qShare"));
        assertEquals("0", getAttribute(vsc2, "VsConverter.targetQpcc"));
        assertEquals("400", getAttribute(vsc2, "VsConverter.targetUpcc"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#VsPpccControlKind.udc", getResource(vsc2, "VsConverter.pPccControl"));
        assertEquals("http://iec.ch/TC57/2013/CIM-schema-cim16#VsQpccControlKind.voltagePcc", getResource(vsc2, "VsConverter.qPccControl"));
    }

    @Test
    void dcNetworkTpExportTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole with more DCNode than DCBus (node-breaker topology model).
        // CGMES network:
        //   DCTerminal and ACDCConverterDCTerminal referencing a DCTopologicalNode.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");
        String tpFile = writeCgmesProfile(network, "TP", tmpDir, new Properties());

        // Verify all dc objects have been exported.
        assertEquals(31, getElementCount(tpFile, "DCTerminal"));
        assertEquals(8, getElementCount(tpFile, "ACDCConverterDCTerminal"));
        assertEquals(6, getElementCount(tpFile, "DCTopologicalNode"));

        // Verify dc objects properties.
        // DCTerminal
        String closedDcSwitchDcTerminal1 = getElement(tpFile, "DCTerminal", "T_DCSW_1_1P_1");
        String closedDcSwitchDcTerminal2 = getElement(tpFile, "DCTerminal", "T_DCSW_1_1P_2");
        assertEquals("DCN_1_1P_dcBus", getResource(closedDcSwitchDcTerminal1, "DCBaseTerminal.DCTopologicalNode"));
        assertEquals("DCN_1_1P_dcBus", getResource(closedDcSwitchDcTerminal2, "DCBaseTerminal.DCTopologicalNode"));

        String openDcSwitchDcTerminal1 = getElement(tpFile, "DCTerminal", "T_DCSW_1_1_1");
        String openDcSwitchDcTerminal2 = getElement(tpFile, "DCTerminal", "T_DCSW_1_1_2");
        assertEquals("DCN_1_1_dcBus", getResource(openDcSwitchDcTerminal1, "DCBaseTerminal.DCTopologicalNode"));
        assertEquals("DCN_1_1P_dcBus", getResource(openDcSwitchDcTerminal2, "DCBaseTerminal.DCTopologicalNode"));

        // ACDCConverterDCTerminal
        String acDcConverterDcTerminal = getElement(tpFile, "ACDCConverterDCTerminal", "T_CSC_1_1_3");
        assertEquals("DCN_1_1P_dcBus", getResource(acDcConverterDcTerminal, "DCBaseTerminal.DCTopologicalNode"));

        // DCTopologicalNode
        String dcTopologicalNode = getElement(tpFile, "DCTopologicalNode", "DCN_1_1P_dcBus");
        assertEquals("DC node 1 1P", getAttribute(dcTopologicalNode, "IdentifiedObject.name"));
    }

    @Test
    void dcNetworkSvExportTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole.
        // CGMES network:
        //   DCTerminal and ACDCConverterDCTerminal referencing a DCTopologicalNode.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");

        // Set some Idc and Udc values (simulate that a load flow has been run) and export SV.
        LineCommutatedConverter lcc = network.getLineCommutatedConverter("CSC_1_1");
        lcc.getDcTerminal1().setI(1000);
        lcc.getDcTerminal1().getDcNode().setV(250);
        lcc.getDcTerminal2().getDcNode().setV(0);

        VoltageSourceConverter vsc = network.getVoltageSourceConverter("VSC_1_2");
        vsc.getDcTerminal1().setI(1000);
        vsc.getDcTerminal1().getDcNode().setV(0);
        vsc.getDcTerminal2().getDcNode().setV(-500);

        String svFile = writeCgmesProfile(network, "SV", tmpDir, new Properties());

        // Verify all dc objects have been exported.
        assertEquals(2, getElementCount(svFile, "CsConverter"));
        assertEquals(2, getElementCount(svFile, "VsConverter"));

        // Verify dc objects properties.
        // CsConverter
        String csConverter = getElement(svFile, "CsConverter", "CSC_1_1");
        assertEquals("1", getAttribute(csConverter, "ACDCConverter.poleLossP"));
        assertEquals("1000", getAttribute(csConverter, "ACDCConverter.idc"));
        assertEquals("0", getAttribute(csConverter, "ACDCConverter.uc"));
        assertEquals("250", getAttribute(csConverter, "ACDCConverter.udc"));
        assertEquals("0", getAttribute(csConverter, "CsConverter.alpha"));
        assertEquals("0", getAttribute(csConverter, "CsConverter.gamma"));

        // VsConverter
        String vsConverter = getElement(svFile, "VsConverter", "VSC_1_2");
        assertEquals("2", getAttribute(vsConverter, "ACDCConverter.poleLossP"));
        assertEquals("1000", getAttribute(vsConverter, "ACDCConverter.idc"));
        assertEquals("0", getAttribute(vsConverter, "ACDCConverter.uc"));
        assertEquals("500", getAttribute(vsConverter, "ACDCConverter.udc"));
        assertEquals("0", getAttribute(vsConverter, "VsConverter.delta"));
        assertEquals("0", getAttribute(vsConverter, "VsConverter.uf"));
    }

    @Test
    void dcNetworkBusBranchImportTest() {
        // CGMES network:
        //   A bus-branch HVDC bipole.
        // IIDM network:
        //   CGMES DCTopologicalNode have been imported into IIDM DcNode. Each DcNode has its own DcBus.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "dc_bus_branch_EQ.xml", "dc_bus_branch_TP.xml");

        // Verify all dc objects have been imported.
        assertEquals(6, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(2, network.getLineCommutatedConverterCount());

        // Since it is a bus-branch topology model, there is as many DcNode as DcBus.
        assertEquals(network.getDcNodeCount(), network.getDcBusCount());
    }

    @Test
    void dcNetworkBusBranchExportTest() throws IOException {
        // IIDM network:
        //   A node-breaker HVDC bipole.
        // CGMES network:
        //   A bus-branch HVDC bipole.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");
        assertEquals(12, network.getDcSwitchCount());
        assertEquals(14, network.getDcNodeCount());
        assertEquals(6, network.getDcBusCount());

        // In CIM16 bus-branch export, there is no DCNode and no DCSwitch.
        Properties exportParams = new Properties();
        exportParams.put(CgmesExport.TOPOLOGY_KIND, CgmesTopologyKind.BUS_BRANCH.name());
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);
        assertEquals(0, getElementCount(eqFile, "DCBreaker"));
        assertEquals(0, getElementCount(eqFile, "DCDisconnector"));
        assertEquals(0, getElementCount(eqFile, "DCNode"));
        assertFalse(eqFile.contains("DCBaseTerminal.DCNode"));

        // In CIM100 bus-branch export, DCNodes come from IIDM DcBuses.
        exportParams.put(CgmesExport.CIM_VERSION, "100");
        eqFile = writeCgmesProfile(network, "EQ", tmpDir, exportParams);
        assertEquals(6, getElementCount(eqFile, "DCNode"));
    }

}
