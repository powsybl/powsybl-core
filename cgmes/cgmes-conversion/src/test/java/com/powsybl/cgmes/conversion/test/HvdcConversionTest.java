/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 * @author Romain Courtier {@literal <romain.courtier at rte-france.com>}
 */
class HvdcConversionTest {

    private static final String DIR = "/issues/hvdc/";
    private static final double TOLERANCE = 0.0001;

    @Test
    void simpleHvdcEqTest() {
        // CGMES network:
        //   An EQ file with 2 HVDC lines (monopole, ground return):
        //   - DCLineSegment DCL_12 with CsConverter CSC_1 and CSC_2 (LCC line)
        //   - DCLineSegment DCL_34 with VsConverter VSC_3 and VSC_4 (VSC line)
        // IIDM network:
        //   - HvdcLine DCL_12 with LccConverterStation CSC_1 and CSC_2
        //   - HvdcLine DCL_34 with VscConverterStation VSC_3 and VSC_4
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml");

        // EQ contains the name of equipments and static values (DCLine resistances).
        // Converter's loss factor, power factor, modes and line's active power setpoint and max value get default value.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1", "CSC_2", 4.65, 0.0, 0.0);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, Double.NaN, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, Double.NaN, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0);
    }

    @Test
    void simpleHvdcEqSshTest() {
        // Same test as with EQ only, but this time the SSH is also read.
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml", "simple_hvdc_SSH.xml");

        // SSH provides converter state data (operating kinds, powers and voltages).
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.65, 99.0, 118.8);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void simpleHvdcFullTest() {
        // Same test as with EQ and SSH, but this time the SV is also read (the TP too but it isn't used).
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml", "simple_hvdc_SSH.xml", "simple_hvdc_SV.xml", "simple_hvdc_TP.xml");

        // SV gives losses in converters.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4024, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4008, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.65, 99.8, 118.8);

        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void pPccControlKindTest() {
        // Control kind is active power at Point of Common Coupling on both sides.
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml", "simple_hvdc_Ppcc_SSH.xml", "simple_hvdc_SV.xml", "simple_hvdc_TP.xml");

        // This gives more precise loss and power factor compared to dc voltage control kind at rectifier side.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4016, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4, 0.9119);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.65, 100.0, 120.0);

        // This doesn't change calculations when control kind at rectifier side was already active power at point of common coupling.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0);
    }

    @Test
    void qPccControlKindTest() {
        // Control kind is reactive power at Point of Common Coupling on both sides.
        // This regulation mode exists only for VSC lines.
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml", "simple_hvdc_Qpcc_SSH.xml", "simple_hvdc_SV.xml", "simple_hvdc_TP.xml");

        // Control variable is reactive power at PCC.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 0.0, -22.5);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 0.0, -30.0);
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
        Network network = readCgmesResources(DIR, "simple_hvdc_EQ.xml", "missing_Ppcc_SSH.xml", "simple_hvdc_SV.xml", "simple_hvdc_TP.xml");

        // Loss factor can't be calculated when Ppcc is missing. They are set to 0.0 and conversion goes on with these values.
        assertContainsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251);
        assertContainsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.9119);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.65, 0.0, 0.0);

        // For VSC line, in addition to also set loss factors to 0.0,
        // it's not possible anymore to compute which side is inverter and which is rectifier without P.
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0);
        assertContainsHvdcLine(network, "DCL_34", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0);
    }

    @Test
    void twoDCLineSegmentsTest() {
        // CGMES network:
        //   An EQ file with a VSC HVDC line (monopole, ground return). The pole consists of 2 DCLineSegments in parallel:
        //   - DCLineSegment DCL_34A (r = 3.15).
        //   - DCLineSegment DCL_34B (r = 6.3).
        // IIDM network:
        //   Only 1 HvdcLine (with its 2 VscConverterStation) has been created.
        //   It is electrically equivalent to the 2 DCLineSegments (r = 2.1).
        Network network = readCgmesResources(DIR, "two_DCLineSegments.xml");

        // A single HvdcLine has been created with an equivalent resistance (1/rEquivalent = 1/r34A + 1/r34B).
        assertContainsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34B", 0.0, Double.NaN, 0.0);
        assertContainsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34B", 0.0, Double.NaN, 0.0);
        assertContainsHvdcLine(network, "DCL_34B", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34B", "VSC_3", "VSC_4", 2.1, 0.0, 0.0);

        // The other DCLineSegment identifier is kept as an alias.
        assertEquals("DCL_34A", network.getHvdcLine("DCL_34B").getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCLineSegment2").orElse(""));
    }

    @Test
    void twoAcDcConvertersTest() {
        // CGMES network:
        //   An EQ file with a LCC HVDC line (monopole, ground return), r = 4.65.
        //   On each side, there is 2 ACDCConverter in serie (CSC_1A and CSC_1B on side 1, CSC_2A and CSC_2B on side 2).
        // IIDM network:
        //   Two HvdcLine have been created, each having a LccConverterStation on each side.
        //   The two IIDM HvdcLine have double the resistance of the CGMES DCLineSegment so that it is electrically equivalent.
        Network network = readCgmesResources(DIR, "two_ACDCConverters.xml");

        // HvdcLine DCL_12 (r = 9.3) links LccConverterStation CSC_1A and CSC_2A.
        assertContainsLccConverter(network, "CSC_1A", "Current source converter 1A", "DCL_12", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2A", "Current source converter 2A", "DCL_12", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1A", "CSC_2A", 9.3, 0.0, 0.0);

        // HvdcLine DCL_12-1 (r = 9.3) links LccConverterStation CSC_1B and CSC_2B.
        assertContainsLccConverter(network, "CSC_1B", "Current source converter 1B", "DCL_12-1", 0.0, 0.8);
        assertContainsLccConverter(network, "CSC_2B", "Current source converter 2B", "DCL_12-1", 0.0, 0.8);
        assertContainsHvdcLine(network, "DCL_12-1", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12-1", "CSC_1B", "CSC_2B", 9.3, 0.0, 0.0);
    }

    @Test
    void vscWithCapabilityCurveTest() {
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
}
