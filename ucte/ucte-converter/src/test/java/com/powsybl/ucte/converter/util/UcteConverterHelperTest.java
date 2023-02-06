/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter.util;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.ucte.converter.UcteImporter;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author Abdelsalem HEDHILI  {@literal <abdelsalem.hedhili at rte-france.com>}
 */
public class UcteConverterHelperTest {

    private static Network reference;

    @BeforeClass
    public static void setup() {
        ReadOnlyDataSource dataSource = new ResourceDataSource("expectedExport", new ResourceSet("/", "expectedExport.uct"));
        reference = new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

    @Test
    public void calculatePhaseDuTest() {
        assertEquals(2.0000, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
        assertNotEquals(2.0001, calculatePhaseDu(reference.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")), 0.00001);
    }

    @Test
    public void calculateSymmAngleDuTest() {
        assertEquals(1.573, calculateSymmAngleDu(reference.getTwoWindingsTransformer("ZABCD221 ZEFGH221 1")), 0.0001);
    }

    @Test
    public void calculateAsymmAngleDuTest() {
        assertEquals(1.000, calculateAsymmAngleDu(reference.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1")), 0.0001);
    }

    @Test
    public void calculateAsymmAngleThetaTest() {
        assertEquals(60.0, calculateAsymmAngleTheta(reference.getTwoWindingsTransformer("HDDDDD2  HCCCCC1  1")), 0.0001);
    }
}
