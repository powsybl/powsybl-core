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

    @Test
    void exportDcNetworkEqTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole with VSC, LCC, some DCSwitches and a DCGround.
        // CGMES network:
        //   All objects (nodes, switches, grounds, lines, converters) have been exported in their dedicated class.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml");
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, new Properties());

        // Verify all objects have been exported.
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

        // Verify objects properties.
        // DCNode.
        String dcNode11 = getElement(eqFile, "DCNode", "DCN_1_1");
        assertTrue(dcNode11.contains("<cim:IdentifiedObject.name>DC node 1 1</cim:IdentifiedObject.name>"));

        // DCBreaker.
        String dcBreaker = getElement(eqFile, "DCBreaker", "DCSW_1_1");
        assertTrue(dcBreaker.contains("<cim:IdentifiedObject.name>DC breaker 1 1</cim:IdentifiedObject.name>"));

        String dcBreakerT1 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1_1");
        assertTrue(dcBreakerT1.contains("<cim:IdentifiedObject.name>DC breaker 1 1 1</cim:IdentifiedObject.name>"));
        assertTrue(dcBreakerT1.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCSW_1_1\"/>"));
        assertTrue(dcBreakerT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_1\"/>"));
        assertTrue(dcBreakerT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        String dcBreakerT2 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1_2");
        assertTrue(dcBreakerT2.contains("<cim:IdentifiedObject.name>DC breaker 1 1 2</cim:IdentifiedObject.name>"));
        assertTrue(dcBreakerT2.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCSW_1_1\"/>"));
        assertTrue(dcBreakerT2.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_G\"/>"));
        assertTrue(dcBreakerT2.contains("<cim:ACDCTerminal.sequenceNumber>2</cim:ACDCTerminal.sequenceNumber>"));

        // DCDisconnector.
        String dcDisconnector = getElement(eqFile, "DCDisconnector", "DCSW_1_1P");
        assertTrue(dcDisconnector.contains("<cim:IdentifiedObject.name>DC disconnector 1 1P</cim:IdentifiedObject.name>"));

        String dcDisconnectorT1 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1P_1");
        assertTrue(dcDisconnectorT1.contains("<cim:IdentifiedObject.name>DC disconnector 1 1P 1</cim:IdentifiedObject.name>"));
        assertTrue(dcDisconnectorT1.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCSW_1_1P\"/>"));
        assertTrue(dcDisconnectorT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_1P\"/>"));
        assertTrue(dcDisconnectorT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        String dcDisconnectorT2 = getElement(eqFile, "DCTerminal", "T_DCSW_1_1P_2");
        assertTrue(dcDisconnectorT2.contains("<cim:IdentifiedObject.name>DC disconnector 1 1P 2</cim:IdentifiedObject.name>"));
        assertTrue(dcDisconnectorT2.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCSW_1_1P\"/>"));
        assertTrue(dcDisconnectorT2.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_G\"/>"));
        assertTrue(dcDisconnectorT2.contains("<cim:ACDCTerminal.sequenceNumber>2</cim:ACDCTerminal.sequenceNumber>"));

        // DCGround.
        String dcGround = getElement(eqFile, "DCGround", "DCGRND");
        assertTrue(dcGround.contains("<cim:IdentifiedObject.name>DC ground</cim:IdentifiedObject.name>"));
        assertTrue(dcGround.contains("<cim:DCGround.r>0.1</cim:DCGround.r>"));

        String dcGroundT1 = getElement(eqFile, "DCTerminal", "T_DCGRND");
        assertTrue(dcGroundT1.contains("<cim:IdentifiedObject.name>DC ground 1</cim:IdentifiedObject.name>"));
        assertTrue(dcGroundT1.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCGRND\"/>"));
        assertTrue(dcGroundT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_G\"/>"));
        assertTrue(dcGroundT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        // DCLineSegment.
        String dcLine = getElement(eqFile, "DCLineSegment", "DCL_1");
        assertTrue(dcLine.contains("<cim:IdentifiedObject.name>DC line 1</cim:IdentifiedObject.name>"));
        assertTrue(dcLine.contains("<cim:DCLineSegment.resistance>0.5</cim:DCLineSegment.resistance>"));

        String dcLineT1 = getElement(eqFile, "DCTerminal", "T_DCL_1_1");
        assertTrue(dcLineT1.contains("<cim:IdentifiedObject.name>DC line 1 1</cim:IdentifiedObject.name>"));
        assertTrue(dcLineT1.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCL_1\"/>"));
        assertTrue(dcLineT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_1\"/>"));
        assertTrue(dcLineT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        String dcLineT2 = getElement(eqFile, "DCTerminal", "T_DCL_1_2");
        assertTrue(dcLineT2.contains("<cim:IdentifiedObject.name>DC line 1 2</cim:IdentifiedObject.name>"));
        assertTrue(dcLineT2.contains("<cim:DCTerminal.DCConductingEquipment rdf:resource=\"#_DCL_1\"/>"));
        assertTrue(dcLineT2.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_2_1\"/>"));
        assertTrue(dcLineT2.contains("<cim:ACDCTerminal.sequenceNumber>2</cim:ACDCTerminal.sequenceNumber>"));

        // CsConverter.
        String lcc = getElement(eqFile, "CsConverter", "CSC_1_1");
        assertTrue(lcc.contains("<cim:IdentifiedObject.name>Current source converter 1 1</cim:IdentifiedObject.name>"));
        assertTrue(lcc.contains("<cim:ACDCConverter.ratedUdc>250</cim:ACDCConverter.ratedUdc>"));
        assertTrue(lcc.contains("<cim:ACDCConverter.idleLoss>0.1</cim:ACDCConverter.idleLoss>"));
        assertTrue(lcc.contains("<cim:ACDCConverter.switchingLoss>0.0003</cim:ACDCConverter.switchingLoss>"));
        assertTrue(lcc.contains("<cim:ACDCConverter.resistiveLoss>0.6</cim:ACDCConverter.resistiveLoss>"));
        assertTrue(lcc.contains("<cim:ACDCConverter.PccTerminal rdf:resource=\"#_T_ACL_1_1\"/>"));
        assertTrue(lcc.contains("<cim:Equipment.EquipmentContainer rdf:resource=\"#_CSC_1_1_LCC_DCCU_250.0\"/>"));

        String lccT1 = getElement(eqFile, "ACDCConverterDCTerminal", "T_CSC_1_1_3");
        assertTrue(lccT1.contains("<cim:IdentifiedObject.name>Current source converter 1 1 1</cim:IdentifiedObject.name>"));
        assertTrue(lccT1.contains("<cim:ACDCConverterDCTerminal.DCConductingEquipment rdf:resource=\"#_CSC_1_1\"/>"));
        assertTrue(lccT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_1P\"/>"));
        assertTrue(lccT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        String lccT2 = getElement(eqFile, "ACDCConverterDCTerminal", "T_CSC_1_1_4");
        assertTrue(lccT2.contains("<cim:IdentifiedObject.name>Current source converter 1 1 2</cim:IdentifiedObject.name>"));
        assertTrue(lccT2.contains("<cim:ACDCConverterDCTerminal.DCConductingEquipment rdf:resource=\"#_CSC_1_1\"/>"));
        assertTrue(lccT2.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_1N\"/>"));
        assertTrue(lccT2.contains("<cim:ACDCTerminal.sequenceNumber>2</cim:ACDCTerminal.sequenceNumber>"));

        // VsConverter.
        String vsc = getElement(eqFile, "VsConverter", "VSC_1_2");
        assertTrue(vsc.contains("<cim:IdentifiedObject.name>Voltage source converter 1 2</cim:IdentifiedObject.name>"));
        assertTrue(vsc.contains("<cim:ACDCConverter.ratedUdc>500</cim:ACDCConverter.ratedUdc>"));
        assertTrue(vsc.contains("<cim:ACDCConverter.idleLoss>0.2</cim:ACDCConverter.idleLoss>"));
        assertTrue(vsc.contains("<cim:ACDCConverter.switchingLoss>0.0006</cim:ACDCConverter.switchingLoss>"));
        assertTrue(vsc.contains("<cim:ACDCConverter.resistiveLoss>1.2</cim:ACDCConverter.resistiveLoss>"));
        assertTrue(vsc.contains("<cim:Equipment.EquipmentContainer rdf:resource=\"#_VSC_1_2_VSC_DCCU_500.0\"/>"));
        assertTrue(vsc.contains("<cim:ACDCConverter.PccTerminal rdf:resource=\"#_T_PTE_1_2_1\"/>"));
        assertTrue(vsc.contains("<cim:VsConverter.CapabilityCurve rdf:resource=\"#_VSC_1_2_VSC_RCC\"/>"));

        String vscT1 = getElement(eqFile, "ACDCConverterDCTerminal", "T_VSC_1_2_2");
        assertTrue(vscT1.contains("<cim:IdentifiedObject.name>Voltage source converter 1 2 1</cim:IdentifiedObject.name>"));
        assertTrue(vscT1.contains("<cim:ACDCConverterDCTerminal.DCConductingEquipment rdf:resource=\"#_VSC_1_2\"/>"));
        assertTrue(vscT1.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_2P\"/>"));
        assertTrue(vscT1.contains("<cim:ACDCTerminal.sequenceNumber>1</cim:ACDCTerminal.sequenceNumber>"));

        String vscT2 = getElement(eqFile, "ACDCConverterDCTerminal", "T_VSC_1_2_3");
        assertTrue(vscT2.contains("<cim:IdentifiedObject.name>Voltage source converter 1 2 2</cim:IdentifiedObject.name>"));
        assertTrue(vscT2.contains("<cim:ACDCConverterDCTerminal.DCConductingEquipment rdf:resource=\"#_VSC_1_2\"/>"));
        assertTrue(vscT2.contains("<cim:DCBaseTerminal.DCNode rdf:resource=\"#_DCN_1_2N\"/>"));
        assertTrue(vscT2.contains("<cim:ACDCTerminal.sequenceNumber>2</cim:ACDCTerminal.sequenceNumber>"));

        // VsCapabilityCurve.
        String vscc = getElement(eqFile, "VsCapabilityCurve", "VSC_1_2_VSC_RCC");
        assertTrue(vscc.contains("<cim:IdentifiedObject.name>RCC_Voltage source converter 1 2</cim:IdentifiedObject.name>"));
        assertTrue(vscc.contains("<cim:Curve.curveStyle rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#CurveStyle.straightLineYValues\"/>"));
        assertTrue(vscc.contains("<cim:Curve.xUnit rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.W\"/>"));
        assertTrue(vscc.contains("<cim:Curve.y1Unit rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.VAr\"/>"));
        assertTrue(vscc.contains("<cim:Curve.y2Unit rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#UnitSymbol.VAr\"/>"));

        String cd0 = getElement(eqFile, "CurveData", "VSC_1_2_VSC_0_RCC_CP");
        assertTrue(cd0.contains("<cim:CurveData.xvalue>-525</cim:CurveData.xvalue>"));
        assertTrue(cd0.contains("<cim:CurveData.y1value>-100</cim:CurveData.y1value>"));
        assertTrue(cd0.contains("<cim:CurveData.y2value>100</cim:CurveData.y2value>"));
        assertTrue(cd0.contains("<cim:CurveData.Curve rdf:resource=\"#_VSC_1_2_VSC_RCC\"/>"));
    }

    @Test
    void exportDcNetworkSshTest() throws IOException {
        // IIDM network:
        //   A mixed HVDC bipole with VSC, LCC, some DCSwitches and a DCGround.
        // CGMES network:
        //   DCTerminal connected status and ACDCConverter state data have been correctly exported.
        Network network = readCgmesResources(IMPORT_PARAMS, DIR, "mixed_bipole_EQ.xml", "mixed_bipole_SSH.xml");
        String sshFile = writeCgmesProfile(network, "SSH", tmpDir, new Properties());

        // Verify all objects have been exported.
        assertEquals(31, getElementCount(sshFile, "DCTerminal"));
        assertEquals(8, getElementCount(sshFile, "ACDCConverterDCTerminal"));
        assertEquals(2, getElementCount(sshFile, "VsConverter"));
        assertEquals(2, getElementCount(sshFile, "CsConverter"));

        // Verify objects properties.
        // DCTerminal.
        String notConnectedDcTerminal = getElement(sshFile, "DCTerminal", "T_DCSW_1_1_1");
        assertTrue(notConnectedDcTerminal.contains("<cim:ACDCTerminal.connected>false</cim:ACDCTerminal.connected>"));

        String connectedDcTerminal = getElement(sshFile, "DCTerminal", "T_DCSW_1_1P_1");
        assertTrue(connectedDcTerminal.contains("<cim:ACDCTerminal.connected>true</cim:ACDCTerminal.connected>"));

        // ACDCConverterDCTerminal.
        String acDcConverterDcTerminal = getElement(sshFile, "ACDCConverterDCTerminal", "T_CSC_1_1_3");
        assertTrue(acDcConverterDcTerminal.contains("<cim:ACDCTerminal.connected>true</cim:ACDCTerminal.connected>"));

        // CsConverter.
        String lcc1 = getElement(sshFile, "CsConverter", "CSC_1_1");
        assertTrue(lcc1.contains("<cim:ACDCConverter.targetPpcc>250</cim:ACDCConverter.targetPpcc>"));
        assertTrue(lcc1.contains("<cim:ACDCConverter.targetUdc>0</cim:ACDCConverter.targetUdc>"));
        assertTrue(lcc1.contains("<cim:ACDCConverter.p>0</cim:ACDCConverter.p>"));
        assertTrue(lcc1.contains("<cim:ACDCConverter.q>0</cim:ACDCConverter.q>"));
        assertTrue(lcc1.contains("<cim:CsConverter.targetAlpha>0</cim:CsConverter.targetAlpha>"));
        assertTrue(lcc1.contains("<cim:CsConverter.targetGamma>0</cim:CsConverter.targetGamma>"));
        assertTrue(lcc1.contains("<cim:CsConverter.targetIdc>0</cim:CsConverter.targetIdc>"));
        assertTrue(lcc1.contains("<cim:CsConverter.operatingMode rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#CsOperatingModeKind.rectifier\"/>"));
        assertTrue(lcc1.contains("<cim:CsConverter.pPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#CsPpccControlKind.activePower\"/>"));

        String lcc2 = getElement(sshFile, "CsConverter", "CSC_2_1");
        assertTrue(lcc2.contains("<cim:ACDCConverter.targetPpcc>0</cim:ACDCConverter.targetPpcc>"));
        assertTrue(lcc2.contains("<cim:ACDCConverter.targetUdc>248.5</cim:ACDCConverter.targetUdc>"));
        assertTrue(lcc2.contains("<cim:ACDCConverter.p>0</cim:ACDCConverter.p>"));
        assertTrue(lcc2.contains("<cim:ACDCConverter.q>0</cim:ACDCConverter.q>"));
        assertTrue(lcc2.contains("<cim:CsConverter.targetAlpha>0</cim:CsConverter.targetAlpha>"));
        assertTrue(lcc2.contains("<cim:CsConverter.targetGamma>0</cim:CsConverter.targetGamma>"));
        assertTrue(lcc2.contains("<cim:CsConverter.targetIdc>0</cim:CsConverter.targetIdc>"));
        assertTrue(lcc2.contains("<cim:CsConverter.operatingMode rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#CsOperatingModeKind.inverter\"/>"));
        assertTrue(lcc2.contains("<cim:CsConverter.pPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#CsPpccControlKind.dcVoltage\"/>"));

        // VsConverter.
        String vsc1 = getElement(sshFile, "VsConverter", "VSC_1_2");
        assertTrue(vsc1.contains("<cim:ACDCConverter.targetPpcc>500</cim:ACDCConverter.targetPpcc>"));
        assertTrue(vsc1.contains("<cim:ACDCConverter.targetUdc>0</cim:ACDCConverter.targetUdc>"));
        assertTrue(vsc1.contains("<cim:ACDCConverter.p>0</cim:ACDCConverter.p>"));
        assertTrue(vsc1.contains("<cim:ACDCConverter.q>0</cim:ACDCConverter.q>"));
        assertTrue(vsc1.contains("<cim:VsConverter.droop>0</cim:VsConverter.droop>"));
        assertTrue(vsc1.contains("<cim:VsConverter.droopCompensation>0</cim:VsConverter.droopCompensation>"));
        assertTrue(vsc1.contains("<cim:VsConverter.qShare>0</cim:VsConverter.qShare>"));
        assertTrue(vsc1.contains("<cim:VsConverter.targetQpcc>1</cim:VsConverter.targetQpcc>"));
        assertTrue(vsc1.contains("<cim:VsConverter.targetUpcc>0</cim:VsConverter.targetUpcc>"));
        assertTrue(vsc1.contains("<cim:VsConverter.pPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#VsPpccControlKind.pPcc\"/>"));
        assertTrue(vsc1.contains("<cim:VsConverter.qPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#VsQpccControlKind.reactivePcc\"/>"));

        String vsc2 = getElement(sshFile, "VsConverter", "VSC_2_2");
        assertTrue(vsc2.contains("<cim:ACDCConverter.targetPpcc>0</cim:ACDCConverter.targetPpcc>"));
        assertTrue(vsc2.contains("<cim:ACDCConverter.targetUdc>497</cim:ACDCConverter.targetUdc>"));
        assertTrue(vsc2.contains("<cim:ACDCConverter.p>0</cim:ACDCConverter.p>"));
        assertTrue(vsc2.contains("<cim:ACDCConverter.q>0</cim:ACDCConverter.q>"));
        assertTrue(vsc2.contains("<cim:VsConverter.droop>0</cim:VsConverter.droop>"));
        assertTrue(vsc2.contains("<cim:VsConverter.droopCompensation>0</cim:VsConverter.droopCompensation>"));
        assertTrue(vsc2.contains("<cim:VsConverter.qShare>0</cim:VsConverter.qShare>"));
        assertTrue(vsc2.contains("<cim:VsConverter.targetQpcc>0</cim:VsConverter.targetQpcc>"));
        assertTrue(vsc2.contains("<cim:VsConverter.targetUpcc>400</cim:VsConverter.targetUpcc>"));
        assertTrue(vsc2.contains("<cim:VsConverter.pPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#VsPpccControlKind.udc\"/>"));
        assertTrue(vsc2.contains("<cim:VsConverter.qPccControl rdf:resource=\"http://iec.ch/TC57/2013/CIM-schema-cim16#VsQpccControlKind.voltagePcc\"/>"));
    }
}
