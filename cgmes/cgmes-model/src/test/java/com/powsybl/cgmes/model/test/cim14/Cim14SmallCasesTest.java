/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.model.test.cim14;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.model.test.CgmesModelTester;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesTest {
    @BeforeClass
    public static void setUp() {
        catalog = new Cim14SmallCasesCatalog();
    }

    @Test
    public void small1() throws IOException {
        new CgmesModelTester(catalog.small1()).test();
    };

    @Test
    public void m7Buses() throws IOException {
        new CgmesModelTester(catalog.m7buses()).test();
    }

    @Test
    public void ieee14() throws IOException {
        new CgmesModelTester(catalog.ieee14()).test();
    }

    @Test
    public void nordic32() throws IOException {
        new CgmesModelTester(catalog.nordic32()).test();
    }

    private static Cim14SmallCasesCatalog catalog;
}
