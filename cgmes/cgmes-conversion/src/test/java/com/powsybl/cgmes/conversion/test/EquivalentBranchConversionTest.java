/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.networkModel;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import com.powsybl.cgmes.conversion.Conversion;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class EquivalentBranchConversionTest {

    @Test
    void microGridBaseCaseBEEquivalentBranchWithDifferentNominalsTest() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranchWithDifferentNominals(), config);

        Line lineBaseVoltage1 = n.getLine("ffbabc27-1ccd-4fdc-b037-e341706c8d29");

        assertEquals(5.087377777778, lineBaseVoltage1.getR(), 1.0e-6);
        assertEquals(69.422222222222, lineBaseVoltage1.getX(), 1.0e-6);
        assertEquals(-0.000023332348, lineBaseVoltage1.getG1(), 1.0e-6);
        assertEquals(0.000318392599, lineBaseVoltage1.getB1(), 1.0e-6);
        assertEquals(0.000023862628, lineBaseVoltage1.getG2(), 1.0e-6);
        assertEquals(-0.000325628794, lineBaseVoltage1.getB2(), 1.0e-6);

        Line lineBaseVoltage2 = n.getLine("b58bf21a-096a-4dae-9a01-3f03b60c24c7");

        assertEquals(1.978977272727, lineBaseVoltage2.getR(), 1.0e-6);
        assertEquals(34.977272727273, lineBaseVoltage2.getX(), 1.0e-6);
        assertEquals(-0.000035831779, lineBaseVoltage2.getG1(), 1.0e-6);
        assertEquals(0.000633305865, lineBaseVoltage2.getB1(), 1.0e-6);
        assertEquals(0.000036646138, lineBaseVoltage2.getG2(), 1.0e-6);
        assertEquals(-0.000647699180, lineBaseVoltage2.getB2(), 1.0e-6);

        DanglingLine dl = n.getDanglingLine("a16b4a6c-70b1-4abf-9a9d-bd0fa47f9fe4");
        assertEquals(4.6, dl.getR(), 1.0e-6);
        assertEquals(69.0, dl.getX(), 1.0e-6);
        assertEquals(0.0, dl.getG(), 1.0e-6);
        assertEquals(0.0, dl.getB(), 1.0e-6);
    }

    @Test
    void microGridBaseCaseBEEquivalentBranchWithZeroImpedanceTest() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBEEquivalentBranchWithZeroImpedanceInsideVoltageLevel(), config);

        Switch sw = n.getSwitch("b58bf21a-096a-4dae-9a01-3f03b60c24c7");
        assertTrue(sw.isFictitious());
    }
}
