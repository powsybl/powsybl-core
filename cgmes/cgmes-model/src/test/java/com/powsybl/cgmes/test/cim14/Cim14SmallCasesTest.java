package com.powsybl.cgmes.test.cim14;

/*
 * #%L
 * CGMES data model
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import org.junit.BeforeClass;
import org.junit.Test;

import com.powsybl.cgmes.test.CgmesModelTester;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cim14SmallCasesTest {
    @BeforeClass
    public static void setUp() {
        catalog = new Cim14SmallCasesCatalog();
    }

    @Test
    public void small1() {
        new CgmesModelTester(catalog.small1()).test();
    };

    @Test
    public void m7Buses() {
        new CgmesModelTester(catalog.m7buses()).test();
    }

    @Test
    public void ieee14() {
        new CgmesModelTester(catalog.ieee14()).test();
    }

    @Test
    public void nordic32() {
        new CgmesModelTester(catalog.nordic32()).test();
    }

    private static Cim14SmallCasesCatalog catalog;
}
