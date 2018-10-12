/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity.test;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.test.CgmesModelTester;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesConformity1Test {

    @BeforeClass
    public static void setUp() {
        catalog = new CgmesConformity1Catalog();
    }

    @Test
    public void microGridBaseCaseBE() {
        new CgmesModelTester(catalog.microGridBaseCaseBE()).test();
    }

    @Test
    public void microGridBaseCaseNL() {
        new CgmesModelTester(catalog.microGridBaseCaseNL()).test();
    }

    @Test
    public void microGridBaseCaseAssembled() {
        new CgmesModelTester(catalog.microGridBaseCaseAssembled()).test();
    }

    @Test
    public void miniBusBranch() {
        new CgmesModelTester(catalog.miniBusBranch()).test();
    }

    @Test
    public void miniNodeBreaker() {
        new CgmesModelTester(catalog.miniNodeBreaker()).test();
    }

    @Test
    public void smallBusBranch() {
        new CgmesModelTester(catalog.smallBusBranch()).test();
    }

    @Test
    public void smallNodeBreaker() {
        new CgmesModelTester(catalog.smallNodeBreaker()).test();
    }

    // This tests fails in Appveyor with an OutOfMemory error
    // Temporal solution is comment it
    // @Test
    public void real() {
        new CgmesModelTester(catalog.real()).test();
    }

    private static CgmesConformity1Catalog catalog;
}
