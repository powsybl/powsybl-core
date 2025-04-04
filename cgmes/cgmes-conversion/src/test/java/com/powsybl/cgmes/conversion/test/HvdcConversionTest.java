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

import java.util.List;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class HvdcConversionTest {

    private static final String DIR = "/issues/hvdc/";

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
        assertTrue(containsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, 0.8));
        assertTrue(containsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1", "CSC_2", 4.64, 0.0, 0.0));

        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, Double.NaN, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, Double.NaN, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0));
    }

    @Test
    void monopoleEqSshTest() {
        // Same test as with EQ only, but this time the SSH is also read.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_SSH.xml");

        // SSH provides converter state data (operating kinds, powers and voltages).
        assertTrue(containsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251));
        assertTrue(containsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.8));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 99.0, 118.8));

        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0));
    }

    @Test
    void monopoleFullTest() {
        // Same test as with EQ and SSH, but this time the SV is also read (the TP too but it isn't used).
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // SV gives losses in converters.
        assertTrue(containsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4024, -0.8251));
        assertTrue(containsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4008, 0.8));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 99.8, 118.8));

        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 90.0, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0));
    }

    @Test
    void monopoleWithMetallicReturnTest() {
        // CGMES network:
        //   A HVDC monopole with metallic return:
        //   The positive polarity DCLineSegment DCL_34P has r = 4.94.
        //   The negative polarity DCLineSegment DCL_34N has r = 4.98.
        // IIDM network:
        //   HvdcLine DCL_34N with VscConverterStation VSC_3 and VSC_4.
        Network network = readCgmesResources(DIR, "monopole_with_metallic_return.xml");

        // A single HvdcLine has been created with an equivalent resistance.
        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34P", 0.0, Double.NaN, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34P", 0.0, Double.NaN, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34P", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34P", "VSC_3", "VSC_4", 2.48, 0.0, 0.0));

        // The other DCLineSegment identifier is kept as an alias.
        assertEquals("DCL_34N", network.getHvdcLine("DCL_34P").getAliasFromType(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "DCLineSegment2").orElse(""));
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
        assertTrue(containsLccConverter(network, "CSC_1A", "Current source converter 1A", "DCL_12", 0.0, 0.8));
        assertTrue(containsLccConverter(network, "CSC_2A", "Current source converter 2A", "DCL_12", 0.0, 0.8));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12", "CSC_1A", "CSC_2A", 9.28, 0.0, 0.0));

        // HvdcLine DCL_12-1 links LccConverterStation CSC_1B and CSC_2B with an equivalent resistance.
        assertTrue(containsLccConverter(network, "CSC_1B", "Current source converter 1B", "DCL_12-1", 0.0, 0.8));
        assertTrue(containsLccConverter(network, "CSC_2B", "Current source converter 2B", "DCL_12-1", 0.0, 0.8));
        assertTrue(containsHvdcLine(network, "DCL_12-1", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 12-1", "CSC_1B", "CSC_2B", 9.28, 0.0, 0.0));
    }

    @Test
    void pPccControlKindTest() {
        // Control kind is active power at Point of Common Coupling on both sides.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_Ppcc_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // This gives more precise loss and power factor compared to dc voltage control kind at rectifier side.
        assertTrue(containsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.4016, -0.8251));
        assertTrue(containsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.4, 0.9119));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 100.0, 120.0));

        // This doesn't change calculations when control kind at rectifier side was already active power at point of common coupling.
        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 95.0, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 90.0, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0));
    }

    @Test
    void qPccControlKindTest() {
        // Control kind is reactive power at Point of Common Coupling on both sides.
        // This regulation mode exists only for VSC lines.
        Network network = readCgmesResources(DIR, "monopole_EQ.xml", "monopole_Qpcc_SSH.xml", "monopole_SV.xml", "monopole_TP.xml");

        // Control variable is reactive power at PCC.
        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.6, 0.0, -22.5));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.6036, 0.0, -30.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0));
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
        assertTrue(containsLccConverter(network, "CSC_1", "Current source converter 1", "DCL_12", 0.0, -0.8251));
        assertTrue(containsLccConverter(network, "CSC_2", "Current source converter 2", "DCL_12", 0.0, 0.9119));
        assertTrue(containsHvdcLine(network, "DCL_12", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 12", "CSC_1", "CSC_2", 4.64, 0.0, 0.0));

        // For VSC line, in addition to also set loss factors to 0.0,
        // it's not possible anymore to compute which side is inverter and which is rectifier without P.
        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 95.0, 0.0));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 90.0, 0.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_INVERTER_SIDE_2_RECTIFIER, "DC line 34", "VSC_3", "VSC_4", 9.92, 0.0, 0.0));
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
        assertTrue(containsVsCapabilityCurve(network, "VSC_3", ReactiveLimitsKind.CURVE, 3,
                List.of(-100.0, 0.0, 100.0),    // list of P values
                List.of(-25.0, -100.0, -25.0),  // list of Qmin values
                List.of(25.0, 100.0, 25.0)));   // list of Qmax values
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
        assertTrue(containsVscConverter(network, "VSC_3", "Voltage source converter 3", "DCL_34", 0.0, 0.0, -22.5));
        assertTrue(containsVscConverter(network, "VSC_4", "Voltage source converter 4", "DCL_34", 0.0, 0.0, -30.0));
        assertTrue(containsHvdcLine(network, "DCL_34", SIDE_1_RECTIFIER_SIDE_2_INVERTER, "DC line 34", "VSC_3", "VSC_4", 9.92, 100.0, 120.0));

        // VSC_3 regulating terminal is remote, VSC_4 regulating terminal is local.
        assertEquals("PT_3", network.getVscConverterStation("VSC_3").getRegulatingTerminal().getConnectable().getId());
        assertEquals("VSC_4", network.getVscConverterStation("VSC_4").getRegulatingTerminal().getConnectable().getId());
    }

    private boolean containsLccConverter(Network n, String id, String name,
        String hvdcLineId, double lossFactor, double powerFactor) {
        ReferenceHvdcConverter referenceHvdcConverter = new ReferenceHvdcConverter(id, HvdcConverterStation.HvdcType.LCC, name,
            hvdcLineId, lossFactor, powerFactor);
        return containsLccConverter(n, referenceHvdcConverter);
    }

    private boolean containsLccConverter(Network n, ReferenceHvdcConverter referenceConverter) {
        LccConverterStation lccConverter = n.getLccConverterStation(referenceConverter.id);
        if (lccConverter == null) {
            LOG.info("HvdcConverterStation {} not found", referenceConverter.id);
            return false;
        }
        if (referenceConverter.hvdcType != lccConverter.getHvdcType()) {
            LOG.info("Different hvdcType {} reference {}", lccConverter.getHvdcType(), referenceConverter.hvdcType);
            return false;
        }
        if (referenceConverter.name.compareTo(lccConverter.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", lccConverter.getNameOrId(), referenceConverter.name);
            return false;
        }
        if (referenceConverter.hvdcLineId.compareTo(lccConverter.getHvdcLine().getId()) != 0) {
            LOG.info("Different hvdcLineId {} reference {}", lccConverter.getHvdcLine().getId(), referenceConverter.hvdcLineId);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceConverter.lossFactor - lccConverter.getLossFactor()) > tolerance) {
            LOG.info("Different lossFactor {} reference {}", lccConverter.getLossFactor(), referenceConverter.lossFactor);
            return false;
        }
        if (Math.abs(referenceConverter.powerFactor - lccConverter.getPowerFactor()) > tolerance) {
            LOG.info("Different powerFactor {} reference {}", lccConverter.getPowerFactor(), referenceConverter.powerFactor);
            return false;
        }
        return true;
    }

    private boolean containsVscConverter(Network n, String id, String name,
        String hvdcLineId, double lossFactor, double voltageSetpoint, double reactivePowerSetpoint) {
        ReferenceHvdcConverter referenceHvdcConverter = new ReferenceHvdcConverter(id, HvdcConverterStation.HvdcType.VSC, name,
            hvdcLineId, lossFactor, voltageSetpoint, reactivePowerSetpoint);
        return containsVscConverter(n, referenceHvdcConverter);
    }

    private boolean containsVscConverter(Network n, ReferenceHvdcConverter referenceConverter) {
        VscConverterStation vscConverter = n.getVscConverterStation(referenceConverter.id);
        if (vscConverter == null) {
            LOG.info("HvdcConverterStation {} not found", referenceConverter.id);
            return false;
        }
        if (referenceConverter.hvdcType != vscConverter.getHvdcType()) {
            LOG.info("Different hvdcType {} reference {}", vscConverter.getHvdcType(), referenceConverter.hvdcType);
            return false;
        }
        if (referenceConverter.name.compareTo(vscConverter.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", vscConverter.getNameOrId(), referenceConverter.name);
            return false;
        }
        if (referenceConverter.hvdcLineId.compareTo(vscConverter.getHvdcLine().getId()) != 0) {
            LOG.info("Different hvdcLineId {} reference {}", vscConverter.getHvdcLine().getId(), referenceConverter.hvdcLineId);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceConverter.lossFactor - vscConverter.getLossFactor()) > tolerance) {
            LOG.info("Different lossFactor {} reference {}", vscConverter.getLossFactor(), referenceConverter.lossFactor);
            return false;
        }
        if (Math.abs(referenceConverter.voltageSetpoint - vscConverter.getVoltageSetpoint()) > tolerance) {
            LOG.info("Different voltageSetpoint {} reference {}", vscConverter.getVoltageSetpoint(), referenceConverter.voltageSetpoint);
            return false;
        }
        if (Math.abs(referenceConverter.reactivePowerSetpoint - vscConverter.getReactivePowerSetpoint()) > tolerance) {
            LOG.info("Different reactivePowerSetpoint {} reference {}", vscConverter.getReactivePowerSetpoint(), referenceConverter.reactivePowerSetpoint);
            return false;
        }
        return true;
    }

    private boolean containsVsCapabilityCurve(Network n, String id, ReactiveLimitsKind type, int values, List<Double> xValues, List<Double> y1Values, List<Double> y2Values) {
        ReferenceVsCapabilityCurve referenceVsCapabilityCurve = new ReferenceVsCapabilityCurve(type, values, xValues, y1Values, y2Values);
        return containsVsCapabilityCurve(n, id, referenceVsCapabilityCurve);
    }

    private boolean containsVsCapabilityCurve(Network n, String id, ReferenceVsCapabilityCurve referenceVsCapabilityCurve) {
        VscConverterStation vscConverter = n.getVscConverterStation(id);
        if (vscConverter.getReactiveLimits().getKind() != referenceVsCapabilityCurve.type) {
            return false;
        }
        ReactiveCapabilityCurve curve = vscConverter.getReactiveLimits(ReactiveCapabilityCurve.class);
        if (curve == null) {
            return false;
        }
        if (curve.getPointCount() != referenceVsCapabilityCurve.num) {
            return false;
        }
        int i = 0;
        for (ReactiveCapabilityCurve.Point point : curve.getPoints()) {
            if (referenceVsCapabilityCurve.xValues.get(i) != point.getP()) {
                return false;
            }
            if (referenceVsCapabilityCurve.y1Values.get(i) != point.getMinQ()) {
                return false;
            }
            if (referenceVsCapabilityCurve.y2Values.get(i) != point.getMaxQ()) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean containsHvdcLine(Network n, String id, HvdcLine.ConvertersMode convertersMode, String name,
        String converterStation1, String converterStation2, double r, double activePowerSetpoint, double maxP) {
        ReferenceHvdcLine referenceHvdcLine = new ReferenceHvdcLine(id, convertersMode, name,
            converterStation1, converterStation2, r, activePowerSetpoint, maxP);
        return containsHvdcLine(n, referenceHvdcLine);
    }

    private boolean containsHvdcLine(Network n, ReferenceHvdcLine referenceHvdcLine) {
        HvdcLine hvdcLine = n.getHvdcLine(referenceHvdcLine.id);
        if (hvdcLine == null) {
            LOG.info("HvdcLine {} not found", referenceHvdcLine.id);
            return false;
        }
        if (referenceHvdcLine.convertersMode != hvdcLine.getConvertersMode()) {
            LOG.info("Different hvdcType {} reference {}", hvdcLine.getConvertersMode(), referenceHvdcLine.convertersMode);
            return false;
        }
        if (referenceHvdcLine.name.compareTo(hvdcLine.getNameOrId()) != 0) {
            LOG.info("Different name {} reference {}", hvdcLine.getNameOrId(), referenceHvdcLine.name);
            return false;
        }
        if (referenceHvdcLine.converterStation1.compareTo(hvdcLine.getConverterStation1().getId()) != 0) {
            LOG.info("Different converterStation1 {} reference {}", hvdcLine.getConverterStation1().getId(), referenceHvdcLine.converterStation1);
            return false;
        }
        if (referenceHvdcLine.converterStation2.compareTo(hvdcLine.getConverterStation2().getId()) != 0) {
            LOG.info("Different converterStation2 {} reference {}", hvdcLine.getConverterStation2().getId(), referenceHvdcLine.converterStation2);
            return false;
        }
        double tolerance = 0.0001;
        if (Math.abs(referenceHvdcLine.r - hvdcLine.getR()) > tolerance) {
            LOG.info("Different R {} reference {}", hvdcLine.getR(), referenceHvdcLine.r);
            return false;
        }
        if (Math.abs(referenceHvdcLine.activePowerSetpoint - hvdcLine.getActivePowerSetpoint()) > tolerance) {
            LOG.info("Different activePowerSetpoint {} reference {}", hvdcLine.getActivePowerSetpoint(), referenceHvdcLine.activePowerSetpoint);
            return false;
        }
        if (Math.abs(referenceHvdcLine.maxP - hvdcLine.getMaxP()) > tolerance) {
            LOG.info("Different maxP {} reference {}", hvdcLine.getMaxP(), referenceHvdcLine.maxP);
            return false;
        }
        return true;
    }

    static class ReferenceHvdcConverter {
        String id;
        HvdcConverterStation.HvdcType hvdcType;
        String name;
        String hvdcLineId;
        double lossFactor = 0.0;
        double powerFactor = 0.0;
        double voltageSetpoint = 0.0;
        double reactivePowerSetpoint = 0.0;

        ReferenceHvdcConverter(String id, HvdcConverterStation.HvdcType hvdcType, String name, String hvdcLineId,
            double lossFactor, double powerFactor) {
            this.id = id;
            this.hvdcType = hvdcType;
            this.name = name;
            this.hvdcLineId = hvdcLineId;
            this.lossFactor = lossFactor;
            this.powerFactor = powerFactor;
        }

        ReferenceHvdcConverter(String id, HvdcConverterStation.HvdcType hvdcType, String name, String hvdcLineId,
            double lossFactor, double voltageSetpoint, double reactivePowerSetpoint) {
            this.id = id;
            this.hvdcType = hvdcType;
            this.name = name;
            this.hvdcLineId = hvdcLineId;
            this.lossFactor = lossFactor;
            this.voltageSetpoint = voltageSetpoint;
            this.reactivePowerSetpoint = reactivePowerSetpoint;
        }
    }

    static class ReferenceHvdcLine {
        String id;
        HvdcLine.ConvertersMode convertersMode;
        String name;
        String converterStation1;
        String converterStation2;
        double r = 0.0;
        double activePowerSetpoint = 0.0;
        double maxP = 0.0;

        ReferenceHvdcLine(String id, HvdcLine.ConvertersMode convertersMode, String name, String converterStation1,
            String converterStation2, double r, double activePowerSetpoint, double maxP) {
            this.id = id;
            this.convertersMode = convertersMode;
            this.name = name;
            this.converterStation1 = converterStation1;
            this.converterStation2 = converterStation2;
            this.r = r;
            this.activePowerSetpoint = activePowerSetpoint;
            this.maxP = maxP;
        }
    }

    static class ReferenceVsCapabilityCurve {
        ReactiveLimitsKind type;
        int num;
        List<Double> xValues;
        List<Double> y1Values;
        List<Double> y2Values;

        ReferenceVsCapabilityCurve(ReactiveLimitsKind type, int num, List<Double> xValues, List<Double> y1Values, List<Double> y2Values) {
            this.type = type;
            this.num = num;
            this.xValues = xValues;
            this.y1Values = y1Values;
            this.y2Values = y2Values;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(HvdcConversionTest.class);
}
