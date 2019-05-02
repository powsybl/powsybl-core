/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity.modified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.FileSystem;

import com.powsybl.iidm.network.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.jimfs.Jimfs;
import com.powsybl.cgmes.conformity.test.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.config.PlatformConfig;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1ModifiedConversionTest {

    @BeforeClass
    public static void setUpBeforeClass() {
        catalog = new CgmesConformity1Catalog();
        catalogModified = new CgmesConformity1ModifiedCatalog();
    }

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem();
        platformConfig = new InMemoryPlatformConfig(fileSystem);
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void microBERatioPhaseTabularTest() throws IOException {
        Network network = new CgmesImport(platformConfig)
            .importData(catalogModified.microGridBaseCaseBERatioPhaseTapChangerTabular().dataSource(), null);
        RatioTapChanger rtc = network.getTwoWindingsTransformer("_b94318f6-6d24-4f56-96b9-df2531ad6543")
            .getRatioTapChanger();
        assertEquals(6, rtc.getStepCount());
        // ratio is missing for step 3
        // ratio is defined explicitly as 1.0 for step 4
        // r not defined in step 5
        // x not defined in step 6
        assertEquals(1.0, rtc.getStep(3).getRho(), 0);
        assertEquals(1.0, rtc.getStep(4).getRho(), 0);
        assertEquals(0.0, rtc.getStep(5).getR(), 0);
        assertEquals(0.0, rtc.getStep(6).getX(), 0);

        PhaseTapChanger ptc = network.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0")
            .getPhaseTapChanger();
        // r,x not defined for step 1
        // ratio not defined for any step
        assertEquals(4, ptc.getStepCount());
        assertEquals(0.0, ptc.getStep(1).getR(), 0);
        assertEquals(0.0, ptc.getStep(1).getX(), 0);
        for (int k = 1; k <= 4; k++) {
            assertEquals(1.0, ptc.getStep(k).getRho(), 0);
        }
    }

    @Test
    public void microBEReactiveCapabilityCurve() {
        Network network = new CgmesImport(platformConfig)
            .importData(catalogModified.microGridBaseCaseBEReactiveCapabilityCurve().dataSource(), null);
        ReactiveLimits rl = network.getGenerator("_3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.CURVE, rl.getKind());
        ReactiveCapabilityCurve rcc = (ReactiveCapabilityCurve) rl;
        assertEquals(4, rcc.getPointCount());
        assertEquals(-20, rl.getMinQ(-200), 0.001);
        assertEquals(-20, rl.getMinQ(-201), 0.001);
        assertEquals(-20 - (180.0 / 100.0), rl.getMinQ(-199), 0.001);
    }

    @Test
    public void microBEReactiveCapabilityCurveOnePoint() {
        Network network = new CgmesImport(platformConfig)
                .importData(catalogModified.microGridBaseCaseBEReactiveCapabilityCurveOnePoint().dataSource(), null);
        ReactiveLimits rl = network.getGenerator("_3a3b27be-b18b-4385-b557-6735d733baf0").getReactiveLimits();
        assertEquals(ReactiveLimitsKind.MIN_MAX, rl.getKind());
        MinMaxReactiveLimits mm = (MinMaxReactiveLimits) rl;
        assertEquals(-200, mm.getMinQ(), 0);
        assertEquals(200, mm.getMaxQ(), 0);
    }

    @Test
    public void miniNodeBreakerTestLimits() throws IOException {
        // Original test case
        Network network0 = new CgmesImport(platformConfig).importData(catalog.miniNodeBreaker().dataSource(), null);
        // The case has been manually modified to have OperationalLimits
        // defined for Equipment
        Network network1 = new CgmesImport(platformConfig)
            .importData(catalogModified.miniNodeBreakerLimitsforEquipment().dataSource(), null);

        double tol = 0;

        // 1 - PATL Current defined for an Equipment ACTransmissionLine
        // Previous limit for one terminal has been modified to refer to the Equipment
        // In the modified case both ends have to see the same value
        Line l0 = network0.getLine("_1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        Line l1 = network1.getLine("_1e7f52a9-21d0-4ebe-9a8a-b29281d5bfc9");
        assertEquals(525, l0.getCurrentLimits1().getPermanentLimit(), tol);
        assertNull(l0.getCurrentLimits2());
        assertEquals(525, l1.getCurrentLimits1().getPermanentLimit(), tol);
        assertEquals(525, l1.getCurrentLimits2().getPermanentLimit(), tol);

        // 2 - PATL Current defined for an ACTransmissionLine
        // that will be mapped to a DanglingLine in IIDM
        DanglingLine dl0 = network0.getDanglingLine("_f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        DanglingLine dl1 = network1.getDanglingLine("_f32baf36-7ea3-4b6a-9452-71e7f18779f8");
        // In network0 limit is defined for the Terminal
        // In network1 limit is defined for the Equipment
        // In both cases the limit should be mapped to IIDM
        assertEquals(1000, dl0.getCurrentLimits().getPermanentLimit(), tol);
        assertEquals(1000, dl1.getCurrentLimits().getPermanentLimit(), tol);

        // 3 - PATL Current defined for a PowerTransformer, should be rejected
        TwoWindingsTransformer tx0 = network0.getTwoWindingsTransformer("_ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        TwoWindingsTransformer tx1 = network1.getTwoWindingsTransformer("_ceb5d06a-a7ff-4102-a620-7f3ea5fb4a51");
        assertEquals(158, tx0.getCurrentLimits1().getPermanentLimit(), tol);
        assertEquals(1732, tx0.getCurrentLimits2().getPermanentLimit(), tol);
        assertNull(tx1.getCurrentLimits1());
        assertEquals(1732, tx1.getCurrentLimits2().getPermanentLimit(), tol);

        // 4 - PATL Current defined for Switch, will be ignored
        TwoWindingsTransformer tx0s = network0.getTwoWindingsTransformer("_6c89588b-3df5-4120-88e5-26164afb43e9");
        TwoWindingsTransformer tx1s = network1.getTwoWindingsTransformer("_6c89588b-3df5-4120-88e5-26164afb43e9");
        assertEquals(1732, tx0s.getCurrentLimits2().getPermanentLimit(), tol);
        assertNull(tx1s.getCurrentLimits2());
    }

    private static CgmesConformity1Catalog catalog;
    private static CgmesConformity1ModifiedCatalog catalogModified;

    private FileSystem fileSystem;
    private PlatformConfig platformConfig;
}
