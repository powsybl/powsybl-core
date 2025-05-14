/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.*;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class HvdcConversionTest extends AbstractSerDeTest {

    private static final String DIR = "/issues/hvdc/";
    private static final double TOLERANCE = 0.001;

    @Test
    void monopoleEqOnlyTest() {
        // CGMES network:
        //   2 HVDC monopole with ground return:
        //   - DCLineSegment DCL_12 with CsConverter CSC_1 and CSC_2 (LCC line).
        //   - DCLineSegment DCL_34 with VsConverter VSC_3 and VSC_4 (VSC line).
        // IIDM network:
        //   - HvdcLine DCL_12 with LccConverterStation CSC_1 and CSC_2.
        //   - HvdcLine DCL_34 with VscConverterStation VSC_3 and VSC_4.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml");

        // EQ contains the name of equipments and static values (DCLine resistances).
        // Converter's loss factor, power factor, modes and line's active power setpoint and max value get default value.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1", "CSC_2", 4.64, 0.0, 0.0);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, Double.NaN, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, Double.NaN, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0);
    }

    @Test
    void monopoleEqSshTest() {
        // Same test as with EQ only, but this time the SSH is also read.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_SSH.xml");

        // SSH provides converter state data (operating kinds, powers). From those we calculate resistive losses.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 99.198, 119.038);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void monopoleFullTest() {
        // Same test as with EQ and SSH, but this time the SV is also read (the TP too but it isn't used).
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // SV gives losses in converters.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4024, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 100.0, 120.0);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6085, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void monopoleWithMetallicReturnTest() {
        // CGMES network:
        //   A HVDC monopole with metallic return:
        //   The positive polarity DCLineSegment DCL_34P has r = 4.94.
        //   The negative polarity DCLineSegment DCL_34N has r = 4.98.
        // IIDM network:
        //   HvdcLine DCL_34P with VscConverterStation VSC_3 and VSC_4.
        Network network = readCgmesResources(DIR, "monopole_with_metallic_return.xml");

        // A single HvdcLine has been created with an equivalent resistance.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34N", 0.0, Double.NaN, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34N", 0.0, Double.NaN, 0.0);
        assertContainsHvdcLine(network, "DCL_34N", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34N", "VSC_3", "VSC_4", 9.92, 0.0, 0.0);

        // The other DCLineSegment identifier is kept as an alias.
        assertEquals("DCL_34P", network.getHvdcLine("DCL_34N").getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCLineSegment2").orElse(""));
    }

    @Test
    void monopoleWith2AcDcConvertersPerUnitTest() {
        // CGMES network:
        //   A HVDC monopole with ground return.
        //   Each converter unit is made of 2 ACDCConverter in series: CSC_1A and CSC_1B, and CSC_2A and CSC_2B.
        //   The DCLineSegment DCL_12 has r = 4.64 and is the closest to CSC_1A and CSC_2A.
        // IIDM network:
        //   - HvdcLine DCL_12 with LccConverterStation CSC_1A and CSC_2A.
        //   - HvdcLine DCL_12-1 with LccConverterStation CSC_1B and CSC_2B.
        Network network = readCgmesResources(DIR, "monopole_with_two_ACDCConverters_per_unit.xml");

        // HvdcLine DCL_12 links LccConverterStation CSC_1A and CSC_2A with an equivalent resistance.
        assertContainsLccConverter(network, "CSC_1A", "Current source converter 1A", "DCL_12", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2A", "Current source converter 2A", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1A", "CSC_2A", 2.32, 0.0, 0.0);

        // HvdcLine DCL_12-1 links LccConverterStation CSC_1B and CSC_2B with an equivalent resistance.
        assertContainsLccConverter(network, "CSC_1B", "Current source converter 1B", "DCL_12-1", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2B", "Current source converter 2B", "DCL_12-1", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12-1", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12-1", "CSC_1B", "CSC_2B", 2.32, 0.0, 0.0);
    }

    @Test
    void bipoleTest() {
        // CGMES network:
        //   A HVDC bipole:
        //   - Positive dc pole is made of DCLineSegment DCL_12P with CsConverter CSC_1P and CSC_2P.
        //   - Negative dc pole is made of DCLineSegment DCL_12N with CsConverter CSC_1N and CSC_2N.
        // IIDM network:
        //   - HvdcLine DCL_12P with LccConverterStation CSC_1P and CSC_2P.
        //   - HvdcLine DCL_12N with LccConverterStation CSC_1N and CSC_2N.
        Network network = readCgmesResources(DIR, "bipole.xml");

        // Positive dc pole.
        assertContainsLccConverter(network, "CSC_1P", "Current source converter 1P", "DCL_12P", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2P", "Current source converter 2P", "DCL_12P", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12P", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12P", "CSC_1P", "CSC_2P", 4.64, 0.0, 0.0);

        // Negative dc pole.
        assertContainsLccConverter(network, "CSC_1N", "Current source converter 1N", "DCL_12N", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2N", "Current source converter 2N", "DCL_12N", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12N", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12N", "CSC_1N", "CSC_2N", 4.64, 0.0, 0.0);
    }

    @Test
    void bipoleWithDedicatedMetallicReturnTest() {
        // CGMES network:
        //   A HVDC bipole with a dedicated metallic return line:
        //   - Positive dc pole is made of DCLineSegment DCL_12P with CsConverter CSC_1P and CSC_2P.
        //   - Negative dc pole is made of DCLineSegment DCL_12N with CsConverter CSC_1N and CSC_2N.
        //   - DCLineSegment DCL_12G is a dedicated metallic return line.
        // IIDM network:
        //   - HvdcLine DCL_12P with LccConverterStation CSC_1P and CSC_2P.
        //   - HvdcLine DCL_12N with LccConverterStation CSC_1N and CSC_2N.
        Network network = readCgmesResources(DIR, "bipole_with_dedicated_metallic_return.xml");

        // Positive dc pole.
        assertContainsLccConverter(network, "CSC_1P", "Current source converter 1P", "DCL_12P", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2P", "Current source converter 2P", "DCL_12P", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12P", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12P", "CSC_1P", "CSC_2P", 4.64, 0.0, 0.0);

        // Negative dc pole.
        assertContainsLccConverter(network, "CSC_1N", "Current source converter 1N", "DCL_12N", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2N", "Current source converter 2N", "DCL_12N", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12N", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12N", "CSC_1N", "CSC_2N", 4.64, 0.0, 0.0);

        // The dedicated metallic return line identifier is kept as an alias.
        assertEquals("DCL_12G", network.getHvdcLine("DCL_12N").getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCLineSegment2").orElse(""));
    }

    @Test
    void pPccControlKindTest() {
        // Control kind is active power at Point of Common Coupling on both sides.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_Ppcc_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // This gives more precise loss and power factor compared to dc voltage control kind at rectifier side.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4024, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4, 0.9119);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 100.0, 120.0);

        // This doesn't change calculations when control kind at rectifier side was already active power at point of common coupling.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6085, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void qPccControlKindTest() {
        // Control kind is reactive power at Point of Common Coupling on both sides.
        // This regulation mode exists only for VSC lines.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_Qpcc_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // Control variable is reactive power at PCC.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 0.0, -22.5);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6085, 0.0, -30.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void inconsistentConverterTypesTest() {
        // CGMES network:
        //   A HVDC line (monopole, ground return) linking a CsConverter and a VsConverter.
        // IIDM network:
        //   Neither HvdcConverterStation nor HvdcLine are created when inputs are inconsistent.
        Network network = readCgmesResources(DIR, "inconsistent_converter_types.xml");

        assertEquals(0, network.getHvdcConverterStationCount());
        assertEquals(0, network.getHvdcLineCount());
    }

    @Test
    void missingDCLineSegmentTest() {
        // CGMES network:
        //   An EQ file with 4 ACDCConverter, but no DCLineSegment joining them.
        // IIDM network:
        //   Neither HvdcConverterStation nor HvdcLine are created when inputs are missing.
        Network network = readCgmesResources(DIR, "missing_DCLineSegment.xml");

        assertEquals(4, network.getSubstationCount());
        assertEquals(0, network.getHvdcConverterStationCount());
        assertEquals(0, network.getHvdcLineCount());
    }

    @Test
    void missingAcDcConverterTest() {
        // CGMES network:
        //   An EQ file with 2 ACDCConverter and 2 DCLineSegment on one side, but no ACDCConverter on the other side.
        // IIDM network:
        //   Neither HvdcConverterStation nor HvdcLine are created when inputs are missing.
        Network network = readCgmesResources(DIR, "missing_ACDCConverter.xml");

        assertEquals(4, network.getSubstationCount());
        assertEquals(0, network.getHvdcConverterStationCount());
        assertEquals(0, network.getHvdcLineCount());
    }

    @Test
    void missingPpccTest() {
        // Control kind is active power at Point of Common Coupling on both sides, but Ppcc values are missing.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_missing_Ppcc_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // Loss factor can't be calculated when Ppcc is missing. They are set to 0.0 and conversion goes on with these values.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.9119);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 0.0, 0.0);

        // For VSC line, in addition to also set loss factors to 0.0,
        // it's not possible anymore to compute which side is inverter and which is rectifier without P.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0);
    }

    @Test
    void vscWithCapabilityCurveTest() throws IOException {
        // CGMES network:
        //   An EQ file with a VSC HVDC line (monopole, ground return).
        //   VsConverter VSC_3 has a VsCapabilityCurve.
        // IIDM network:
        //   HvdcLine and VscConverterStation have been created.
        //   VscConverterStation VSC_3 has ReactiveLimits.
        Network network = readCgmesResources(DIR, "vsCapabilityCurve.xml");

        assertEquals(2, network.getHvdcConverterStationCount());
        assertEquals(1, network.getHvdcLineCount());
        VscConverterStation vscConverter = network.getVscConverterStation("VSC_3");
        assertContainsVsCapabilityCurve(vscConverter, Map.of(
                -100.0, new double[]{-25.0, 25.0},
                0.0, new double[]{-100.0, 100.0},
                100.0, new double[]{-25.0, 25.0}
        ));

        // Curve and CurveData are correctly exported to CGMES.
        String eqFile = writeCgmesProfile(network, "EQ", tmpDir, new Properties());
        assertEquals(1, getElementCount(eqFile, "VsCapabilityCurve"));
        assertTrue(getElement(eqFile, "VsConverter", "VSC_3").contains("VsConverter.CapabilityCurve"));
        assertEquals(3, getElementCount(eqFile, "CurveData"));
        assertContainsCurveData(eqFile, "VSC_3_DCCS_0_RCC_CP", "-100", "-25", "25");
        assertContainsCurveData(eqFile, "VSC_3_DCCS_1_RCC_CP", "0", "-100", "100");
        assertContainsCurveData(eqFile, "VSC_3_DCCS_2_RCC_CP", "100", "-25", "25");
    }

    @Test
    void vscWithRemotePccTerminalTest() {
        // CGMES network:
        //   A VSC HVDC line DCL_34 (monopole, ground return).
        //   VsConverter VSC_3 has a remote pccTerminal, VsConverter VSC_4 hasn't.
        // IIDM network:
        //   HvdcLine and VscConverterStation have been created.
        //   VscConverterStation VSC_3 regulating terminal is a remote one, VSC_4 regulating terminal is local.
        Network network = readCgmesResources(DIR, "remote_pccTerminal_EQ.xml", "remote_pccTerminal_SSH.xml");

        // HVDC line and converters are imported with the same characteristics.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 0.0, -22.5);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 0.0, -30.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);

        // VSC_3 regulating terminal is remote, VSC_4 regulating terminal is local.
        assertEquals("PT_3", network.getVscConverterStation("VSC_3").getRegulatingTerminal().getConnectable().getId());
        assertEquals("VSC_4", network.getVscConverterStation("VSC_4").getRegulatingTerminal().getConnectable().getId());
    }

    private void assertContainsLccConverter(Network network, String id, String name,
                                               String hvdcLineId, double lossFactor, double powerFactor) {
        LccConverterStation lccConverter = network.getLccConverterStation(id);
        assertNotNull(lccConverter);
        assertEquals(name, lccConverter.getNameOrId());
        assertEquals(hvdcLineId, lccConverter.getHvdcLine().getId());
        assertEquals(lossFactor, lccConverter.getLossFactor(), TOLERANCE);
        assertEquals(powerFactor, lccConverter.getPowerFactor(), TOLERANCE);
    }

    private void assertContainsVscConverter(Network network, String id, String name,
        String hvdcLineId, double lossFactor, double voltageSetpoint, double reactivePowerSetpoint) {
        VscConverterStation vscConverter = network.getVscConverterStation(id);
        assertNotNull(vscConverter);
        assertEquals(name, vscConverter.getNameOrId());
        assertEquals(hvdcLineId, vscConverter.getHvdcLine().getId());
        assertEquals(lossFactor, vscConverter.getLossFactor(), TOLERANCE);
        assertEquals(voltageSetpoint, vscConverter.getVoltageSetpoint(), TOLERANCE);
        assertEquals(reactivePowerSetpoint, vscConverter.getReactivePowerSetpoint(), TOLERANCE);
    }

    private void assertContainsHvdcLine(Network network, String id, HvdcLine.ConvertersMode convertersMode, String name,
                                     String converterStation1, String converterStation2, double r, double activePowerSetpoint, double maxP) {
        HvdcLine hvdcLine = network.getHvdcLine(id);
        assertNotNull(hvdcLine);
        assertEquals(convertersMode, hvdcLine.getConvertersMode());
        assertEquals(name, hvdcLine.getNameOrId());
        assertEquals(converterStation1, hvdcLine.getConverterStation1().getId());
        assertEquals(converterStation2, hvdcLine.getConverterStation2().getId());
        assertEquals(r, hvdcLine.getR(), TOLERANCE);
        assertEquals(activePowerSetpoint, hvdcLine.getActivePowerSetpoint(), TOLERANCE);
        assertEquals(maxP, hvdcLine.getMaxP(), TOLERANCE);
    }

    private void assertContainsVsCapabilityCurve(VscConverterStation vscConverter, Map<Double, double[]> values) {
        assertNotNull(vscConverter);
        assertNotNull(vscConverter.getReactiveLimits());
        assertEquals(ReactiveLimitsKind.CURVE, vscConverter.getReactiveLimits().getKind());
        ReactiveCapabilityCurve curve = vscConverter.getReactiveLimits(ReactiveCapabilityCurve.class);
        assertEquals(values.size(), curve.getPointCount());
        for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
            double pValue = point.getP();
            assertTrue(values.containsKey(pValue));
            assertEquals(point.getMinQ(), values.get(pValue)[0]);
            assertEquals(point.getMaxQ(), values.get(pValue)[1]);
        }
    }

    private void assertContainsCurveData(String eqFile, String curveDataId, String xValue, String y1Value, String y2Value) {
        String curveData = getElement(eqFile, "CurveData", curveDataId);
        assertTrue(curveData.contains("<cim:CurveData.xvalue>" + xValue + "</cim:CurveData.xvalue>"));
        assertTrue(curveData.contains("<cim:CurveData.y1value>" + y1Value + "</cim:CurveData.y1value>"));
        assertTrue(curveData.contains("<cim:CurveData.y2value>" + y2Value + "</cim:CurveData.y2value>"));
    }
}
