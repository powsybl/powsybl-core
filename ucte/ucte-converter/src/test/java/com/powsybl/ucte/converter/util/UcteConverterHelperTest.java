/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.ucte.converter.UcteException;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.math3.complex.Complex;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 */
class UcteConverterHelperTest {

    private static Network reference;

    private static Network reference2;

    @BeforeAll
    static void setup() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("expectedExport", new ResourceSet("/", "expectedExport.uct"));
        reference = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        ReadOnlyDataSource dataSource2 = new ResourceDataSource("expectedExport2", new ResourceSet("/", "expectedExport2.uct"));
        reference2 = new UcteImporter().importData(dataSource2, NetworkFactory.findDefault(), null);
    }

    @Test
    void calculatePhaseDuTest() {
        assertEquals(2.0000, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
        assertNotEquals(2.0001, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
    }

    @Test
    void calculateSymmAngleDuTest() {
        assertEquals(1.573, calculateSymmAngleDu(reference.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1")), 0.0001);
    }

    @Test
    void calculateAsymmAngleDuAndAngleTest() {
        Complex du = calculateAsymmAngleDuAndAngle(reference.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);
        assertEquals(0.9, du.abs(), 0.0001);
        assertEquals(60.0, Math.toDegrees(du.getArgument()), 0.0001);
    }

    @Test
    void calculatePhaseDuTest2() {
        assertEquals(-2.000, calculatePhaseDu(reference2.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
        assertEquals(-1.57, calculateSymmAngleDu(reference2.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1")), 0.00001); // loss of one decimal with sign

        Complex duRef2 = calculateAsymmAngleDuAndAngle(reference2.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);

        double angleExpected = Math.toRadians(-120.0);
        double moduleExpected = -0.900;
        Complex duExpected = new Complex(Math.cos(angleExpected), Math.sin(angleExpected)).multiply(moduleExpected);

        assertEquals(duExpected.getReal(), duRef2.getReal(), 0.0001);
        assertEquals(duExpected.getImaginary(), duRef2.getImaginary(), 0.0001);

        ReadOnlyDataSource dataSource = new ResourceDataSource("expectedExport4", new ResourceSet("/", "expectedExport4.uct"));
        Network reference4 = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);

        Complex duRef4 = calculateAsymmAngleDuAndAngle(reference4.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);

        double angleExpected4 = Math.toRadians(-120.0);
        double moduleExpected4 = 1.833;
        Complex duExpected4 = new Complex(Math.cos(angleExpected4), Math.sin(angleExpected4)).multiply(moduleExpected4);

        assertEquals(duExpected4.getReal(), duRef4.getReal(), 0.0001);
        assertEquals(duExpected4.getImaginary(), duRef4.getImaginary(), 0.0001);

        ReadOnlyDataSource dataSource6 = new ResourceDataSource("expectedExport5", new ResourceSet("/", "expectedExport5.uct"));
        Network reference6 = new UcteImporter().importData(dataSource6, NetworkFactory.findDefault(), null);

        Complex duRef6 = calculateAsymmAngleDuAndAngle(reference6.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1"), false);
        assertEquals(0.990, duRef6.abs(), 0.00001);
        assertEquals(90.00, Math.toDegrees(duRef6.getArgument()), 0.00001); // loss of one decimal with sign
    }

    @Test
    void getOrderCodeTest() {
        assertEquals('1', getOrderCode(0));
        assertEquals('A', getOrderCode(9));
        assertThrows(UcteException.class, () -> {
            getOrderCode(-1);
        });
        assertThrows(UcteException.class, () -> {
            getOrderCode(50);
        });
    }
}
