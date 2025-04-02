/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static com.powsybl.iidm.network.HvdcLine.ConvertersMode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.GridModelReference;

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
    void smallNodeBreakerHvdc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1Catalog.smallNodeBreakerHvdc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1inverter2rectifier(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49261084, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49019608, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 102.0, 122.39999999999999));
    }

    @Test
    void smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2BothConvertersTargetPpcc1rectifier2inverter(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.5, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.5025126, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 100.0, 120.0));
    }

    @Test
    void smallNodeBreakerHvdcDcLine2Inverter1Rectifier2() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcDcLine2Inverter1Rectifier2(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.49751243, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.4950495, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 101.0, 120.0));
    }

    @Test
    void smallNodeBreakerHvdcVscReactiveQPcc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcVscReactiveQPcc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 0.0, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 0.0, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcMissingDCLineSegment() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcMissingDCLineSegment(), config);

        assertEquals(0, n.getHvdcConverterStationCount());
        assertEquals(0, n.getHvdcLineCount());
    }

    @Test
    void smallNodeBreakerHvdcMissingAcDcConverters() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcMissingAcDcConverters(), config);

        assertEquals(0, n.getHvdcConverterStationCount());
        assertEquals(0, n.getHvdcLineCount());
    }

    @Test
    void smallNodeBreakerHvdcNanTargetPpcc() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcNanTargetPpcc(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 0.0, 0.0));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.0, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.0, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 0.0, 0.0));
    }

    @Test
    void smallNodeBreakerHvdcTwoDcLineSegments() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcTwoDcLineSegments(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 5.60575, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcTwoDcLineSegmentsOneTransformer() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcTwoDcLineSegmentsOneTransformer(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 5.60575, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcTwoAcDcConvertersOneDcLineSegment() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcTwoAcDcConvertersOneDcLineSegments(), config);

        assertEquals(6, n.getHvdcConverterStationCount());
        assertEquals(3, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-1", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "7393a68f-c4e6-48dd-9347-543858363fdb", "Conv1b", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));

        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-1", 0.0, -0.75741));
        assertTrue(containsLccConverter(n, "9793118e-5ba1-4a9c-b2e0-db1d15be5913", "Conv2b", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));

        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68f-c4e6-48dd-9347-543858363fdb", "9793118e-5ba1-4a9c-b2e0-db1d15be5913", 2 * 12.3, 63.8, 76.55999999999999));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-1", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine-1",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 2 * 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcWithOneTransformer() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithTransformers(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcWithTwoTransformers() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithTwoTransformers(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcWithDifferentConverterTypes() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithDifferentConverterTypes(), config);

        assertEquals(2, n.getHvdcConverterStationCount());
        assertEquals(1, n.getHvdcLineCount());

        // The other HVDC link has been discarded as the converters have different type

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));
    }

    @Test
    void smallNodeBreakerHvdcWithVsCapabilityCurve() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcWithVsCapabilityCurve(), config);

        assertEquals(4, n.getHvdcConverterStationCount());
        assertEquals(2, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
                "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
                "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));

        assertTrue(containsVsCapabilityCurve(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", ReactiveLimitsKind.CURVE, 3,
                Collections.unmodifiableList(Arrays.asList(-100.0, 0.0, 100.0)),
                Collections.unmodifiableList(Arrays.asList(-200.0, -300.0, -200.0)),
                Collections.unmodifiableList(Arrays.asList(200.0, 300.0, 200.0))));
    }

    @Test
    void smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegments() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegments(), config);

        assertEquals(6, n.getHvdcConverterStationCount());
        assertEquals(3, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb-2s", "Conv1-2s", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", 0.0, 0.8));

        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913-2q", "Conv2-2q", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", 0.0, 0.8));

        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine-2s-2q",
            "7393a68e-c4e6-48dd-9347-543858363fdb-2s", "9793118d-5ba1-4a9c-b2e0-db1d15be5913-2q", 12.3, 0.0, 0.0));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    @Test
    void smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegmentsNoAcConnectionAtOneEnd() throws IOException {
        Conversion.Config config = new Conversion.Config().setEnsureIdAliasUnicity(true);
        Network n = networkModel(CgmesConformity1ModifiedCatalog.smallNodeBreakerHvdcTwoAcDcConvertersTwoDcLineSegmentsNoAcConnectionAtOneEnd(), config);

        assertEquals(6, n.getHvdcConverterStationCount());
        assertEquals(3, n.getHvdcLineCount());

        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb", "Conv1", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, 0.8));
        assertTrue(containsLccConverter(n, "7393a68e-c4e6-48dd-9347-543858363fdb-2s", "Conv1-2s", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", 0.0, 0.8));

        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913", "Conv2", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", 0.0, -0.75741));
        assertTrue(containsLccConverter(n, "9793118d-5ba1-4a9c-b2e0-db1d15be5913-2q", "Conv2-2q", "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", 0.0, 0.8));

        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine",
            "7393a68e-c4e6-48dd-9347-543858363fdb", "9793118d-5ba1-4a9c-b2e0-db1d15be5913", 12.3, 63.8, 76.55999999999999));
        assertTrue(containsHvdcLine(n, "11d10c55-94cc-47e4-8e24-bc5ac4d026c0-2s-2q", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine-2s-2q",
            "7393a68e-c4e6-48dd-9347-543858363fdb-2s", "9793118d-5ba1-4a9c-b2e0-db1d15be5913-2q", 12.3, 0.0, 0.0));

        assertTrue(containsVscConverter(n, "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "VSC2", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32362458, 218.47, 0.0));
        assertTrue(containsVscConverter(n, "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", "VSC1", "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", 0.32467532, 213.54, 0.0));
        assertTrue(containsHvdcLine(n, "d9a49bc9-f4b8-4bfa-9d0f-d18f12f2575b", HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER, "dcLine2",
            "b46bfb8e-7af6-459e-acf3-53a42c943a7c", "b48ce7cf-abf5-413f-bc51-9e1d3103c9bd", 8.3, 154.5, 184.2));
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) throws IOException {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
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

    private void logHvdcConverterStations(Network n) {
        n.getHvdcConverterStationStream().forEach(c -> logHvdcConverterStation(n, c));
    }

    private void logHvdcConverterStation(Network n, HvdcConverterStation<?> hvdcConverterStation) {
        LOG.info("HvdcConverterStation");
        LOG.info("Id: {}", hvdcConverterStation.getId());
        LOG.info("HvdcType: {}", hvdcConverterStation.getHvdcType());
        LOG.info("Name: {}", hvdcConverterStation.getNameOrId());
        LOG.info("HvdcLine Id: {}", hvdcConverterStation.getHvdcLine().getId());
        LOG.info("LossFactor: {}", hvdcConverterStation.getLossFactor());
        if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
            LccConverterStation lccConverterStation = n.getLccConverterStation(hvdcConverterStation.getId());
            LOG.info("PowerFactor: {}", lccConverterStation.getPowerFactor());
        } else if (hvdcConverterStation.getHvdcType() == HvdcConverterStation.HvdcType.VSC) {
            VscConverterStation vscConverterStation = n.getVscConverterStation(hvdcConverterStation.getId());
            LOG.info("voltageSetpoint: {}", vscConverterStation.getVoltageSetpoint());
            LOG.info("reactivePowerSetpoint: {}", vscConverterStation.getReactivePowerSetpoint());
        }
    }

    private void logHvdcLines(Network n) {
        n.getHvdcLineStream().forEach(dc -> logHvdcLine(dc));
    }

    private void logHvdcLine(HvdcLine hvdcLine) {
        LOG.info("HvdcLine");
        LOG.info("Id: {}", hvdcLine.getId());
        LOG.info("convertersMode: {}", hvdcLine.getConvertersMode());
        LOG.info("Name: {}", hvdcLine.getNameOrId());
        LOG.info("converterStationId1: {}", hvdcLine.getConverterStation1().getId());
        LOG.info("converterStationId2: {}", hvdcLine.getConverterStation2().getId());
        LOG.info("R: {}", hvdcLine.getR());
        LOG.info("activePowerSetpoint: {}", hvdcLine.getActivePowerSetpoint());
        LOG.info("MaxP: {}", hvdcLine.getMaxP());
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
