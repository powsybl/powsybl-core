/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conformity.test;

import java.io.IOException;

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
    public void microGridBaseCaseBE() throws IOException {
        new CgmesModelTester(catalog.microGridBaseCaseBE()).test();
    }

    @Test
    public void microGridBaseCaseNL() throws IOException {
        new CgmesModelTester(catalog.microGridBaseCaseNL()).test();
    }

    @Test
    public void microGridBaseCaseAssembled() throws IOException {
        new CgmesModelTester(catalog.microGridBaseCaseAssembled()).test();
    }

    @Test
    public void miniBusBranch() throws IOException {
        new CgmesModelTester(catalog.miniBusBranch()).test();
    }

    @Test
    public void miniNodeBreaker() throws IOException {
        new CgmesModelTester(catalog.miniNodeBreaker()).test();
    }

    @Test
    public void smallBusBranch() throws IOException {
        new CgmesModelTester(catalog.smallBusBranch()).test();
    }

    @Test
    public void smallNodeBreaker() throws IOException {
        new CgmesModelTester(catalog.smallNodeBreaker()).test();
    }

    private static CgmesConformity1Catalog catalog;
}
